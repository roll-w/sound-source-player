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

package tech.rollw.player.ui

import android.content.Context
import androidx.fragment.app.Fragment
import tech.rollw.player.getApplicationService

/**
 * @author RollW
 */

inline fun <reified T : Any> Fragment.applicationService(): Lazy<T> = lazy {
    requireContext().getApplicationService(T::class.java)
}

inline fun <reified T : Any> Context.applicationService(): Lazy<T> = lazy {
    getApplicationService(T::class.java)
}
