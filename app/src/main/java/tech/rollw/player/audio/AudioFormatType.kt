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

/**
 * @author RollW
 */
enum class AudioFormatType(val extension: String) {
    MP3("mp3"),
    FLAC("flac"),
    WAV("wav"),
    OGG("ogg"),
    AAC("aac"),
    WMA("wma"),
    APE("ape"),
    ALAC("alac"),
    AIFF("aiff"),
    DSD("dsd"),
    OPUS("opus"),
    ;

    companion object {
        @JvmStatic
        fun fromExtension(extension: String): AudioFormatType {
            return fromExtensionOrNull(extension)
                ?: throw IllegalArgumentException("Unknown extension: $extension")
        }

        @JvmStatic
        fun fromExtensionOrNull(extension: String): AudioFormatType? {
            return entries.find {
                it.extension.equals(extension, ignoreCase = true)
            }
        }
    }
}
