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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // cache all settings
            dataStore.data.first()
        }
    }

    operator fun <T> get(key: Preferences.Key<T>): T? =
        getValue(key, null)


    operator fun <T> get(settingKey: SettingKey<T, *>): Flow<T?> =
        dataStore.data.map {
            it[settingKey.asPreferencesKey()] as T?
        }

    operator fun <T> get(settingSpec: SettingSpec<T, *>): Flow<T?> =
        get(settingSpec.key)

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

    private inline fun <reified T> setValue(
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

        notifyListeners(key.asSettingKey(), values)
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

    fun <T, V> getValue(spec: SettingSpec<T, V>): T? {
        return getValue(
            spec.key,
            spec.defaultValue
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, V> getValue(key: SettingKey<T, V>, defaultValue: T?): T? {
        val valueFlow = dataStore.data.map {
            it[key.asPreferencesKey()]
        }
        val res =
            runBlocking { valueFlow.first() } ?: defaultValue
        return res as T?
    }

    fun <T, V> setValue(spec: SettingSpec<T, V>, value: T?) = setValue(spec.key, value)

    fun <T, V> setValue(key: SettingKey<T, V>, value: T?) {
        notifyListeners(key, value)

        val pKey = key.asPreferencesKey()
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit {
                if (value == null) {
                    it.remove(pKey)
                    return@edit
                }

                @Suppress("UNCHECKED_CAST")
                when (key.type) {
                    SettingType.STRING -> it[pKey as Preferences.Key<String>] = value as String
                    SettingType.INT -> it[pKey as Preferences.Key<Int>] = value as Int
                    SettingType.LONG -> it[pKey as Preferences.Key<Long>] = value as Long
                    SettingType.FLOAT -> it[pKey as Preferences.Key<Float>] = value as Float
                    SettingType.DOUBLE -> it[pKey as Preferences.Key<Double>] = value as Double
                    SettingType.BOOLEAN -> it[pKey as Preferences.Key<Boolean>] = value as Boolean
                }
            }
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

    fun getAll(): Map<SettingKey<*, *>, Any?> {
        val map = dataStore.data.map {
            it.asMap()
        }
        return runBlocking {
            map.first().mapKeys { (key, value) ->
                key.toSettingKey(SettingType.of(value))
            }
        }
    }

    private val listeners = mutableSetOf<OnPreferenceChangeListener>()

    interface OnPreferenceChangeListener {
        fun onPreferenceChange(key: SettingKey<*, *>, value: Any?)
    }

    fun addOnPreferenceChangeListener(listener: OnPreferenceChangeListener) {
        listeners.add(listener)
    }

    fun removeOnPreferenceChangeListener(listener: OnPreferenceChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(
        key: SettingKey<*, *>,
        value: Any?
    ) {
        listeners.forEach {
            it.onPreferenceChange(key, value)
        }
    }
}
