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

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * @author RollW
 */
interface PlayerDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<T>)

    @Delete
    fun delete(entity: T)

    @Update
    fun update(entity: T)

    fun get() : List<T>

    fun getFlow() : Flow<List<T>>
}