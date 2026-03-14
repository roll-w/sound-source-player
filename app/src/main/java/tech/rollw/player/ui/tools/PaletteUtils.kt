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

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import tech.rollw.support.colortheif.ColorThief

/**
 * @author RollW
 */
object PaletteUtils {
    suspend fun create(
        imageLoader: ImageLoader,
        imageRequest: ImageRequest,
    ): Palette? {
        val drawable = imageLoader.execute(imageRequest).drawable ?: return null
        val bitmap = drawable.toBitmapOrNull() ?: return null
        val memoryBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val palette = Palette.from(memoryBitmap)
            .maximumColorCount(24)
            .generate()
        return palette
    }

    suspend fun getPalettes(
        imageLoader: ImageLoader,
        imageRequest: ImageRequest,
        colors: Int = 16,
    ): List<Color>? {
        val drawable = imageLoader.execute(imageRequest).drawable ?: return null
        val bitmap = drawable.toBitmapOrNull() ?: return null
        val memoryBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        return ColorThief.getPalette(memoryBitmap, colors).map {
            Color(it[0], it[1], it[2])
        }
    }
}