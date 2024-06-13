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

package tech.rollw.player.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.list.PlaylistType

/**
 * @author RollW
 */
@Dao
interface PlaylistDao : AutoPrimaryKeyDao<Playlist> {
    @Query("SELECT * FROM playlist WHERE id = :id")
    override fun getById(id: Long): Playlist?

    @Query("SELECT * FROM playlist")
    override fun getFlow(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist")
    override fun get(): List<Playlist>

    @Query("DELETE FROM playlist WHERE id = :id")
    override fun deleteById(id: Long)

    @Query("DELETE FROM playlist WHERE id IN (:ids)")
    override fun deleteByIds(ids: Collection<Long>)

    @Query("SELECT * FROM playlist WHERE name = :name AND type = :type")
    fun getByName(name: String, type: PlaylistType): Playlist?

    @Query("SELECT * FROM playlist WHERE name = :name AND type = :type")
    fun getByNameFlow(name: String, type: PlaylistType): Flow<Playlist?>

    @Query("SELECT * FROM playlist WHERE name IN (:names) AND type = :type")
    fun getByNames(names: Collection<String>, type: PlaylistType): List<Playlist>

    @Query("SELECT * FROM playlist WHERE name IN (:names) AND type = :type")
    fun getByNamesFlow(names: Collection<String>, type: PlaylistType): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE type = :type")
    fun getByType(type: PlaylistType): List<Playlist>

    @Query("SELECT * FROM playlist WHERE type = :type")
    fun getByTypeFlow(type: PlaylistType): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE id IN (:ids)")
    override fun getByIds(ids: Collection<Long>): List<Playlist>

    @Query("SELECT * FROM playlist WHERE id IN (:ids)")
    override fun getByIdsFlow(ids: Collection<Long>): Flow<List<Playlist>>

}