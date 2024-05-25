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

package tech.rollw.player.audio

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.rollw.player.audio.tag.AudioTag
import tech.rollw.player.audio.tag.AudioTagField

/**
 * @author RollW
 */
@Entity(
    tableName = "audio",
    indices = [
        Index("title"),
    ]
)
data class Audio(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "artist") val artist: String?,
    @ColumnInfo(name = "album") val album: String?,
    @ColumnInfo(name = "album_artist") val albumArtist: String?,
    @ColumnInfo(name = "composer") val composer: String?,
    @ColumnInfo(name = "lyricist") val lyricist: String?,
    @ColumnInfo(name = "arranger") val arranger: String?,
    @ColumnInfo(name = "track_number") val trackNo: String?,
    @ColumnInfo(name = "disk_number") val diskNo: String?,
    @ColumnInfo(name = "copyright") val copyright: String?,
    @ColumnInfo(name = "year") val year: String?,
    @ColumnInfo(name = "genre") val genre: String?,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "sample_rate") val sampleRate: Int,
    @ColumnInfo(name = "bit_rate") val bitRate: Int,
    @ColumnInfo(name = "channels") val channels: Int,
    @ColumnInfo(name = "type") val type: AudioFormatType,
    @ColumnInfo(name = "last_modified") val lastModified: Long,
    @ColumnInfo(name = "create_time") val createTime: Long
) {

    companion object {
        val EMPTY = Audio(
            null, null, null, null, null,
            null, null, null, null, null, null, null,
            null, 0, 0, 0, 0,
            AudioFormatType.MP3, 0, 0
        )
    }

    fun isEmpty() = this == EMPTY || id == null
}

fun AudioTag.toAudio(
    id: Long?,
    createTime: Long
) = Audio(
    id,
    getTagField(AudioTagField.TITLE),
    getTagField(AudioTagField.ARTIST),
    getTagField(AudioTagField.ALBUM),
    getTagField(AudioTagField.ALBUM_ARTIST),
    getTagField(AudioTagField.COMPOSER),
    getTagField(AudioTagField.LYRICIST),
    getTagField(AudioTagField.ARRANGER),
    getTagField(AudioTagField.TRACK_NUMBER),
    getTagField(AudioTagField.DISC_NUMBER),
    getTagField(AudioTagField.COPYRIGHT),
    getTagField(AudioTagField.DATE),
    getTagField(AudioTagField.GENRE),
    getAudioProperties().duration,
    getAudioProperties().sampleRate,
    getAudioProperties().bitRate,
    getAudioProperties().channels,
    audioFormatType,
    getLastModified(),
    createTime
)
