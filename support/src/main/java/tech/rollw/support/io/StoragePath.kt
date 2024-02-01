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

import android.net.Uri
import java.io.File

/**
 * @author RollW
 */
data class StoragePath(
    val path: String,
    val type: PathType
) {
    fun toUri(): Uri {
        return when (type) {
            PathType.FILE -> Uri.fromFile(File(path))
            PathType.URI -> Uri.parse(path)
        }
    }

    fun getSuffix(): String {
        return FileUtils.getSuffix(path)
    }

}

fun String.toStoragePath(type: PathType) = StoragePath(this, type)

fun Uri.toStoragePath() = StoragePath(toString(), PathType.URI)
