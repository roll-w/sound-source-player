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
    private var indexInternal: Int = 0

    override val index: Int
        get() = indexInternal

    private var _playlist: List<AudioContent> = emptyList()
    private var _playlistInfo: Playlist = Playlist.EMPTY

    override val current: AudioContent?
        get() {
            if (playlist.isEmpty()) {
                return null
            }
            return playlist[index]
        }

    override val playlist: List<AudioContent>
        get() = _playlist

    override val playlistInfo: Playlist
        get() = _playlistInfo

    override fun setIndex(
        index: Int,
        extras: Bundle?
    ) {
        setIndexInternal(index, true, extras)
    }

    private val listeners = mutableListOf<AudioPlaylistProvider.OnAudioPlaylistListener>()

    override fun setPlaylist(
        playlist: List<AudioContent>,
        playlistInfo: Playlist,
        index: Int,
        extras: Bundle?
    ) {
        if (anyChange(playlist, playlistInfo)) {
            this._playlist = playlist
            this._playlistInfo = playlistInfo
            setIndexInternal(index, false)
            listeners.forEach {
                it.onPlaylistChanged(playlist, playlistInfo, index, extras)
            }
            return
        }
        if (indexInternal == index) {
            return
        }
        setIndexInternal(index, true, extras)
    }

    private fun anyChange(
        playlist: List<AudioContent>,
        playlistInfo: Playlist
    ): Boolean {
        return this.playlistInfo != playlistInfo || this.playlist != playlist
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
    private fun setIndexInternal(
        index: Int,
        notify: Boolean = true,
        extras: Bundle? = null
    ) {
        if (indexInternal == index) {
            return
        } else if (index < 0 ||
            (index >= playlist.size && playlist.isNotEmpty())
        ) {
            throw IndexOutOfBoundsException("Got position: $index, but playlist size: ${playlist.size}.")
        }
        indexInternal = index
        if (!notify) {
            return
        }
        listeners.forEach {
            it.onPlaylistItemChanged(playlist[index], index, extras)
        }
    }
}