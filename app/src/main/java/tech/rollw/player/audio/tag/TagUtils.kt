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

import android.content.Context
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.AudioFormatType
import tech.rollw.player.audio.AudioPath
import tech.rollw.support.appcompat.openFileDescriptor
import tech.rollw.support.io.ContentPath

/**
 * @author RollW
 */
object TagUtils {
    fun AudioPath.openTag(
        context: Context,
        readonly: Boolean = false
    ): AudioTag = path.openTag(context, readonly)

    fun AudioContent.openTag(
        context: Context,
        readonly: Boolean = false
    ): AudioTag {
        val pfd = path.toUri().openFileDescriptor(context, "rw")
        val fd = pfd.detachFd()
        return NativeLibAudioTag(fd, this.audio.type, readonly)
    }

    fun ContentPath.openTag(
        context: Context,
        readonly: Boolean = false
    ): AudioTag {
        val pfd = toUri().openFileDescriptor(context, "rw")
        val fd = pfd.detachFd()
        val formatType = AudioFormatType.fromExtension(extension)
        return NativeLibAudioTag(fd, formatType, readonly)
    }
}