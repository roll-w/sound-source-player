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

package tech.rollw.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.ui.player.viewmodel.PlayerViewModel
import tech.rollw.player.ui.player.viewmodel.PlaylistViewModel
import tech.rollw.support.SourcedData

/**
 * @author RollW
 */
class PlayerStateViewModel : ViewModel(), PlaylistViewModel, PlayerViewModel {
    private val _scrollOffset = MutableStateFlow(0)

    /**
     * Current scroll offset of the screen
     */
    val scrollOffset: StateFlow<Int> = _scrollOffset

    private val _playing = MutableStateFlow(false)
    private val _audioPosition = MutableStateFlow(0L)

    override val playing: StateFlow<Boolean> = _playing
    override val audioPosition: StateFlow<Long> = _audioPosition

    override fun setPlaying(playing: Boolean) {
        viewModelScope.launch {
            _playing.value = playing
        }
    }

    override fun setAudioPosition(position: Long) {
        viewModelScope.launch {
            _audioPosition.value = position
        }
    }

    private val _playlistInfo = MutableStateFlow(Playlist.EMPTY)
    private val _playlist = MutableStateFlow(emptyList<AudioContent>())
    private val _index = MutableStateFlow(SourcedData(0))

    override val playlistInfo: StateFlow<Playlist> = _playlistInfo
    override val playlist: StateFlow<List<AudioContent>> = _playlist
    override val index: StateFlow<SourcedData<Int>> = _index

    /**
     * Set current scroll offset
     *
     * @see scrollOffset
     */
    fun setScrollOffset(offset: Int = INVALID_SCROLL_OFFSET) {
        viewModelScope.launch {
            _scrollOffset.value = offset
        }
    }

    override fun setIndex(index: Int, source: String?) {
        viewModelScope.launch {
            _index.value = SourcedData(index, source)
        }
    }

    override fun setPlaylist(
        playlistInfo: Playlist,
        audioContents: List<AudioContent>,
        index: Int,
        source: String?
    ) {
        viewModelScope.launch {
            _playlistInfo.value = playlistInfo
            _playlist.value = audioContents
            _index.value = SourcedData(index, source)
        }
    }

    companion object {
        const val INVALID_SCROLL_OFFSET = Int.MIN_VALUE
    }
}