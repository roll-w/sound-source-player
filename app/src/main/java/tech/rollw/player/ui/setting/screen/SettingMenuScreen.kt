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

import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import tech.rollw.player.BuildConfig
import tech.rollw.player.R
import tech.rollw.player.data.setting.DebugSettings
import tech.rollw.player.ui.AboutActivity
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.player.SetupActivity
import tech.rollw.player.ui.setting.SettingNavigations
import tech.rollw.player.ui.setting.preferences.PreferenceScreen
import tech.rollw.player.ui.setting.preferences.preference
import tech.rollw.player.ui.setting.preferences.preferenceCategory
import tech.rollw.player.ui.tools.rememberSetting

/**
 * @author RollW
 */
@Composable
fun SettingMenuScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val context = LocalContext.current

    val debugEnabled by rememberSetting(settingSpec = DebugSettings.DebugEnabled)

    PreferenceScreen(
        modifier = modifier,
        contentTypography = contentTypography
    ) {
        preferenceCategory(
            title = {
                Text(text = stringResource(R.string.setting))
            }
        )

        preference(
            title = {
                Text(text = stringResource(R.string.setting_ui))
            },
            summary = {
                Text(text = "Provides UI customization options.")
            },
            shape = SettingPreferenceDefaults.Shape
        ) {
            navController.navigate(SettingNavigations.ROUTE_UI)
        }

        preference(
            title = {
                Text(text = stringResource(R.string.setting_storage))
            },
            summary = {
                Text(text = "Provides storage management options.")
            },
            shape = SettingPreferenceDefaults.Shape
        ) {
            navController.navigate(SettingNavigations.ROUTE_STORAGE)
        }

        preference(
            title = {
                Text(text = stringResource(R.string.media_store))
            },
            summary = {
                Text(text = stringResource(R.string.media_store))
            },
            shape = SettingPreferenceDefaults.Shape
        ) {
        }

        preference(
            title = {
                Text(text = "Setup")
            },
            summary = {
                Text(text = "Setup")
            },
            shape = SettingPreferenceDefaults.Shape
        ) {
            val intent = Intent(context, SetupActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }

        if (debugEnabled == true) {
            preference(
                title = {
                    Text(text = stringResource(R.string.setting_debug))
                },
                summary = {
                    Text(text = "Debug")
                },
                shape = SettingPreferenceDefaults.Shape
            ) {
                navController.navigate(SettingNavigations.ROUTE_DEBUG)
            }
        }

        preferenceCategory(
            title = {
                Text(text = "Other")
            }
        )

        preference(
            title = {
                Text(text = stringResource(R.string.about))
            },
            summary = {
                Text(text = BuildConfig.VERSION_NAME + " Type: " + BuildConfig.BUILD_TYPE)
            },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_colored),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            },
            shape = SettingPreferenceDefaults.Shape
        ) {
            ContextCompat.startActivity(
                context,
                Intent(
                    context,
                    AboutActivity::class.java
                ),
                null
            )
        }
    }
}
