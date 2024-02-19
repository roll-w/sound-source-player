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

import java.io.File

/**
 * @author RollW
 */

/**
 * Try to get the file type by its extension.
 */
fun guessFileType(extension: String): FileType {
    val lowercase = extension.lowercase()

    // TODO: add more extensions
    return when (lowercase) {
        "txt", "md", "html", "xml", "json", "log" -> FileType.TEXT
        "mp3", "flac", "wav", "ogg", "m4a" -> FileType.AUDIO
        "mp4", "mkv", "avi", "mov", "wmv" -> FileType.VIDEO
        "jpg", "jpeg", "png", "gif", "webp" -> FileType.IMAGE
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx" -> FileType.DOCUMENT
        else -> FileType.OTHER
    }
}

fun File.fileType() = guessFileType(extension)

fun String.fileType() = guessFileType(
    substringAfterLast('.', "")
)
