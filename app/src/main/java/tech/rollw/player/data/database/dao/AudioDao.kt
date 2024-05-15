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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioPath

/**
 * @author RollW
 */
@Dao
abstract class AudioDao : AutoPrimaryKeyDao<Audio> {
    @Query("SELECT * FROM audio WHERE id = :id")
    abstract override fun getById(id: Long): Audio?

    @Query("SELECT * FROM audio")
    abstract override fun get(): List<Audio>

    @Query("SELECT * FROM audio")
    abstract override fun getFlow(): Flow<List<Audio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertPaths(paths: List<AudioPath>)

    @Transaction
    open fun insertAudioWithPath(
        audio: Audio,
        audioPaths: List<AudioPath>
    ): Long {
        val id = insertAndReturn(audio)
        val copies = audioPaths.map {
            it.copy(id = id)
        }
        insertPaths(copies)
        return id
    }
}