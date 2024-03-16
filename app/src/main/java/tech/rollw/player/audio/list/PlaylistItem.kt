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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tech.rollw.player.audio.list.PlaylistItem.Companion.NO_ITEM

/**
 * The item in the playlist.
 *
 * @author RollW
 */
@Entity(tableName = "playlist_item")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long?,
    /**
     * Refer to [Playlist.id]
     */
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,
    /**
     * Refer to [tech.rollw.player.audio.Audio.id]
     */
    @ColumnInfo(name = "audio_id")
    val audioId: Long,

    /**
     * Whether it is the first item in the playlist.
     *
     * If it is true, it means it is the first item in the playlist.
     */
    @ColumnInfo(name = "is_top")
    val isTop: Boolean = false,

    /**
     * The next item in the playlist.
     *
     * If it is [NO_ITEM], it means it is the last item in the playlist.
     */
    @ColumnInfo(name = "next")
    val next: Long = NO_ITEM
) {

    init {
        require(id == null || id >= 0) { "The id must not negative." }
        require(playlistId >= 0) { "The playlistId must not negative." }
        require(audioId >= 0) { "The audioId must not negative." }
    }

    companion object {
        const val NO_ITEM = -1L
    }
}
