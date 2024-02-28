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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * @author RollW
 */
// https://stackoverflow.com/questions/67801939/get-previous-value-of-state
/**
 * Returns a dummy MutableState that does not cause render when setting it
 */
@Composable
fun <T> rememberRef(): MutableState<T?> {
    // for some reason it always recreated the value with vararg keys,
    // leaving out the keys as a parameter for remembering for now
    return remember {
        object: MutableState<T?> {
            override var value: T? = null

            override fun component1(): T? = value

            override fun component2(): (T?) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T> rememberPrevious(
    current: T,
    shouldUpdate: (prev: T?, curr: T) -> Boolean = { a: T?, b: T -> a != b },
): T? {
    val ref = rememberRef<T>()

    // launched after render, so the current render will have the old value anyway
    SideEffect {
        if (shouldUpdate(ref.value, current)) {
            ref.value = current
        }
    }

    return ref.value
}