/*
 * Copyright (C) 2022 Lingu. All rights reserved.
 *
 *           https://lingu.space/
 *
 * Any form of use of the source code, pictures
 * and other resources of the project without
 * written consent shall not be allowed.
 */
package tech.rollw.player.ui.player

import android.content.Context
import androidx.annotation.StringRes
import tech.rollw.player.R
import tech.rollw.player.audio.list.PlaylistType

enum class AudioListType(
    val value: String,
    @StringRes val resId: Int = 0,
) {
    ALL_SONGS("all_songs", R.string.all_songs),
    ARTIST("artist", R.string.artists),
    ALBUM("album", R.string.albums),
    ALBUM_ARTIST("album_artist"),
    FOLDER("folder", R.string.folders),
    GENRE("genre"),
    QUEUE("queue"),
    PLAYLIST("playlist", R.string.playlists),
    RECENTLY_ADDED("recently_added", R.string.recently_added),
    LAST_PLAYED("last_played"),
    MOST_OFTEN_PLAYED("most_often");

    override fun toString(): String {
        return value
    }

    fun getName(context: Context): String {
        return if (resId != 0) {
            context.getString(resId)
        } else {
            value
        }
    }

    val isFullListType: Boolean
        get() = this == ALL_SONGS || (this == RECENTLY_ADDED) ||
                (this == LAST_PLAYED) || (this == MOST_OFTEN_PLAYED)

    fun toPlaylistType(): PlaylistType {
        return when (this) {
            PLAYLIST -> PlaylistType.PLAYLIST
            ARTIST -> PlaylistType.ARTIST
            ALBUM -> PlaylistType.ALBUM
            ALBUM_ARTIST -> PlaylistType.ALBUM_ARTIST
            FOLDER -> PlaylistType.FOLDER
            GENRE -> PlaylistType.GENRE
            else -> PlaylistType.OTHER
        }
    }

    companion object {
        fun fromParam(param: String?): AudioListType? {
            param ?: return null
            for (params in entries) {
                if (params.value.equals(param, ignoreCase = true)) {
                    return params
                }
            }
            return null
        }

        fun PlaylistType.toAudioListType(): AudioListType {
            return when (this) {
                PlaylistType.PLAYLIST -> PLAYLIST
                PlaylistType.ARTIST -> ARTIST
                PlaylistType.ALBUM -> ALBUM
                PlaylistType.ALBUM_ARTIST -> ALBUM_ARTIST
                PlaylistType.FOLDER -> FOLDER
                PlaylistType.GENRE -> GENRE
                else -> ALL_SONGS
            }
        }
    }
}
