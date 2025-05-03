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
import androidx.navigation.NavController
import tech.rollw.player.R
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.setting.SettingPreferenceDefaults
import tech.rollw.player.ui.setting.preferences.PreferenceScreen
import tech.rollw.player.ui.setting.preferences.preference
import tech.rollw.player.ui.setting.preferences.preferenceCategory

/**
 * @author RollW
 */
@Composable
fun StorageScreen(
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

        preference(
            title = {
                Text(text = stringResource(R.string.setting_storage_clear_cache_title))
            },
            summary = {
                Text(text = stringResource(R.string.setting_storage_clear_cache_summary))
            },
            onClick = {

            },
            shape = SettingPreferenceDefaults.Shape
        )

        preference(
            title = {
                Text(text = stringResource(R.string.setting_storage_clear_data_title))
            },
            summary = {
                Text(text = stringResource(R.string.setting_storage_clear_data_summary))
            },
            onClick = {

            },
            shape = SettingPreferenceDefaults.Shape
        )

    }
}
