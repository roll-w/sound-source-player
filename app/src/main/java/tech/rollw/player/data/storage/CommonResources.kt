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

package tech.rollw.player.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import tech.rollw.player.R

/**
 * Provides in-memory cache for common resources.
 *
 * @author RollW
 */
class CommonResources(
    private val context: Context
) {
    val logo: Bitmap by lazy {
        BitmapFactory.decodeResource(
            context.resources, R.mipmap.ic_logo
        )
    }
}