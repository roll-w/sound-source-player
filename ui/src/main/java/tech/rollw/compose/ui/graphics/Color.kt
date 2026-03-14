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

package tech.rollw.compose.ui.graphics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

/**
 * @author RollW
 */
fun Color.convertHsv(
    hue: Float = -1F,
    saturation: Float = -1F,
    value: Float = -1F,
    alpha: Float = this.alpha,
    colorSpace: Rgb = ColorSpaces.Srgb
): Color {
    val argb = toArgb()
    val red = argb.red
    val green = argb.green
    val blue = argb.blue
    val (h, s, v) = rgbToHsv(
        red, green, blue
    )
    return Color.hsl(
        if (hue < 0) h else hue,
        if (saturation < 0) s else saturation,
        if (value < 0) v else value,
        alpha,
        colorSpace
    )
}


fun Color.hsv(): Triple<Float, Float, Float> {
    val argb = toArgb()

    val red = argb.red
    val green = argb.green
    val blue = argb.blue
    val (h, s, v) = rgbToHsv(
        red, green, blue
    )

    return Triple(h, s, v)
}


private fun rgbToHsv(
    red: Int,
    green: Int,
    blue: Int
): Triple<Float, Float, Float> {
    val out = FloatArray(3)

    android.graphics.Color.colorToHSV(
        android.graphics.Color.rgb(red, green, blue),
        out
    )
    return Triple(
        out[0],
        out[1],
        out[2]
    )
}