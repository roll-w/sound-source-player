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

import android.net.Uri
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import tech.rollw.player.data.storage.LocalImageLoader
import tech.rollw.support.io.ContentPath
import tech.rollw.support.io.PathType

/**
 * @author RollW
 */

/**
 * Convert [Audio] to [MediaItem].
 */
fun Audio.toMediaItem(
    audioPath: AudioPath,
    artwork: ByteArray? = null
) = toMediaItem(audioPath.path, artwork)

private const val PREFIX_AUDIO = "audio="

/**
 * Convert [Audio] to [MediaItem].
 */
fun Audio.toMediaItem(
    contentPath: ContentPath,
    artwork: ByteArray? = null
) = MediaItem.Builder()
    .setMediaId("$PREFIX_AUDIO$id")
    .setUri(contentPath.toUri())
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setAlbumArtist(albumArtist)
            .setSubtitle(artist)
            .setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            .setExtras(Bundle().apply {
                putString("path", contentPath.path)
                putSerializable("type", contentPath.type)
                putSerializable("audioContent", AudioContent(this@toMediaItem, contentPath))
            })
            .build()
    )
    .build()

fun AudioContent.toMediaItem(
    artwork: ByteArray? = null
) = audio.toMediaItem(path, artwork)

fun MediaItem.toAudioContent(): AudioContent {
    val audioContent = mediaMetadata.extras?.let {
        BundleCompat.getSerializable(it, "audioContent", AudioContent::class.java)
    }
    if (audioContent != null) {
        return audioContent
    }

    val path: ContentPath = mediaMetadata.extras?.let {
        val path = it.getString("path")
        val type = BundleCompat.getSerializable(it, "type", PathType::class.java)
        ContentPath(path!!, type!!)
    } ?: buildPathFromUri(localConfiguration?.uri ?: Uri.EMPTY)

    val id = this.mediaId.substringAfter(PREFIX_AUDIO).toIntOrNull()
    val audio = Audio.EMPTY.copy(
        id = id?.toLong(),
        title = mediaMetadata.title.toString(),
        artist = mediaMetadata.artist.toString(),
        album = mediaMetadata.albumTitle.toString(),
        albumArtist = mediaMetadata.albumArtist.toString(),
    )

    return AudioContent(audio, path)
}

private fun buildPathFromUri(uri: Uri) =
    ContentPath(uri.toString(), PathType.URI)

@Throws(IllegalArgumentException::class)
fun List<AudioContent>.toMediaItems(
    localImageLoader: LocalImageLoader? = null
) = map {
    it.toMediaItem(
        localImageLoader?.load(it.path)
    )
}
