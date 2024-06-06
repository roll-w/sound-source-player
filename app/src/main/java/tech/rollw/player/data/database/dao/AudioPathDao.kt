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
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.rollw.player.audio.AudioPath
import tech.rollw.support.io.PathType

/**
 * @author RollW
 */
@Dao
abstract class AudioPathDao : PlayerDao<AudioPath> {
    @Insert
    abstract override fun insert(entity: AudioPath)

    @Query("SELECT * FROM audio_path WHERE id = :audioId")
    abstract fun getById(audioId: Long): Flow<List<AudioPath>>

    @Query("SELECT * FROM audio_path WHERE path = :path AND type = :type")
    abstract fun getByPath(path: String, type: PathType): AudioPath?

    @Query("SELECT * FROM audio_path")
    abstract override fun get(): List<AudioPath>

    @Query("SELECT * FROM audio_path")
    abstract override fun getFlow(): Flow<List<AudioPath>>

    @Query("SELECT * FROM audio_path WHERE identifier = :identifier")
    abstract fun getByIdentifier(identifier: String): List<AudioPath>
}