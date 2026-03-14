/*
 * Copyright (C) 2024 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.rollw.player.audio.player

import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ThreadFactoryBuilder
import tech.rollw.player.audio.toAudioContent
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author RollW
 */
@UnstableApi
@ExperimentalPlayerApi
class AudioPlayerBridge(
    val player: AudioPlayer
) : SimpleBasePlayer(Looper.getMainLooper()), AudioPlayer.Listener {
    private var mediaItem: MediaItem? = null

    private var playWhenReady = false

    init {
        player.addListener(this)
    }

    override fun getState(): State {
        return State.Builder()
            .setAvailableCommands(COMMANDS)
            .setContentPositionMs { player.position }
            .setCurrentMediaItemIndex(
                if (mediaItem == null) {
                    C.INDEX_UNSET
                } else {
                    0
                }
            )
            .setPlayWhenReady(
                playWhenReady, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
            )
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setIsLoading(player.playbackState == AudioPlayer.PlaybackState.BUFFERING)
            .setPlaybackState(toMedia3PlaybackState(player.playbackState))
            .setPlaylist(getPlaylist())
            .build()
            .also {
                Log.d("AudioPlayerBridge", "Called getState(): ${it.toHumanReadableString()}")
            }
    }

    private fun State.toHumanReadableString(): String {
        return "State(" +
                "playbackState=$playbackState, " +
                "playWhenReady=$playWhenReady, " +
                "playWhenReadyChangeReason=$playWhenReadyChangeReason, " +
                "isLoading=$isLoading, " +
                "playlist=${playlist}}, " +
                "timeline=${timeline}" +
                ")"
    }

    @Player.State
    private fun toMedia3PlaybackState(playbackState: AudioPlayer.PlaybackState): Int {
        return when (playbackState) {
            AudioPlayer.PlaybackState.IDLE,
            AudioPlayer.PlaybackState.STOPPED -> Player.STATE_IDLE

            AudioPlayer.PlaybackState.BUFFERING -> Player.STATE_BUFFERING
            AudioPlayer.PlaybackState.PLAYING,
            AudioPlayer.PlaybackState.PAUSED -> Player.STATE_READY

            AudioPlayer.PlaybackState.ENDED -> Player.STATE_ENDED
            else -> throw IllegalArgumentException("Invalid playback state: $playbackState")
        }
    }

    private fun toMedia3PlayWhenReadyChangeReason(
        playbackState: AudioPlayer.PlaybackState
    ): Int {
        return when (playbackState) {
            AudioPlayer.PlaybackState.PLAYING -> Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
            AudioPlayer.PlaybackState.PAUSED -> Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
            AudioPlayer.PlaybackState.ENDED -> Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM
            else -> Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
        }
    }

    private fun getPlaylist(): List<MediaItemData> {
        if (mediaItem == null) {
            return emptyList()
        }
        val mediaItem = mediaItem!!
        return listOf(
            MediaItemData.Builder(mediaItem.mediaId)
                .setMediaItem(mediaItem)
                .build()
        )
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        this.playWhenReady = playWhenReady

        return Futures.submit({
            if (playWhenReady) {
                player.play()
            } else {
                player.pause()
            }
        }, executor)
    }

    override fun handlePrepare(): ListenableFuture<*> {
        if (mediaItem == null) {
            throw IllegalStateException("MediaItem is not set.")
        }

        player.addListener(this)

        return Futures.submit({
            try {
                player.prepare(mediaItem!!.toAudioContent())
            } catch (e: Exception) {
                Log.e("AudioPlayerBridge", "Failed to prepare media item.", e)
            }
        }, executor)
    }

    override fun handleStop(): ListenableFuture<*> {
        return Futures.submit({
            try {
                player.stop()
            } catch (e: Exception) {
                Log.e("AudioPlayerBridge", "Failed to stop player.", e)
            }
        }, executor)
    }

    override fun handleRelease(): ListenableFuture<*> {
        player.removeListener(this)

        return Futures.submit({
            try {
                player.stop()
            } catch (e: Exception) {
                Log.e("AudioPlayerBridge", "Failed to release player.", e)
            }
        }, executor)
    }

    override fun handleSetVolume(volume: Float): ListenableFuture<*> {
        return Futures.submit({
            player.setAudioVolume(volume)
        }, executor)
    }

    override fun handleSetMediaItems(
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<*> {
        mediaItem = when {
            mediaItems.isEmpty() -> {
                null
            }

            startIndex == C.INDEX_UNSET -> {
                mediaItems.first()
            }

            else -> {
                mediaItems[startIndex]
            }
        }
        return Futures.immediateVoidFuture()
    }

    override fun handleSeek(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int
    ): ListenableFuture<*> {
        return Futures.submit({
            try {
                player.seekTo(positionMs)
            } catch (e: Exception) {
                Log.e("AudioPlayerBridge", "Failed to seek to position.", e)
            }
        }, executor)
    }

    override fun onPlaybackStateChanged(state: AudioPlayer.PlaybackState) {
        when (state) {
            AudioPlayer.PlaybackState.ENDED -> {
                invalidateState()
            }

            else -> {}
        }
    }

    private val executor = ThreadPoolExecutor(
        1, 1,
        10L, TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        ThreadFactoryBuilder()
            .setNameFormat("AudioPlayerBridge-%d")
            .setUncaughtExceptionHandler { _, e ->
                Log.e("AudioPlayerBridge", "Uncaught exception in player executor.", e)
            }
            .build()
    )

    companion object {
        private val COMMANDS = Player.Commands.Builder()
            .addAll(
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_PREPARE,
                Player.COMMAND_STOP,
                Player.COMMAND_RELEASE,
                Player.COMMAND_SET_MEDIA_ITEM,
                Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
            )
            .build()

        private val AUDIO_ATTRIBUTES = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
}
