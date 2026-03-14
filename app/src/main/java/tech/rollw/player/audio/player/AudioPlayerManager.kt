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

import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ThreadFactoryBuilder
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.toAudioContent
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author RollW
 */
@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalPlayerApi::class)
class AudioPlayerManager(
    private val audioPlaylistProvider: AudioPlaylistProvider
) : AudioPlaylistProvider.OnAudioPlaylistListener {
    init {
        audioPlaylistProvider.addOnAudioPlaylistListener(this)
    }

    private var _audioPlayer: AudioPlayer? = null

    private val audioPlayer: AudioPlayer
        get() = _audioPlayer ?: throw IllegalStateException("AudioPlayer is not initialized.")

    fun asMedia3Player(): Player = TODO()

    fun applyConfig(config: Config) {
        this.config = config
        applyPlayMode()
    }

    private var config: Config = Config()

    var playMode: PlayMode
        get() = config.playMode
        set(value) {
            val old = config.playMode
            if (old == value) {
                return
            }
            config = config.copy(playMode = value)
            applyPlayMode()
        }

    private fun applyPlayMode() {
        // TODO
        when (config.playMode) {
            PlayMode.SHUFFLE -> {
            }

            else -> {}
        }
    }

    var playerEngine: AudioPlayerEngine
        get() = config.playerEngine
        set(value) {
            val old = config.playerEngine
            if (old == value) {
                return
            }
            config = config.copy(playerEngine = value)
            applyPlayerEngine()
        }

    private fun applyPlayerEngine() {
        // TODO
        val player = AudioPlayerFactory.createPlayer(playerEngine)
        applyAudioPlayer(player)
    }

    private fun applyAudioPlayer(audioPlayer: AudioPlayer) {
        _audioPlayer = audioPlayer
    }

    data class Config(
        val playMode: PlayMode = PlayMode.LOOP,
        val playerEngine: AudioPlayerEngine = AudioPlayerEngine.MEDIA_PLAYER,
        // TODO
    )

    fun seekToPrevious() {
        if (audioPlaylistProvider.index - 1 < 0) {
            return
        }
        val playingState = audioPlayer.playbackState.isPlaying()
        audioPlaylistProvider.setIndex(
            audioPlaylistProvider.index - 1,
            IDENTIFIER
        )

        audioPlaylistProvider.current?.let {
            setAudioAndPlay(it, playingState)
        }
    }

    fun seekToNext() {
        if (audioPlaylistProvider.index + 1 >=
            audioPlaylistProvider.playlist.size
        ) {
            return
        }
        val playingState = audioPlayer.playbackState.isPlaying()
        audioPlaylistProvider.setIndex(
            audioPlaylistProvider.index + 1,
            IDENTIFIER
        )

        audioPlaylistProvider.current?.let {
            setAudioAndPlay(it, playingState)
        }
    }

    fun hasNext(): Boolean {
        return audioPlaylistProvider.index + 1 <
                audioPlaylistProvider.playlist.size
    }

    fun hasPrevious(): Boolean {
        return audioPlaylistProvider.index - 1 >= 0
    }

    val position = audioPlayer.position

    private fun setAudioAndPlay(
        audioContent: AudioContent,
        play: Boolean
    ) {
        audioPlayer.prepare(audioContent)
        if (play) {
            audioPlayer.play()
        }
    }

    fun stop() {
        audioPlayer.stop()
    }

    // TODO
    private inner class AudioPlayerBridge :
        Player, SimpleBasePlayer(Looper.getMainLooper()), AudioPlayer.Listener {

        private var mediaItem: MediaItem? = null

        private var playWhenReady = false

        init {
            _audioPlayer?.addListener(this)
        }

        override fun getState(): State {
            return State.Builder()
                .setAvailableCommands(COMMANDS)
                .setContentPositionMs { position }
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
                .setIsLoading(audioPlayer.playbackState == AudioPlayer.PlaybackState.BUFFERING)
                .setPlaybackState(toMedia3PlaybackState(audioPlayer.playbackState))
                .setPlaylist(getPlaylist())
                .build()
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
                    audioPlayer.play()
                } else {
                    audioPlayer.pause()
                }
            }, executor)
        }

        override fun handlePrepare(): ListenableFuture<*> {
            if (mediaItem == null) {
                throw IllegalStateException("MediaItem is not set.")
            }

            audioPlayer.addListener(this)

            return Futures.submit({
                try {
                    audioPlayer.prepare(mediaItem!!.toAudioContent())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to prepare media item.", e)
                }
            }, executor)
        }

        override fun handleStop(): ListenableFuture<*> {
            return Futures.submit({
                try {
                    audioPlayer.stop()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to stop player.", e)
                }
            }, executor)
        }

        override fun handleRelease(): ListenableFuture<*> {
            audioPlayer.removeListener(this)

            return Futures.submit({
                try {
                    audioPlayer.stop()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to release player.", e)
                }
            }, executor)
        }

        override fun handleSetVolume(volume: Float): ListenableFuture<*> {
            return Futures.submit({
                audioPlayer.setAudioVolume(volume)
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
                    audioPlayer.seekTo(positionMs)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to seek to position.", e)
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
    }

    private val executor = ThreadPoolExecutor(
        1, 1,
        10L, TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        ThreadFactoryBuilder()
            .setNameFormat("AudioPlayerManager-%d")
            .setUncaughtExceptionHandler { _, e ->
                Log.e(TAG, "Uncaught exception in player executor.", e)
            }
            .build()
    )

    companion object {
        private const val TAG = "AudioPlayerManager"

        private val IDENTIFIER = Bundle().apply {
            putString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE, TAG)
        }

        /**
         * The extra key for the [AudioPlayerManager] to
         * play the audio when the playlist changed.
         *
         * The value should be a boolean. If it is true, the player
         * should play the audio when the playlist changed.
         *
         * If not set, will play the audio according to the current
         * state (whether it is playing or not).
         */
        const val EXTRA_PLAY = "tech.rollw.player.audio.player.AudioPlayerManager.EXTRA_PLAY"

        private val COMMANDS = Player.Commands.Builder()
            .addAll(
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_PREPARE,
                Player.COMMAND_STOP,
                Player.COMMAND_RELEASE,
                Player.COMMAND_SET_MEDIA_ITEM,
                Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                Player.COMMAND_GET_METADATA,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_NEXT,
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
            )
            .build()

        private val AUDIO_ATTRIBUTES = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
}