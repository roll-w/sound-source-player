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

package tech.rollw.player.data.setting

import android.content.Context
import okhttp3.internal.toImmutableList

/**
 * @author RollW
 */
class SettingApplyProviderDelegate(
    private val context: Context
) : SettingApplyProviderRegistry,
    PlayerPreferenceDataStore.OnPreferenceChangeListener {

    fun init(dataStore: PlayerPreferenceDataStore) {
        dataStore.addOnPreferenceChangeListener(this)
    }

    private val providers = mutableSetOf<SettingApplyProvider>()

    override fun register(provider: SettingApplyProvider) {
        providers.add(provider)
    }

    override fun unregister(provider: SettingApplyProvider) {
        providers.remove(provider)
    }

    override fun getProviders(settingKey: SettingKey<*, *>): List<SettingApplyProvider> =
        providers.filter { it.supports(settingKey) }
            .toImmutableList()

    override fun onPreferenceChange(key: SettingKey<*, *>, value: Any?) =
        getProviders(key).forEach {
            it.apply(context, key, value)
        }
}