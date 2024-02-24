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

import kotlinx.coroutines.flow.StateFlow
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist
import tech.rollw.support.SourcedData

/**
 * Playlist view model. It provides the playlist information
 * and the current playing position.
 *
 * @author RollW
 */
interface PlaylistViewModel {
    // TODO: maybe we can use a single state flow to provide
    //  the playlist & playlistInfo and also with its source

    val playlistInfo: StateFlow<Playlist>

    fun setPlaylist(
        playlistInfo: Playlist,
        audioContents: List<AudioContent>,
        index: Int = 0,
        source: String? = null
    )

    val playlist: StateFlow<List<AudioContent>>

    fun setIndex(
        index: Int,
        source: String? = null
    )

    val index: StateFlow<SourcedData<Int>>
}