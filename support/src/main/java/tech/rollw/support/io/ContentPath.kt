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
import tech.rollw.support.StringUtils.getSuffix
import java.io.File
import java.io.Serializable

/**
 * @author RollW
 */
data class ContentPath(
    val path: String,
    val type: PathType
) : Serializable {
    val extension: String
        get() = path.getSuffix()

    fun toUri(): Uri {
        return when (type) {
            PathType.FILE -> Uri.fromFile(File(path))
            PathType.URI -> Uri.parse(path)
        }
    }

    companion object {
        val EMPTY = ContentPath("", PathType.FILE)

        fun String.toContentPath(type: PathType) = ContentPath(this, type)

        fun Uri.toContentPath() = ContentPath(toString(), PathType.URI)
    }
}
