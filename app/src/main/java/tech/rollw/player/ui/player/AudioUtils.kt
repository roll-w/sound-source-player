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

package tech.rollw.player.ui.player

import androidx.annotation.IntDef
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioContent
import java.text.Collator
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * @author RollW
 */
object AudioUtils {
    fun Long.formatDuration() = this.toDuration(DurationUnit.MILLISECONDS)
        .toComponents { minutes, seconds, _ ->
            "$minutes:${seconds.toString().padStart(2, '0')}"
        }

    fun Audio.formatDuration() = duration.formatDuration()

    private val StringComparator: Comparator<Any> = Collator.getInstance(Locale.US)

    private val TitleComparator: Comparator<Audio> = Comparator { o1, o2 ->
        StringComparator.compare(o1.title, o2.title)
    }

    private val ArtistComparator: Comparator<Audio> = Comparator { o1, o2 ->
        StringComparator.compare(o1.artist, o2.artist)
    }

    private val AlbumComparator: Comparator<Audio> = Comparator { o1, o2 ->
        StringComparator.compare(o1.album, o2.album)
    }

    private val LastModifiedComparator: Comparator<Audio> = Comparator { o1, o2 ->
        o1.lastModified.compareTo(o2.lastModified)
    }

    fun List<Audio>.sortBy(
        @AudioOrder order: Int,
        reverse: Boolean = false
    ): List<Audio> {
        return when (order) {
            AudioOrder.ORDER_TITLE ->
                sortedWith(
                    TitleComparator
                        .thenComparing(ArtistComparator)
                        .thenComparing(AlbumComparator)
                        .thenComparing(LastModifiedComparator)
                )

            AudioOrder.ORDER_ARTIST -> sortedWith(
                ArtistComparator
                    .thenComparing(TitleComparator)
                    .thenComparing(AlbumComparator)
                    .thenComparing(LastModifiedComparator)
            )

            AudioOrder.ORDER_ALBUM -> sortedWith(
                AlbumComparator
                    .thenComparing(TitleComparator)
                    .thenComparing(ArtistComparator)
                    .thenComparing(LastModifiedComparator)
            )

            AudioOrder.ORDER_DURATION -> sortedBy { it.duration }
            AudioOrder.ORDER_YEAR -> sortedBy { it.year }
            AudioOrder.ORDER_LAST_MODIFIED -> sortedWith(
                LastModifiedComparator
                    .thenComparing(TitleComparator)
                    .thenComparing(ArtistComparator)
                    .thenComparing(AlbumComparator)
            )

            else -> this
        }.let { if (reverse) it.reversed() else it }
    }

    @JvmName("sortByAudioContent")
    fun List<AudioContent>.sortBy(
        @AudioOrder order: Int,
        reverse: Boolean = false
    ): List<AudioContent> {
        // AudioContent is a wrapper class for Audio, so we can use the same logic
        val map = hashMapOf(
            *this.map { it.audio to it }.toTypedArray()
        )

        return map.keys.toList().sortBy(order, reverse).map { map[it]!! }
    }
}


@IntDef(
    AudioOrder.ORDER_TITLE,
    AudioOrder.ORDER_ARTIST,
    AudioOrder.ORDER_ALBUM,
    AudioOrder.ORDER_DURATION,
    AudioOrder.ORDER_YEAR,
    AudioOrder.ORDER_LAST_MODIFIED
)
annotation class AudioOrder{

    companion object {
        const val ORDER_TITLE = 0
        const val ORDER_ARTIST = 1
        const val ORDER_ALBUM = 2
        const val ORDER_DURATION = 3
        const val ORDER_YEAR = 4
        const val ORDER_LAST_MODIFIED = 5
    }
}
