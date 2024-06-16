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

package tech.rollw.player.ui.player.viewmodel

import android.os.Bundle
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.player.AudioPlaylistProvider

/**
 * @author RollW
 */
class PlaylistViewModelUpdater(
    private val playlistViewModel: PlaylistViewModel
) : AudioPlaylistProvider.OnAudioPlaylistListener {
    private var audioPlaylistProvider: AudioPlaylistProvider? = null

    fun init(audioPlaylistProvider: AudioPlaylistProvider) {
        if (this.audioPlaylistProvider != null &&
            this.audioPlaylistProvider != audioPlaylistProvider
        ) {
            release()
        }

        this.audioPlaylistProvider = audioPlaylistProvider
        audioPlaylistProvider.addOnAudioPlaylistListener(this)
        playlistViewModel.setPlaylist(
            audioPlaylistProvider.playlistInfo,
            audioPlaylistProvider.playlist,
            audioPlaylistProvider.index
        )
    }

    fun release() {
        audioPlaylistProvider?.removeOnAudioPlaylistListener(this)
        audioPlaylistProvider = null
    }

    override fun onPlaylistChanged(
        playlist: List<AudioContent>,
        playlistInfo: Playlist,
        index: Int,
        extras: Bundle?
    ) {
        val source = extras?.getString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE)
        playlistViewModel.setPlaylist(
            playlistInfo,
            playlist,
            index,
            source
        )
    }

    override fun onPlaylistItemChanged(
        audio: AudioContent,
        index: Int,
        extras: Bundle?
    ) {
        val source = extras?.getString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE)
        playlistViewModel.setIndex(index, source)
    }
}