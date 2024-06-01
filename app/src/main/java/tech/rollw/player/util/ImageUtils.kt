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

import android.graphics.Bitmap
import androidx.annotation.Keep


/**
 * @author RollW
 */
@Keep
object ImageUtils {
    init {
        System.loadLibrary("soundsource")
    }

    private external fun blurPixels(image: IntArray, w: Int, h: Int, radius: Int): IntArray

    /**
     * Blur bitmap with given radius.
     *
     * @param bitmap the bitmap to blur
     * @param radius the radius of the blur, range from 0 to 100
     * @param copy if false, the given bitmap will be modified directly
     */
    fun blur(
        bitmap: Bitmap, radius: Int = 25,
        copy: Boolean = true
    ): Bitmap {
        if (radius < 0 || radius > 100) {
            throw IllegalArgumentException("Radius must be in range [0, 100].")
        }
        if (radius == 0) {
            return bitmap
        }

        val copied = if (copy) {
            val cBitmap = bitmap.copy(
                bitmap.config,
                true
            )
            if (cBitmap == null) {
                throw IllegalArgumentException("Failed to copy bitmap.")
            }
            cBitmap
        } else {
            bitmap
        }

        blurBitmap(copied, radius)
        return copied
    }

    /**
     * Blur bitmap with the radius.
     *
     * Note: it will modify the given bitmap.
     */
    private external fun blurBitmap(bitmap: Bitmap, radius: Int)
}