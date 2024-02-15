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

package tech.rollw.player.audio.list

import android.content.Context
import androidx.annotation.LongDef
import androidx.room.*
import tech.rollw.support.io.ContentPath

/**
 * @author RollW
 */
@Entity(
    tableName = "playlist",
    indices = [
        Index("name", "type", unique = true),
    ]
)
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long?,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "type")
    val type: PlaylistType,
    @ColumnInfo(name = "create_time")
    val createTime: Long = 0,
    @ColumnInfo(name = "update_time")
    val updateTime: Long = 0,

    /**
     * The path of the cover image of the playlist.
     * If it is null, the default cover will be used.
     */
    @Embedded(prefix = "cover_")
    val coverPath: ContentPath? = null,
    @ColumnInfo(name = "count")
    val count: Int = 0,
    @ColumnInfo(name = "duration")
    val duration: Long = 0
) {
    fun isValid() = id != null

    fun isUpdateTimeValid() = updateTime > 0

    fun isCreateTimeValid() = createTime > 0

    companion object {
        val EMPTY = Playlist(
            id = null,
            name = "",
            type = PlaylistType.OTHER,
        )


        fun ofSystem(
            @SystemPlaylistType id: Long,
            context: Context
        ) = Playlist(
            id = id,
            name = getNameOfSystemType(id, context),
            type = PlaylistType.OTHER,
        )

        const val LIST_SYSTEM_ALL = -1L
        const val LIST_SYSTEM_RECENT = -2L

        private fun getNameOfSystemType(
            @SystemPlaylistType type: Long,
            context: Context
        ) = when (type) {
            // TODO: get string from resources
            LIST_SYSTEM_ALL -> "All"
            LIST_SYSTEM_RECENT -> "Recent"
            else -> throw IllegalArgumentException("Unknown system playlist type: $type")
        }

    }

    @LongDef(LIST_SYSTEM_ALL, LIST_SYSTEM_RECENT)
    annotation class SystemPlaylistType
}