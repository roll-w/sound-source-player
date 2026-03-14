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

package tech.rollw.player.service

import android.content.Context
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.data.setting.AppSettings
import tech.rollw.player.data.setting.SettingApplyProvider
import tech.rollw.player.data.setting.SettingKey
import tech.rollw.player.ui.applicationService

/**
 * @author RollW
 */
object PlaylistInitializer : SettingApplyProvider {
    override fun supports(settingKey: SettingKey<*, *>): Boolean {
        return SUPPORTS.contains(settingKey)
    }

    override fun apply(
        context: Context,
        settingKey: SettingKey<*, *>,
        value: Any?
    ) {
        val audioPlaylistProvider by context.applicationService<AudioPlaylistProvider>()


        when (settingKey) {
            AppSettings.LastPlayedList.key -> {
            }

            AppSettings.LastPlayedIndex.key -> {
            }

            AppSettings.LastPlayedPosition.key -> {
            }
        }
    }

    private fun getPlaylist(context: Context, id: Long?): Playlist {
        if (id == null) {
            return Playlist.EMPTY
        }

        if (id < 0) {
            //return Playlist.ofSystem(id, context)
        }

        return Playlist.EMPTY
    }

    private val SUPPORTS = listOf(
        AppSettings.LastPlayedList.key,
        AppSettings.LastPlayedIndex.key,
        AppSettings.LastPlayedPosition.key
    )
}