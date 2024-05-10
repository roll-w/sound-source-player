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

package tech.rollw.player.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import tech.rollw.player.data.setting.SettingSpec
import tech.rollw.player.data.setting.SettingValue

/**
 * @author RollW
 */
internal class SettingState<T, V> (
    private val settingValue: SettingValue<T, V>,
    /**
     * May reduce recomposition by using [collectAsState].
     */
    private val valueState: State<T?>
) : MutableState<T?> {
    override var value: T?
        get() = valueState.value
        set(newValue) {
            settingValue.value = newValue
        }

    override fun component1(): T? = value

    override fun component2(): (T?) -> Unit = { value = it }
}

/**
 * Remember setting value from [SettingSpec].
 *
 * @see SettingValue
 */
@Composable
fun <T, V> rememberSetting(
    settingSpec: SettingSpec<T, V>,
    allowAnyValue: Boolean = settingSpec.allowAnyValue()
): MutableState<T?> {
    val context = LocalContext.current
    val settingValue = SettingValue(settingSpec, context, allowAnyValue)

    val valueState = settingValue.asFlow().collectAsState(
        initial = settingValue.value
    )

    return remember(valueState.value) {
        SettingState(settingValue, valueState)
    }
}
