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

import tech.rollw.player.audio.AudioFormatType
import java.io.IOException

/**
 * @author RollW
 */
class NativeLibAudioTag(
    private val fileDescriptor: Int,
    override val audioFormatType: AudioFormatType,
    val readonly: Boolean = false
) : AudioTag {
    /**
     * Native reference to the tag.
     */
    private val accessorRef: Long = openFileCheck(fileDescriptor, readonly)
    private var closed = false
    private lateinit var audioProperties: AudioProperties

    override fun getTagField(field: AudioTagField): String? {
        return getTagField(accessorRef, field.value)
    }

    override fun getArtwork(): ByteArray? {
        return getArtwork(accessorRef)
    }

    override fun setTagField(field: AudioTagField, value: String?) {
        if (value == null) {
            return deleteTagField(accessorRef, field.value)
        }
        return setTagField(accessorRef, field.value, value)
    }

    override fun setArtwork(artwork: ByteArray?) {
        if (artwork == null) {
            return deleteTagField(accessorRef, KEY_ARTWORK)
        }
        return setArtwork(accessorRef, artwork)
    }

    override fun getAudioProperties(): AudioProperties {
        if (!this::audioProperties.isInitialized) {
            audioProperties = getAudioProperties(accessorRef)
        }
        return audioProperties
    }

    override fun getLastModified(): Long {
       return lastModified(accessorRef)
    }

    override fun save() {
        return saveFile(accessorRef)
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        return closeFile(accessorRef)
    }

    private fun openFileCheck(fileDescriptor: Int, readonly: Boolean): Long {
        val fileRef = openFile(fileDescriptor, readonly)
        if (fileRef == 0L) {
            throw IOException("Cannot open file.")
        }
        return fileRef
    }

    private external fun openFile(accessorRef: Int, readonly: Boolean): Long

    private external fun closeFile(accessorRef: Long)

    private external fun getTagField(accessorRef: Long, tagField: String): String?

    private external fun getArtwork(accessorRef: Long): ByteArray?

    private external fun setTagField(accessorRef: Long, tagField: String, value: String)

    private external fun deleteTagField(accessorRef: Long, tagField: String)

    private external fun setArtwork(accessorRef: Long, artwork: ByteArray)

    private external fun saveFile(accessorRef: Long)

    private external fun getAudioProperties(accessorRef: Long) : AudioProperties

    private external fun lastModified(accessorRef: Long) : Long

    companion object {
        init {
            System.loadLibrary("soundsource")
        }

        private const val KEY_ARTWORK = "PICTURE"
    }
}