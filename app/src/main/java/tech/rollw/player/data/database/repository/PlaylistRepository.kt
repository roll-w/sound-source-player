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

package tech.rollw.player.data.database.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.list.PlaylistType
import tech.rollw.player.data.database.PlayerDatabase
import tech.rollw.player.data.database.dao.PlaylistDao

/**
 * @author RollW
 */
class PlaylistRepository(
    context: Context
) : AutoPrimaryKeyRepository<Playlist>(context) {

    override fun getDao(database: PlayerDatabase): PlaylistDao =
        database.getPlaylistDao()

    fun getByName(name: String, type: PlaylistType): Playlist? =
        (dao as PlaylistDao).getByName(name, type)

    fun getByNameFlow(name: String, type: PlaylistType): Flow<Playlist?> =
        (dao as PlaylistDao).getByNameFlow(name, type)

    fun getByNames(names: Collection<String>, type: PlaylistType): List<Playlist> =
        (dao as PlaylistDao).getByNames(names, type)

    fun getByNamesFlow(names: Collection<String>, type: PlaylistType): Flow<List<Playlist>> =
        (dao as PlaylistDao).getByNamesFlow(names, type)

    fun getByType(type: PlaylistType): List<Playlist> =
        (dao as PlaylistDao).getByType(type)

    fun getByTypeFlow(type: PlaylistType): Flow<List<Playlist>> =
        (dao as PlaylistDao).getByTypeFlow(type)
}