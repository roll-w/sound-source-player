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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A delegate for setting value.
 *
 * @author RollW
 */
class SettingValue<T, V>(
    val spec: SettingSpec<T, V>,
    private val context: Context,

    /**
     * If true, the value can be any value, otherwise,
     * the value must be one of the allowed values in
     * [SettingSpec.valueEntries]
     */
    private val allowAnyValue: Boolean = spec.allowAnyValue()
) : ReadWriteProperty<Any?, T?> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T? {
        return getSettingValue()
    }

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?
    ) {
        setSettingValue(value)
    }

    var value: T?
        get() = getSettingValue()
        set(value) {
            setSettingValue(value)
        }

    private fun getSettingValue(): T? {
        return context.preferenceDataStore.getValue(spec)
    }

    private fun setSettingValue(value: T?) {
        checkValueIn(value)
        context.preferenceDataStore.setValue(spec, value)
    }

    private fun checkValueIn(value: T?) {
        if (allowAnyValue || value == null) {
            return
        }
        if (!spec.hasValueByType(value)) {
            throw IllegalArgumentException("Value $value is not allowed by ${spec.keyName}")
        }
    }

    private var _valueFlow: Flow<T?>? = null

    fun asFlow(): Flow<T?> {
        val preferenceDataStore = context.preferenceDataStore
        if (_valueFlow == null) {
            _valueFlow = preferenceDataStore[spec].distinctUntilChanged()
        }
        return _valueFlow!!
    }

    companion object {
        fun <T, V> SettingSpec<T, V>.value(
            context: Context,
            allowAnyValue: Boolean = this.allowAnyValue()
        ): SettingValue<T, V> {
            return SettingValue(this, context, allowAnyValue)
        }

        fun Context.settingValue(
            spec: SettingSpec<*, *>,
            allowAnyValue: Boolean = false
        ): SettingValue<*, *> {
            return SettingValue(spec, this, allowAnyValue)
        }
    }
}