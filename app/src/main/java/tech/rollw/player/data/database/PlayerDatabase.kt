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

package tech.rollw.player.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioPath
import tech.rollw.player.audio.AudioStatistics
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.data.database.dao.AudioDao
import tech.rollw.player.data.database.dao.AudioPathDao
import tech.rollw.player.data.database.dao.DateStatisticsDao
import tech.rollw.player.data.database.dao.PlaylistDao
import tech.rollw.player.data.database.dao.StatisticsDao
import tech.rollw.player.statistics.DateStatistics
import tech.rollw.player.statistics.Statistics
import kotlin.concurrent.Volatile

/**
 * @author RollW
 */
@Database(
    entities = [
        Audio::class,
        AudioPath::class,
        Playlist::class,
        AudioStatistics::class,
        Statistics::class, DateStatistics::class
    ],
    version = 1
)
@TypeConverters(DataConverter::class)
abstract class PlayerDatabase : RoomDatabase() {

    abstract fun getAudioDao(): AudioDao

    abstract fun getAudioPathDao(): AudioPathDao

    abstract fun getPlaylistDao(): PlaylistDao

    abstract fun getStatisticsDao(): StatisticsDao

    abstract fun getDateStatisticsDao(): DateStatisticsDao

    companion object {
        @Volatile
        private var INSTANCE: PlayerDatabase? = null

        private const val DATABASE_NAME = "player_database"

        fun getDatabase(context: Context): PlayerDatabase {
            if (INSTANCE == null) {
                synchronized(PlayerDatabase::class) {
                    if (INSTANCE == null) {
                        INSTANCE = databaseBuilder(
                            context.applicationContext, PlayerDatabase::class.java,
                            DATABASE_NAME
                        ).allowMainThreadQueries()
                            .enableMultiInstanceInvalidation()
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        fun deleteDatabase(context: Context) {
            INSTANCE = null
            context.deleteDatabase(DATABASE_NAME)
        }
    }
}