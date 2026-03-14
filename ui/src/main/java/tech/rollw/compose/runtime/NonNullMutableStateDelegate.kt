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
internal class NonNullMutableStateDelegate<T : Any>(
    private val mutableState: MutableState<T?>
) : MutableState<T> {
    override var value: T
        get() = mutableState.value
            ?: throw IllegalStateException(
                "You are trying to covert a nullable state to a non-null state, " +
                        "but the value is null."
            )
        set(newValue) {
            mutableState.value = newValue
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
}

/**
 * Convert a nullable state to a non-null state.
 */
fun <T : Any> MutableState<T?>.toNonNull(): MutableState<T> =
    NonNullMutableStateDelegate(this)