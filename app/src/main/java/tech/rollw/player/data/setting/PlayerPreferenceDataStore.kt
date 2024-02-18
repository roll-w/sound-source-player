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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Player preference data store implementation based
 * on [DataStore].
 *
 * All set* methods are asynchronous and get* methods
 * are synchronous.
 *
 * @author RollW
 */
class PlayerPreferenceDataStore(
    private val dataStore: DataStore<Preferences>
) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        setValue(stringPreferencesKey(key), value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        setValue(stringSetPreferencesKey(key), values)
    }

    override fun putInt(key: String, value: Int) {
        setValue(intPreferencesKey(key), value)
    }

    override fun putLong(key: String, value: Long) {
        setValue(longPreferencesKey(key), value)
    }

    override fun putFloat(key: String, value: Float) {
        setValue(floatPreferencesKey(key), value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        setValue(booleanPreferencesKey(key), value)
    }

    private fun <T> setValue(
        key: Preferences.Key<T>,
        values: T?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit {
                if (values == null) {
                    it.remove(key)
                    return@edit
                }
                it[key] = values
            }
        }
    }

    private fun <T> getValue(
        key: Preferences.Key<T>,
        defValue: T?
    ): T? {
        val flow = dataStore.data.map {
            it[key] ?: defValue
        }
        return runBlocking {
            flow.first()
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return getValue(stringPreferencesKey(key), defValue)
    }

    override fun getStringSet(
        key: String,
        defValues: MutableSet<String>?
    ): Set<String>? {
        return getValue(stringSetPreferencesKey(key), defValues)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return getValue(intPreferencesKey(key), defValue)!!
    }

    override fun getLong(key: String, defValue: Long): Long {
        return getValue(longPreferencesKey(key), defValue)!!
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return getValue(floatPreferencesKey(key!!), defValue)!!
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return getValue(booleanPreferencesKey(key!!), defValue)!!
    }

    private fun getAll() {
        val map = dataStore.data.map {
            it.asMap()
        }
        return runBlocking { map.first() }
    }
}
