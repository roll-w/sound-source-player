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

package tech.rollw.player.util

import android.content.Context
import tech.rollw.player.data.setting.PlayerPreferenceDataStore
import tech.rollw.player.data.setting.SettingApplyProvider
import tech.rollw.player.data.setting.SettingKey

/**
 * No outside changes will be received.
 * Only used to apply settings at the start of the app.
 *
 * @author RollW
 */
class SettingInitializer(
    private val dataStore: PlayerPreferenceDataStore,
    private val settingApplyProviders: List<SettingApplyProvider>
) : SettingApplyProvider {

    fun applyDefaults(
        context: Context
    ) {
        dataStore.getAll().forEach { (key, value) ->
            apply(context, key, value)
        }
    }

    override fun supports(settingKey: SettingKey<*, *>): Boolean {
        // should not receive any outside changes
        return false
    }

    override fun apply(
        context: Context,
        settingKey: SettingKey<*, *>,
        value: Any?
    ) {
        settingApplyProviders.firstOrNull { it.supports(settingKey) }
            ?.apply(context, settingKey, value)
    }
}