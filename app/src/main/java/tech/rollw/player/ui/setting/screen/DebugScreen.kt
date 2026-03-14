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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tech.rollw.compose.material3.RoundThumb
import tech.rollw.compose.material3.RoundTrack
import tech.rollw.player.R
import tech.rollw.player.data.database.PlayerDatabase
import tech.rollw.player.data.setting.DebugSettings
import tech.rollw.player.data.setting.SettingSpec
import tech.rollw.player.data.setting.SettingType
import tech.rollw.player.destroyApplicationService
import tech.rollw.player.getApplicationService
import tech.rollw.player.service.scanner.AudioClassificationWorker
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.setting.SettingNavigations
import tech.rollw.player.ui.setting.SettingPreferenceDefaults
import tech.rollw.player.ui.setting.preferences.PreferenceScreen
import tech.rollw.player.ui.setting.preferences.PreferenceScreenScope
import tech.rollw.player.ui.setting.preferences.checkboxPreference
import tech.rollw.player.ui.setting.preferences.chipsPreference
import tech.rollw.player.ui.setting.preferences.multiSelectChipsPreference
import tech.rollw.player.ui.setting.preferences.preference
import tech.rollw.player.ui.setting.preferences.preferenceCategory
import tech.rollw.player.ui.setting.preferences.radioListPreference
import tech.rollw.player.ui.setting.preferences.sliderPreference
import tech.rollw.player.ui.setting.preferences.switchPreference
import tech.rollw.player.ui.toast

/**
 * @author RollW
 */
