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

package tech.rollw.player.ui.setting.provider

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import tech.rollw.player.data.setting.CommonValues
import tech.rollw.player.data.setting.LocaleKey
import tech.rollw.player.data.setting.SettingApplyProvider
import tech.rollw.player.data.setting.SettingKey
import tech.rollw.player.data.setting.UISettings
import tech.rollw.support.appcompat.LocaleDelegate

/**
 * @author RollW
 */
object UISettingApplyProvider : SettingApplyProvider {
    override fun supports(settingKey: SettingKey<*, *>): Boolean {
        return Supports.any { it.key == settingKey }
    }

    override fun apply(
        context: Context,
        settingKey: SettingKey<*, *>,
        value: Any?
    ) {
        when (settingKey) {
            UISettings.NightMode.key -> {
                applyDarkMode(value)
            }
            UISettings.Language.key -> {
                applyLanguage(value)
            }
        }
    }

    private fun applyDarkMode(
        value: Any?
    ) {
        val castedValue = value as? String ?: return
        val mode = when (castedValue) {
            CommonValues.ON -> AppCompatDelegate.MODE_NIGHT_YES
            CommonValues.OFF -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun applyLanguage(
        value: Any?
    ) {
        val castedValue = value as? String ?: return
        val localeKey = LocaleKey.fromLocale(castedValue)
        val locale = localeKey.asLocale()
        LocaleDelegate.defaultLocale = locale
    }

    private val Supports = listOf(
        UISettings.NightMode,
        UISettings.Language
    )
}