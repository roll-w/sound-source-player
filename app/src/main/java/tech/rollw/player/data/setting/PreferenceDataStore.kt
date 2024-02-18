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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * @author RollW
 */

/**
 * Setting [DataStore].
 */
val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private var pDataStore: PlayerPreferenceDataStore? = null

/**
 * Setting preference data store.
 */
val Context.preferenceDataStore: PlayerPreferenceDataStore
    get() {
        if (pDataStore == null) {
            pDataStore = PlayerPreferenceDataStore(settingDataStore)
        }
        return pDataStore!!
    }
