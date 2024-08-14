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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.rollw.support.io.ContentPath

/**
 * The path of the audio file.
 *
 * @author RollW
 */
// We choose split the audio path from the [Audio] class,
// because there are some files that have multiple paths,
// but they can still referring to the same file.
// We need to handle this but also don't want the [Audio]
// to become too complex.
@Entity(
    tableName = "audio_path",
    indices = [
        Index("identifier"),
        Index("id")
    ]
)
data class AudioPath(
    @PrimaryKey
    @Embedded
    val path: ContentPath,

    /**
     * To identify the audio path.
     *
     * Should be unique from different audio files.
     */
    @ColumnInfo(name = "identifier")
    val identifier: String,

    /**
     * Refer to [Audio.id]
     */
    @ColumnInfo
    val id: Long,
)

fun ContentPath.toAudioPath(id: Long, identifier: String) = AudioPath(
    this,
    identifier = identifier,
    id = id
)

