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

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import tech.rollw.player.data.storage.LocalImageLoader
import tech.rollw.support.io.ContentPath

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

/**
 * Convert [Audio] to [MediaItem].
 */
fun Audio.toMediaItem(
    contentPath: ContentPath,
    artwork: ByteArray? = null
) = MediaItem.Builder()
    .setMediaId("audio=$id")
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
            })
            .build()
    )
    .build()

fun AudioContent.toMediaItem(
    artwork: ByteArray? = null
) = audio.toMediaItem(path, artwork)

@Throws(IllegalArgumentException::class)
fun List<AudioContent>.toMediaItems(
    localImageLoader: LocalImageLoader? = null
) = map {
    it.toMediaItem(
        localImageLoader?.load(it.path)
    )
}
