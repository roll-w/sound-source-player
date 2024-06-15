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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.AudioPath
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.list.PlaylistItem
import tech.rollw.player.audio.list.PlaylistType
import tech.rollw.player.data.database.repository.AudioPathRepository
import tech.rollw.player.data.database.repository.AudioRepository
import tech.rollw.player.data.database.repository.PlaylistItemRepository
import tech.rollw.player.data.database.repository.PlaylistRepository
import tech.rollw.player.getApplicationService
import tech.rollw.player.ui.player.AudioOrder
import tech.rollw.player.ui.player.AudioUtils.sortBy

/**
 * @author RollW
 */
class AudioListViewModel(
    private val audioRepository: AudioRepository,
    private val audioPathRepository: AudioPathRepository,
    private val playlistRepository: PlaylistRepository,
    private val playlistItemRepository: PlaylistItemRepository
) : ViewModel() {

    val audios: Flow<List<Audio>> = audioRepository.getFlow()
        .distinctUntilChanged()

    val audioPaths: Flow<List<AudioPath>> = audioPathRepository.getFlow()
        .distinctUntilChanged()

    val audioContents: Flow<List<AudioContent>> =
        audios.combine(audioPaths) { audios, audioPaths ->
            audios.mapNotNull { audio ->
                val path = audioPaths.find { it.id == audio.id } ?: return@mapNotNull null
                AudioContent(audio, path.path)
            }
        }.distinctUntilChanged()

    fun getAudioContents(
        audioOrder: AudioOrder,
        reverse: Boolean = false
    ): SharedFlow<List<AudioContent>> =
        audioContents.map {
            it.sortBy(audioOrder, reverse)
        }.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    fun getAudioPath(audio: Audio): Flow<List<AudioPath>> =
        audio.id?.let {
            audioPathRepository.getByAudioIdFlow(it)
        } ?: throw IllegalArgumentException("Audio id is null.")

    val playlists: Flow<List<Playlist>> = playlistRepository.getFlow()
        .distinctUntilChanged()

    fun getPlaylists(type: PlaylistType): Flow<List<Playlist>> =
        playlistRepository.getByTypeFlow(type)
            .distinctUntilChanged()

    fun getRawPlaylistItems(playlist: Playlist): Flow<List<PlaylistItem>> =
        if (playlist.id == null) emptyFlow()
        else playlistItemRepository.getByPlaylistFlow(playlist.id)
            .distinctUntilChanged()

    fun getPlaylistItems(playlist: Playlist): Flow<List<AudioContent>> =
        if (playlist.id == null) emptyFlow()
        else playlistItemRepository.getByPlaylistFlow(playlist.id)
            .distinctUntilChanged()
            .map { playlistItems ->
                val ids = playlistItems.map { it.audioId }
                getAudioContentByIds(ids).last()
            }

    private fun getAudioByIds(ids: List<Long>): Flow<List<Audio>> {
        return audios.map { audios ->
            audios.filter { it.id in ids }
        }.distinctUntilChanged()
    }

    private fun getAudioContentByIds(ids: List<Long>): Flow<List<AudioContent>> {
        return audios.combine(audioPaths) { audios, audioPaths ->
            audios.filter { it.id in ids }
                .mapNotNull { audio ->
                    val path = audioPaths.find { it.id == audio.id } ?: return@mapNotNull null
                    AudioContent(audio, path.path)
                }
        }.distinctUntilChanged()
    }

    companion object {
        val FACTORY: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                        as Context
                val audioRepository = context.getApplicationService(AudioRepository::class.java)
                val audioPathRepository =
                    context.getApplicationService(AudioPathRepository::class.java)
                val playlistRepository =
                    context.getApplicationService(PlaylistRepository::class.java)
                val playlistItemRepository =
                    context.getApplicationService(PlaylistItemRepository::class.java)

                AudioListViewModel(
                    audioRepository, audioPathRepository,
                    playlistRepository, playlistItemRepository
                )
            }
        }
    }
}