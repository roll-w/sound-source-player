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

package tech.rollw.player.data.storage

import android.content.Context
import android.util.Log
import tech.rollw.player.audio.AudioFormatType
import tech.rollw.player.audio.tag.NativeLibAudioTag
import tech.rollw.support.appcompat.openFileDescriptor
import tech.rollw.support.io.ContentPath

/**
 * Load image raw bytes from given [ContentPath].
 *
 * If the path is an audio file, it will try to
 * load the cover art from the audio file.
 *
 * @author RollW
 */
class LocalImageLoader(
    val context: Context,
) {
    private val listeners = mutableListOf<OnImageLoadListener>()

    fun load(contentPath: ContentPath): ByteArray? {
        val result = loadInternal(contentPath)
        if (result == null) {
            Log.w(TAG, "Failed to load image: $contentPath")
            listeners.forEach { it.onImageLoadFailed(contentPath) }
            return null
        }
        listeners.forEach { it.onImageLoadSuccess(contentPath, result) }
        return result
    }

    private fun loadInternal(contentPath: ContentPath): ByteArray? {
        val suffix = contentPath.getSuffix()
        val audioFormatType = AudioFormatType.fromExtensionOrNull(suffix)
        if (audioFormatType != null) {
            return ifAudioFile(contentPath, audioFormatType)
        }
        return ifImageFile(contentPath)
    }

    private fun ifAudioFile(
        contentPath: ContentPath,
        audioFormatType: AudioFormatType
    ): ByteArray? {
        val pfd = contentPath.toUri().openFileDescriptor(context)
        val fd = pfd.detachFd()

        val tag = NativeLibAudioTag(
            fileDescriptor = fd,
            audioFormatType = audioFormatType,
            readonly = true
        )
        Log.d(TAG, "Load artwork from audio file: $contentPath with fd=$fd")

        return tag.getArtwork()
    }

    private fun ifImageFile(contentPath: ContentPath): ByteArray? {
        val inputStream = context.contentResolver.openInputStream(contentPath.toUri())
        inputStream.use {
            return it?.readBytes()
        }
    }

    fun addOnImageLoadListener(listener: OnImageLoadListener) {
        listeners.add(listener)
    }

    fun removeOnImageLoadListener(listener: OnImageLoadListener) {
        listeners.remove(listener)
    }

    interface OnImageLoadListener {
        fun onImageLoadSuccess(contentPath: ContentPath, image: ByteArray)

        fun onImageLoadFailed(contentPath: ContentPath)
    }

    companion object {
        private const val TAG = "LocalImageLoader"
    }
}
