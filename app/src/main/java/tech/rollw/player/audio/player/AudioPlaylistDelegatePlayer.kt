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
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.toMediaItem

/**
 * This delegate player will handle the playlist changed from
 * foreground, and automatically switch to the next or previous
 * audio when the current media item is ended. Which means the
 * activity or fragment should not call methods such as [Player.seekToNext],
 * they only need to change the [AudioPlaylistProvider.position].
 *
 * This delegate player will also override some methods of the
 * [player] implementation, such as [Player.seekToNext],
 * [Player.seekToPrevious] and so on.
 *
 * @param player player to delegate
 * @author RollW
 */
// TODO: shuffle mode and repeat mode.
@OptIn(UnstableApi::class)
class AudioPlaylistDelegatePlayer(
    private val player: Player,
    private val audioPlaylistProvider: AudioPlaylistProvider
) : Player by player, Player.Listener, AudioPlaylistProvider.OnAudioPlaylistListener {
    init {
        audioPlaylistProvider.addOnAudioPlaylistListener(this)
        player.addListener(this)
    }

    /**
     * For internal use, to identify the change source.
     */
    private val identifier = Bundle().apply {
        putString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE, TAG)
    }

    override fun getAvailableCommands(): Commands = Commands
        .Builder()
        .addAll(
            Player.COMMAND_PLAY_PAUSE,
            Player.COMMAND_PREPARE,
            Player.COMMAND_STOP,
            Player.COMMAND_SET_MEDIA_ITEM,
            Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
            Player.COMMAND_GET_METADATA,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
        )
        .addAll(player.availableCommands)
        .build()


    override fun hasNextMediaItem(): Boolean {
        return audioPlaylistProvider.position + 1 <
                audioPlaylistProvider.playlist.size
    }

    override fun hasPreviousMediaItem(): Boolean {
        return audioPlaylistProvider.position - 1 >= 0
    }

    override fun seekToNextMediaItem() = seekToNext()

    override fun seekToNext() {
        if (audioPlaylistProvider.position + 1 >=
            audioPlaylistProvider.playlist.size
        ) {
            return
        }
        val playingState = player.isPlaying
        audioPlaylistProvider.setPosition(
            audioPlaylistProvider.position + 1,
            identifier
        )

        audioPlaylistProvider.current?.let {
            setAudioAndPlay(it, playingState)
        }
    }

    override fun seekToPreviousMediaItem() = seekToPrevious()

    override fun seekToPrevious() {
        if (audioPlaylistProvider.position - 1 < 0) {
            return
        }
        val playingState = player.isPlaying
        audioPlaylistProvider.setPosition(
            audioPlaylistProvider.position - 1,
            identifier
        )

        audioPlaylistProvider.current?.let {
            setAudioAndPlay(it, playingState)
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.w(TAG, "onPlayerError: $error")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
            seekToNext()
        }
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?, @MediaItemTransitionReason reason: Int
    ) {

    }

    private fun setAudioAndPlay(
        audioContent: AudioContent,
        play: Boolean
    ) {
        val mediaItem = audioContent.toMediaItem()
        setMediaItem(mediaItem)
        prepare()

        if (play) {
            playWhenReady = true
        }
    }

    override fun onPlaylistChanged(
        playlist: List<AudioContent>,
        playlistInfo: Playlist,
        position: Int,
        extras: Bundle?
    ) {
        if (playlist.isEmpty()) {
            return
        }
        val playingState = extras?.getBoolean(EXTRA_PLAY, isPlaying)
            ?: isPlaying
        setAudioAndPlay(playlist[position], playingState)
    }

    override fun onPlaylistItemChanged(
        audio: AudioContent,
        position: Int,
        extras: Bundle?
    ) {
        if (extras == identifier ||
            extras?.getString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE) == TAG
        ) {
            return
        }
        val playingState = extras?.getBoolean(EXTRA_PLAY, isPlaying)
            ?: isPlaying
        setAudioAndPlay(audio, playingState)
    }

    companion object {
        private const val TAG = "DelegatePlayer"

        /**
         * The extra key for the [AudioPlaylistDelegatePlayer] to
         * play the audio when the playlist changed.
         *
         * The value should be a boolean. If it is true, the player
         * should play the audio when the playlist changed.
         *
         * If not set, will play the audio according to the current
         * state (whether it is playing or not).
         */
        const val EXTRA_PLAY = "AudioPlaylistDelegatePlayer.EXTRA_PLAY"
    }
}

fun Player.withAudioPlaylistProvider(
    audioPlaylistProvider: AudioPlaylistProvider
): Player {
    if (this is AudioPlaylistDelegatePlayer) {
        return this
    }
    return AudioPlaylistDelegatePlayer(this, audioPlaylistProvider)
}
