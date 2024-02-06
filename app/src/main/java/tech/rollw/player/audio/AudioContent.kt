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

import tech.rollw.support.io.ContentPath

/**
 * Combine audio and its content path for
 * easy access to the audio file.
 *
 * @author RollW
 */
data class AudioContent(
    val audio: Audio,
    val path: ContentPath
)

fun Audio.toAudioContent(path: ContentPath) = AudioContent(this, path)

fun List<Audio>.toAudioContentList(audioPaths: List<AudioPath>) = map {
    val audioPath = audioPaths.find { path -> path.id == it.id }
    if (audioPath == null) {
        throw IllegalArgumentException("Audio path cannot found for audio: ${it.id}.")
    }
    AudioContent(it, audioPath.path)
}
