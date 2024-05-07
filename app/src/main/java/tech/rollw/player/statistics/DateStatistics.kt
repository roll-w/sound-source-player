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

package tech.rollw.player.statistics

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.time.LocalDate

/**
 * @author RollW
 */
@Entity(tableName = "date_statistics", primaryKeys = ["key", "date"])
data class DateStatistics(
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "value")
    val value: Long = 0,

    @ColumnInfo(name = "date")
    val date: LocalDate = LocalDate.now()
) {
    val type: StatisticsType
        get() = StatisticsType.fromKey(key)

    companion object {
        fun today(): Int = toRawDate(LocalDate.now())

        /**
         * Get the date in the format of 20000101 for 2000-01-01
         */
        fun toRawDate(date: LocalDate): Int {
            return date.year * 10000 + date.monthValue * 100 + date.dayOfMonth
        }

        fun fromRawDate(date: Int): LocalDate {
            val year = date / 10000
            val month = date / 100 % 100
            val day = date % 100
            return LocalDate.of(year, month, day)
        }
    }
}
