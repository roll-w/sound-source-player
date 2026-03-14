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

package tech.rollw.compose.runtime

import androidx.compose.runtime.MutableState

/**
 * @author RollW
 */
internal class NullableMutableStateDelegate<T : Any>(
    private val mutableState: MutableState<T>
) : MutableState<T?> {
    override var value: T?
        get() = mutableState.value
        set(newValue) {
            if (newValue != null) {
                mutableState.value = newValue
            }
        }

    override fun component1(): T? = value

    override fun component2(): (T?) -> Unit = { value = it }
}

/**
 * Convert a non-null state to a nullable state.
 *
 * Will ignore the assignment if the new value is null.
 */
fun <T : Any> MutableState<T>.toNullable(): MutableState<T?> =
    NullableMutableStateDelegate(this)
