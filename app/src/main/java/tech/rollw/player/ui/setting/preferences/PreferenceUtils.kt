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

package tech.rollw.player.ui.setting.preferences

import androidx.preference.ListPreference
import androidx.preference.Preference
import tech.rollw.player.data.setting.SettingSpec
import tech.rollw.player.data.setting.SettingType
import tech.rollw.player.data.setting.preferenceDataStore

/**
 * @author RollW
 */

object PreferenceUtils  {
    fun Preference.setup(
        settingSpec: SettingSpec<*, *>,
        defaultValue: Any? = settingSpec.defaultValue
    ): Preference {
        preferenceDataStore = context.preferenceDataStore
        key = settingSpec.keyName
        if (icon == null) {
            isIconSpaceReserved = false
        }
        setDefaultValue(defaultValue)
        return this
    }

    fun Preference.titleAndSummary(
        title: String,
        summary: String? = null,
        isSingleLineTitle: Boolean = true
    ): Preference {
        this.title = title
        this.summary = summary
        this.isSingleLineTitle = isSingleLineTitle
        if (icon == null) {
            isIconSpaceReserved = false
        }
        return this
    }

    fun Preference.titleAndSummary(
        title: Int,
        summary: Int = 0,
        isSingleLineTitle: Boolean = true
    ): Preference {
        setTitle(title)
        if (summary != 0) {
            setSummary(summary)
        }

        this.isSingleLineTitle = isSingleLineTitle
        if (icon == null) {
            isIconSpaceReserved = false
        }
        return this
    }

    fun ListPreference.setupList(
        settingSpec: SettingSpec<*, *>,
        defaultValue: Any? = settingSpec.defaultValue,
        entryProvider: (String?) -> String? = { it }
    ): ListPreference {
        setup(settingSpec, defaultValue)
        if (settingSpec.type == SettingType.STRING ||
            settingSpec.type == SettingType.STRING_SET
        ) {
            val entryValues = settingSpec.valueEntries.map {
                it.toString()
            }.toTypedArray()
            this.entryValues = entryValues
            entries = entryValues.map(entryProvider).toTypedArray()
        }
        return this
    }
}
