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

package tech.rollw.player.ui.setting.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import tech.rollw.player.R
import tech.rollw.player.data.setting.CommonValues
import tech.rollw.player.data.setting.LocaleKey
import tech.rollw.player.data.setting.UISettings
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.setting.SettingPreferenceDefaults
import tech.rollw.player.ui.setting.preferences.PreferenceScreen
import tech.rollw.player.ui.setting.preferences.chipsPreference
import tech.rollw.player.ui.setting.preferences.preferenceCategory
import tech.rollw.player.util.ContextUtils.getActivity

/**
 * @author RollW
 */
@Composable
fun UIScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val context = LocalContext.current

    PreferenceScreen(
        modifier = modifier,
        contentTypography = contentTypography
    ) {

        preferenceCategory(
            title = {
                Text(text = stringResource(R.string.setting_category_general_title))
            }
        )

        chipsPreference(
            settingSpec = UISettings.NightMode,
            title = { Text(stringResource(R.string.setting_ui_night_mode_title)) },
            valueToText = {
                when (it) {
                    CommonValues.AUTO -> context.getString(R.string.setting_ui_night_mode_system)
                    CommonValues.ON -> context.getString(R.string.setting_ui_night_mode_dark)
                    CommonValues.OFF -> context.getString(R.string.setting_ui_night_mode_light)
                    else -> "Invalid"
                }
            },
            shape = SettingPreferenceDefaults.Shape
        )

        chipsPreference(
            settingSpec = UISettings.Language,
            title = { Text(stringResource(id = R.string.setting_ui_language_title)) },
            valueToText = {
                val localeKey = LocaleKey.fromLocale(it)
                when (localeKey) {
                    LocaleKey.System -> context.getString(R.string.locale_system)
                    LocaleKey.English -> context.getString(R.string.locale_en)
                    LocaleKey.Chinese -> context.getString(R.string.locale_zh)
                    else -> context.getString(R.string.locale_system)
                }
            },
            shape = SettingPreferenceDefaults.Shape
        ) {
            context.getActivity()?.let {
                ActivityCompat.recreate(it)
            }
        }
    }
}
