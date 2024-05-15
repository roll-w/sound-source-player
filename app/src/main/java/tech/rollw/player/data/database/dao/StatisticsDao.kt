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
import tech.rollw.player.statistics.Statistics

/**
 * @author RollW
 */
@Dao
abstract class StatisticsDao : PlayerDao<Statistics> {
    @Query("SELECT * FROM statistics")
    abstract override fun get(): List<Statistics>

    @Query("SELECT * FROM statistics")
    abstract override fun getFlow(): Flow<List<Statistics>>
}