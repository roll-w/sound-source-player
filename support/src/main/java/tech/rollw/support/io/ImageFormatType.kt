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

package tech.rollw.support.io

enum class ImageFormatType(val mimeType: String, val extension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp"),
    GIF("image/gif", "gif"),
    BMP("image/bmp", "bmp"),
    ICO("image/x-icon", "ico"),
    UNKNOWN("image/*", ".");

    companion object {
        fun fromMimeType(mimeType: String?): ImageFormatType {
            if (mimeType == null) {
                return UNKNOWN
            }

            if (!mimeType.startsWith("image/")) {
                throw IllegalArgumentException("Invalid image MIME type: $mimeType")
            }
            for (type in entries) {
                if (type.mimeType == mimeType) {
                    return type
                }
            }
            return UNKNOWN
        }

        fun fromExtension(extension: String): ImageFormatType {
            val lowercase = extension.lowercase()
            for (type in entries) {
                if (type.extension == lowercase) {
                    return type
                }
            }
            return UNKNOWN
        }

        fun fromMimeTypeOrExtension(mimeType: String, extension: String): ImageFormatType {
            return fromMimeType(mimeType).takeIf { it != UNKNOWN }
                ?: fromExtension(extension).takeIf { it != UNKNOWN }
                ?: UNKNOWN
        }
    }
}