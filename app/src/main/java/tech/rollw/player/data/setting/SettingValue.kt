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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author RollW
 */
class SettingValue<T, V>(
    private val spec: SettingSpec<T, V>,
    private val context: Context,
    /**
     * If true, the value can be any value, otherwise,
     * the value must be one of the allowed values in
     * [SettingSpec.valueEntries]
     */
    private val allowAnyValue: Boolean = false
) : ReadWriteProperty<Any?, T?> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T? {
        return context.preferenceDataStore.getValue(spec)
    }

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?
    ) {
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

}

fun Context.settingValue(
    spec: SettingSpec<*, *>,
    allowAnyValue: Boolean = false
): SettingValue<*, *> {
    return SettingValue(spec, this, allowAnyValue)
}