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

package tech.rollw.player.audio.tag

import tech.rollw.support.io.ImageFormatType

/**
 * @author RollW
 */
data class ByteArrayArtwork(
    override val format: ImageFormatType,
    override val data: ByteArray?,
    override val width: Int,
    override val height: Int,
    override val length: Long,
    override val description: String?,
    override val type: String?
) : Artwork {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ByteArrayArtwork) return false

        if (format != other.format) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (length != other.length) return false
        if (description != other.description) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + length.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }
}