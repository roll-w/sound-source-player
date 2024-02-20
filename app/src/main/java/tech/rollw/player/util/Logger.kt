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

package tech.rollw.player.util


/**
 * @author RollW
 */
interface Logger {
    fun debug(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null
    )

    fun error(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null
    )

    fun info(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null
    )

    fun warn(
        tag: String? = null,
        message: String,
        throwable: Throwable? = null
    )
}