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

package tech.rollw.player.ui.player.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import tech.rollw.player.ui.components.TextIconItem

typealias SelectionKey = String

object SelectionKeys {
    const val ADD_PLAYLIST: SelectionKey = "add_to_playlist"
    const val PLAY_NEXT: SelectionKey = "play_next"
    const val DELETE: SelectionKey = "delete"
    const val SHARE: SelectionKey = "share"
    const val AUDIO_INFO: SelectionKey = "audio_info"
    const val AUDIO_COVER: SelectionKey = "audio_cover"
    const val LYRIC: SelectionKey = "lyric"
    const val PLAY: SelectionKey = "play"
    const val RENAME: SelectionKey = "rename"
    const val CLEAR_QUEUE: SelectionKey = "clear_queue"

    fun SelectionKey.createTextIconItem(
        text: String, @DrawableRes iconRes: Int,
        enabled: Boolean = true
    ): TextIconItem =
        TextIconItem.of(this, text, iconRes, enabled)

    fun SelectionKey.createTextIconItem(
        @StringRes textRes: Int,
        @DrawableRes iconRes: Int,
        enabled: Boolean = true
    ): TextIconItem =
        TextIconItem.of(this, textRes, iconRes, enabled)

    fun SelectionKey.createTextIconItem(
        text: String, icon: ImageVector,
        enabled: Boolean = true
    ): TextIconItem =
        TextIconItem.of(this, text, icon, enabled)
}