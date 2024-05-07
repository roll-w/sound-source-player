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

/**
 * @author RollW
 */
enum class StatisticsType(
    val key: String
) {
    LISTEN_COUNT("listen_count"),
    LISTEN_TIME("listen_time"),
    SONG_COUNT("song_count"),
    ARTIST_COUNT("artist_count"),
    ALBUM_COUNT("album_count"),
    PLAYLIST_COUNT("playlist_count"),
    UNDEFINED("undefined");

    companion object {
        fun fromKey(key: String): StatisticsType {
            return entries.find { it.key == key } ?: UNDEFINED
        }
    }
}