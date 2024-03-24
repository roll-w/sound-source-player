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

package tech.rollw.compose.ui.text

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.TextUnit

/**
 * Represents a pair of [TextUnit]s, one for text size
 * and the other for line height.
 *
 * Can be used to specify the text size and line height
 * of a text style.
 *
 * @author RollW
 */
@Immutable
data class FontUnit(
    val textSize: TextUnit,
    val lineHeight: TextUnit
) {
    companion object {
        val Unspecified: FontUnit
            get() = Default

        private val Default = FontUnit(
            textSize = TextUnit.Unspecified,
            lineHeight = TextUnit.Unspecified
        )

        infix fun TextUnit.lineHeight(lineHeight: TextUnit) = FontUnit(
            textSize = this,
            lineHeight = lineHeight
        )

        val TextStyle.fontUnit: FontUnit
            get() = FontUnit(
                textSize = fontSize,
                lineHeight = lineHeight
            )
    }
}

fun TextStyle(
    color: Color = Color.Unspecified,
    fontUnit: FontUnit = FontUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontSynthesis: FontSynthesis? = null,
    fontFamily: FontFamily? = null,
    fontFeatureSettings: String? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    baselineShift: BaselineShift? = null,
    textGeometricTransform: TextGeometricTransform? = null,
    localeList: LocaleList? = null,
    background: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    shadow: Shadow? = null,
    drawStyle: DrawStyle? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    textDirection: TextDirection = TextDirection.Unspecified,
    textIndent: TextIndent? = null,
    platformStyle: PlatformTextStyle? = null,
    lineHeightStyle: LineHeightStyle? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    hyphens: Hyphens = Hyphens.Unspecified,
    textMotion: TextMotion? = null
) = TextStyle(
    color = color,
    fontSize = fontUnit.textSize,
    fontWeight = fontWeight,
    fontStyle = fontStyle,
    fontSynthesis = fontSynthesis,
    fontFamily = fontFamily,
    fontFeatureSettings = fontFeatureSettings,
    letterSpacing = letterSpacing,
    baselineShift = baselineShift,
    textGeometricTransform = textGeometricTransform,
    localeList = localeList,
    background = background,
    textDecoration = textDecoration,
    shadow = shadow,
    drawStyle = drawStyle,
    textAlign = textAlign,
    textDirection = textDirection,
    textIndent = textIndent,
    platformStyle = platformStyle,
    lineHeight = fontUnit.lineHeight,
    lineHeightStyle = lineHeightStyle,
    lineBreak = lineBreak,
    hyphens = hyphens,
    textMotion = textMotion
)