@Composable
fun DebugScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val context = LocalContext.current

    val switchState = remember { mutableStateOf(false) }
    val chipsState = remember { mutableStateOf("1") }

    val multiSelectChipsState = remember { mutableStateOf(setOf("1")) }

    val radioListState = remember { mutableStateOf("1") }
    val sliderState = remember { mutableFloatStateOf(0.2f) }
    val sliderStateValue = remember { mutableFloatStateOf(0.2f) }

    PreferenceScreen(
        modifier = modifier,
        contentTypography = contentTypography
    ) {
        preferenceCategory(title = {
            Text(text = stringResource(R.string.setting_debug_category_debug_title))
        })

        preference(
            title = {},
            summary = {
                Text(text = stringResource(R.string.setting_debug_category_debug_summary))
            }
        )

        titleAndSummaryPreference(
            title = context.getString(R.string.setting_debug_database_title),
            summary = context.getString(R.string.setting_debug_database_summary)
        ) {
            context.apply {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.setting_debug_database_title)
                    .setMessage("Are you sure you want to reset your database?")
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        destroyApplicationService(PlayerDatabase::class.java)
                        PlayerDatabase.deleteDatabase(context)
                        getApplicationService(PlayerDatabase::class.java) {
                            PlayerDatabase.getDatabase(context)
                        }
                        toast("The database has been reset")
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }

        titleAndSummaryPreference(
            title = "Test Classify Audios",
            summary = "Test classify audios by their album, artist, and etc."
        ) {
            AudioClassificationWorker.submitWork(context)
        }

        titleAndSummaryPreference(
            title = "File Explorer",
            summary = "View the internal files of the app.",
        ) {
            navController.navigate(SettingNavigations.buildInternalFileExplorerRoute())
        }

        titleAndSummaryPreference(
            title = "Background Tasks",
            summary = "View the background tasks of the app."
        ) {
            navController.navigate(SettingNavigations.ROUTE_BACKGROUND_TASKS)
        }

        titleAndSummaryPreference(
            title = "Theme Viewer",
            summary = "View the theme of the app."
        ) {
            navController.navigate(SettingNavigations.ROUTE_THEME_VIEWER)
        }

        titleAndSummaryPreference(
            title = "Audio Devices",
            summary = "View all the audio devices connected to the device."
        ) {
            navController.navigate(SettingNavigations.ROUTE_AUDIO_DEVICES)
        }

        titleAndSummaryPreference(
            title = "Analytics Events",
            summary = "View all the analytics events of the app."
        ) {
            navController.navigate(SettingNavigations.ROUTE_ANALYTICS_EVENTS)
        }

        val flags = DebugSettings.specs.mapNotNull {
            if (it.key.type == SettingType.BOOLEAN) {
                it
            } else null
        }

        preferenceCategory(title = {
            Text(text = "Flags")
        })

        @Suppress("UNCHECKED_CAST")
        flags.forEach {
            debugFlagPreference(it as SettingSpec<Boolean, Boolean>)
        }

        preferenceCategory(title = {
            Text(text = "Samples")
        })

        switchPreference(
            state = switchState,
            title = {
                Text("Switch")
            },
            summary = {
                Text("Switch summary, Current: ${if (it) "on" else "off"}")
            },
            shape = SettingPreferenceDefaults.Shape
        )

        preference(
            title = {
                Text(text = "Disabled Preference")
            },
            summary = {
                Text(text = "This preference is controlled by the switch above.")
            },
            enabled = switchState.value,
            shape = SettingPreferenceDefaults.Shape
        ) {
        }

        chipsPreference(
            state = chipsState,
            values = listOf("1", "2", "3", "4", "5"),
            title = {
                Text("Chips")
            },
            enabled = switchState.value,
            summary = {
                Text("Chips summary: $it")
            },
            shape = SettingPreferenceDefaults.Shape,
            optionEnabled = {
                if (chipsState.value == "1") {
                    true
                } else it != "3"
            },
            confirmValueChange = {
                !(chipsState.value == "1" && it == "3")
            }
        )

        multiSelectChipsPreference(
            state = multiSelectChipsState,
            values = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "1000000", "11111"),
            allowNoSelection = false,
            title = {
                Text("Multi Select Chips")
            },
            summary = {
                Text("Multi Select Chips summary: $it")
            },
            shape = SettingPreferenceDefaults.Shape,
            optionEnabled = {
                it != "3"
            }
        )

        sliderPreference(
            state = sliderState,
            sliderState = sliderStateValue,
            title = {
                Text(text = "Slider")
            },
            summary = {
                Text("Slider Preference summary: ${sliderState.floatValue} ${sliderStateValue.floatValue}")
            },
            thumb = {
                SliderDefaults.RoundThumb(
                    interactionSource = remember { MutableInteractionSource() }
                )
            },
            track = {
                SliderDefaults.RoundTrack(sliderState = it)
            }
        )

        radioListPreference(
            state = radioListState,
            enabled = chipsState.value != "1",
            values = listOf("1", "2", "3", "4", "5"),
            title = {
                Text("Radio List No Overflow")
            },
            summary = {
                Text("Radio List summary: $it")
            },
            shape = SettingPreferenceDefaults.Shape,
            optionEnabled = {
                it != "3"
            }
        )

        radioListPreference(
            state = radioListState,
            values = listOf("1", "2", "3", "4", "5"),
            title = {
                Text("Radio List Overflow")
            },
            summary = {
                Text("Radio List summary: ${radioListState.value}")
            },
            maxLines = 2,
            maxItemsInEachColumn = 2,
            shape = SettingPreferenceDefaults.Shape
        )
    }
}

private fun PreferenceScreenScope.titleAndSummaryPreference(
    title: String,
    summary: String? = null,
    onClick: (() -> Unit)? = null
) {
    preference(
        title = {
            Text(text = title)
        },
        summary = summary?.let {
            {
                Text(it)
            }
        },
        onClick = onClick,
        shape = SettingPreferenceDefaults.Shape
    )
}

private fun PreferenceScreenScope.debugFlagPreference(
    settingSpec: SettingSpec<Boolean, Boolean>,
    summary: String? = null
) {
    checkboxPreference(
        settingSpec = settingSpec,
        title = {
            Text(settingSpec.keyName)
        },
        summary = summary?.let {
            { _ ->
                Text(it)
            }
        },
        shape = SettingPreferenceDefaults.Shape
    )
}