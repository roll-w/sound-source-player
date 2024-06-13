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
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist

/**
 * Player should register to this [AudioPlaylistProvider]
 * to get the playlist and playlist info, and also control
 * play position.
 *
 * For Fragment or Activity, it should provide the
 * playlist and playlist info to this [AudioPlaylistProvider],
 * and through [androidx.media3.common.Player] or
 * [AudioPlaylistProvider] to control the play position.
 *
 * @see DefaultAudioPlaylistProvider
 * @see AudioPlaylistDelegatePlayer
 * @author RollW
 */
interface AudioPlaylistProvider {
    val index: Int

    val current: AudioContent?

    val playlist: List<AudioContent>

    val playlistInfo: Playlist

    fun setIndex(
        index: Int,
        extras: Bundle? = null
    )

    fun setPlaylist(
        playlist: List<AudioContent>,
        playlistInfo: Playlist,
        index: Int = 0,
        extras: Bundle? = null
    )

    fun addOnAudioPlaylistListener(listener: OnAudioPlaylistListener)

    fun removeOnAudioPlaylistListener(listener: OnAudioPlaylistListener)

    interface OnAudioPlaylistListener {
        fun onPlaylistChanged(
            playlist: List<AudioContent>,
            playlistInfo: Playlist,
            index: Int,
            extras: Bundle?
        ) {
        }

        fun onPlaylistItemChanged(
            audio: AudioContent,
            index: Int,
            extras: Bundle?
        ) {
        }
    }

    companion object {
        /**
         * The extra key for the source of the playlist change.
         *
         * The value should be [String].
         */
        const val EXTRA_CHANGE_SOURCE = "AudioPlaylistProvider.EXTRA_CHANGE_SOURCE"

        fun AudioPlaylistProvider.resetEmptyPlaylist(extras: Bundle? = null) {
            setPlaylist(
                playlist = emptyList(),
                playlistInfo = Playlist.EMPTY,
                index = 0,
                extras = extras
            )
        }
    }
}