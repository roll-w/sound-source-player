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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

fun SettingKey<*, *>.asPreferencesKey(): Preferences.Key<*> = when (type) {
    SettingType.STRING -> stringPreferencesKey(key)
    SettingType.INT -> intPreferencesKey(key)
    SettingType.LONG -> longPreferencesKey(key)
    SettingType.FLOAT -> floatPreferencesKey(key)
    SettingType.DOUBLE -> doublePreferencesKey(key)
    SettingType.BOOLEAN -> booleanPreferencesKey(key)
    SettingType.STRING_SET -> stringSetPreferencesKey(key)
    else -> {
        throw IllegalArgumentException("Unsupported type: $type")
    }
}

inline fun <reified T> Preferences.Key<T>.asSettingKey() = when (T::class) {
    String::class -> SettingKey(name, SettingType.STRING)
    Int::class -> SettingKey(name, SettingType.INT)
    Long::class -> SettingKey(name, SettingType.LONG)
    Float::class -> SettingKey(name, SettingType.FLOAT)
    Double::class -> SettingKey(name, SettingType.DOUBLE)
    Boolean::class -> SettingKey(name, SettingType.BOOLEAN)
    Set::class -> SettingKey(name, SettingType.STRING_SET)
    else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
}

fun Preferences.Key<*>.toSettingKey(type: SettingType<*, *>): SettingKey<*, *> = when (type) {
    SettingType.STRING,
    SettingType.INT,
    SettingType.LONG,
    SettingType.FLOAT,
    SettingType.DOUBLE,
    SettingType.BOOLEAN,
    SettingType.STRING_SET -> SettingKey(name, type)

    else -> {
        throw IllegalArgumentException("Unsupported type: $type")
    }
}
