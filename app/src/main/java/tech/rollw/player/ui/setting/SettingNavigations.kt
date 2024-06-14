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

package tech.rollw.player.ui.setting

import tech.rollw.player.ui.setting.screen.debug.InternalFileExplorerScreen

typealias Route = String

/**
 * @author RollW
 */
object SettingNavigations {
    const val ROUTE_MENU: Route = "menu"
    const val ROUTE_UI: Route = "ui"
    const val ROUTE_DEBUG: Route = "debug"
    const val ROUTE_STORAGE: Route = "storage"

    const val ROUTE_AUDIO_DEVICES: Route = "debug/audio_devices"
    const val ROUTE_BACKGROUND_TASKS: Route = "debug/background_tasks"
    const val ROUTE_THEME_VIEWER: Route = "debug/theme_viewer"

    private const val PREFIX_ROUTE_INTERNAL_FILE_EXPLORER = "debug/internal_file_explorer"

    const val ROUTE_INTERNAL_FILE_EXPLORER =
        "${PREFIX_ROUTE_INTERNAL_FILE_EXPLORER}?${InternalFileExplorerScreen.EXTRA_PATH}={path}"

    const val KEY_SETTING_ITEM = "setting"

    private const val SETTING_PARAM = "${KEY_SETTING_ITEM}={setting}"

    fun buildRoute(route: Route, setting: String? = null): String {
        if (setting == null) {
            return ROUTE_MENU
        }
        return "$ROUTE_MENU?${SETTING_PARAM}"
    }

    fun buildInternalFileExplorerRoute(path: String? = null): String {
        if (path == null) {
            return PREFIX_ROUTE_INTERNAL_FILE_EXPLORER
        }
        return "${PREFIX_ROUTE_INTERNAL_FILE_EXPLORER}?" +
                "${InternalFileExplorerScreen.EXTRA_PATH}=$path"
    }
}
