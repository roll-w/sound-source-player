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
 * @author RollW
 */
class DefaultAudioPlaylistProvider : AudioPlaylistProvider {
    private var positionInternal: Int = 0

    override val position: Int
        get() = positionInternal

    private var _playlist: List<AudioContent> = emptyList()
    private var _playlistInfo: Playlist = Playlist.EMPTY

    override val current: AudioContent?
        get() {
            if (playlist.isEmpty()) {
                return null
            }
            return playlist[position]
        }

    override val playlist: List<AudioContent>
        get() = _playlist

    override val playlistInfo: Playlist
        get() = _playlistInfo

    override fun setPosition(
        position: Int,
        extras: Bundle?
    ) {
        setPositionInternal(position, true, extras)
    }

    private val listeners = mutableListOf<AudioPlaylistProvider.OnAudioPlaylistListener>()

    override fun setPlaylist(
        playlist: List<AudioContent>,
        playlistInfo: Playlist,
        position: Int,
        extras: Bundle?
    ) {
        if (anyChange(playlist, playlistInfo)) {
            this._playlist = playlist
            this._playlistInfo = playlistInfo
            setPositionInternal(position, false)
            listeners.forEach {
                it.onPlaylistChanged(playlist, playlistInfo, position, extras)
            }
            return
        }
        if (positionInternal == position) {
            return
        }
        setPositionInternal(position, true, extras)
    }

    private fun anyChange(
        playlist: List<AudioContent>,
        playlistInfo: Playlist
    ): Boolean {
        return this.playlist != playlist || this.playlistInfo != playlistInfo
    }

    override fun addOnAudioPlaylistListener(
        listener: AudioPlaylistProvider.OnAudioPlaylistListener
    ) {
        listeners.add(listener)
    }

    override fun removeOnAudioPlaylistListener(
        listener: AudioPlaylistProvider.OnAudioPlaylistListener
    ) {
        listeners.remove(listener)
    }

    @Throws(IndexOutOfBoundsException::class)
    private fun setPositionInternal(
        position: Int,
        notify: Boolean = true,
        extras: Bundle? = null
    ) {
        if (positionInternal == position) {
            return
        } else if (position < 0 ||
            (position >= playlist.size && playlist.isNotEmpty())
        ) {
            throw IndexOutOfBoundsException("Got position: $position, but playlist size: ${playlist.size}.")
        }
        positionInternal = position
        if (!notify) {
            return
        }
        listeners.forEach {
            it.onPlaylistItemChanged(playlist[position], position, extras)
        }
    }
}