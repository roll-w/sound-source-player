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

package tech.rollw.player.service

import android.content.Context

/**
 * Worker specification.
 *
 * @author RollW
 */
interface WorkerSpec {
    val tag: String

    val clazzName: String

    fun getName(context: Context): String

    fun isAnyOf(tags: Collection<String>): Boolean = tag in tags || clazzName in tags
}
