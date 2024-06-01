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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import tech.rollw.compose.ui.text.FontUnit
import tech.rollw.compose.ui.text.FontUnit.Companion.lineHeight
import tech.rollw.compose.ui.text.TextStyle
import tech.rollw.compose.ui.text.copy

/**
 * @author RollW
 */
@Immutable
data class Typography(
    val contentLarge: ContentTypography = TypographyDefaults.ContentDefault,
    val contentMedium: ContentTypography = TypographyDefaults.ContentDefault,
    val contentNormal: ContentTypography = TypographyDefaults.ContentDefault,
    val contentSmall: ContentTypography = TypographyDefaults.ContentDefault,
    /**
     * The default font units for the typography.
     */
    val fontUnits: FontUnits = TypographyDefaults.FontUnits
)

enum class TypographyTokens {
    Header,
    Title,
    Subtitle,
    Body,
    Info,
    Tag,
    Tip,
    Code
}

/**
 * Represents the typography for the content in the app.
 *
 * A basic content card is like:
 *
 * ```
 * --------
 * [header]
 * --------
 *
 * [title] [tag]
 * [subtitle]
 *
 * [body]/[code]
 *
 * [info]
 *
 * [tip]
 * --------
 * ```
 */
@Immutable
data class ContentTypography(
    val header: TextStyle = TypographyDefaults.Header,
    val title: TextStyle = TypographyDefaults.Title,
    val subtitle: TextStyle = TypographyDefaults.Subtitle,
    val body: TextStyle = TypographyDefaults.Body,
    val info: TextStyle = TypographyDefaults.Info,
    val tag: TextStyle = TypographyDefaults.Tag,
    val tip: TextStyle = TypographyDefaults.Tip,
    val code: TextStyle = TypographyDefaults.Code,
    val fontUnits: FontUnits = TypographyDefaults.FontUnits
) {
    /**
     * Apply the given [fontUnit] to all text styles.
     *
     * Default values are:
     * - [header] = [FontUnitTokens.Huge]
     * - [title] = [FontUnitTokens.ExtraLarge]
     * - [subtitle] = [FontUnitTokens.Large]
     * - [body] = [FontUnitTokens.Normal]
     * - [info] = [FontUnitTokens.Small]
     * - [tag] = [FontUnitTokens.Normal]
     * - [tip] = [FontUnitTokens.Normal]
     * - [code] = [FontUnitTokens.Normal]
     */
    fun applyFontUnit(fontUnit: FontUnits): ContentTypography = copy(
        header = header.copy(fontUnit = fontUnit[FontUnitTokens.Huge]),
        title = title.copy(fontUnit = fontUnit[FontUnitTokens.ExtraLarge]),
        subtitle = subtitle.copy(fontUnit = fontUnit[FontUnitTokens.Large]),
        body = body.copy(fontUnit = fontUnit[FontUnitTokens.Normal]),
        info = info.copy(fontUnit = fontUnit[FontUnitTokens.Small]),
        tag = tag.copy(fontUnit = fontUnit[FontUnitTokens.Normal]),
        tip = tip.copy(fontUnit = fontUnit[FontUnitTokens.Normal]),
        code = code.copy(fontUnit = fontUnit[FontUnitTokens.Normal]),
        fontUnits = fontUnit
    )

    operator fun get(token: TypographyTokens): TextStyle = when (token) {
        TypographyTokens.Header -> header
        TypographyTokens.Title -> title
        TypographyTokens.Subtitle -> subtitle
        TypographyTokens.Body -> body
        TypographyTokens.Info -> info
        TypographyTokens.Tag -> tag
        TypographyTokens.Tip -> tip
        TypographyTokens.Code -> code
    }

    companion object {
        fun createFromBaselineStyle(
            textStyle: TextStyle, fontUnits: FontUnits,
            mapping: Map<TypographyTokens, FontUnitTokens> = Mapping
        ): ContentTypography {

            fun TextStyle.copy(fontUnit: FontUnitTokens): TextStyle = copy(
                fontUnit = fontUnits[fontUnit]
            )

            fun getOrDefault(
                mapping: Map<TypographyTokens, FontUnitTokens>,
                token: TypographyTokens
            ): FontUnitTokens = mapping[token] ?: Mapping.getValue(token)

            return ContentTypography(
                header = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Header)),
                title = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Title)),
                subtitle = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Subtitle)),
                body = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Body)),
                info = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Info)),
                tag = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Tag)),
                tip = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Tip)),
                code = textStyle.copy(fontUnit = getOrDefault(mapping, TypographyTokens.Code)),
                fontUnits = fontUnits
            )
        }

        val Mapping = mapOf(
            TypographyTokens.Header to FontUnitTokens.Huge,
            TypographyTokens.Title to FontUnitTokens.ExtraLarge,
            TypographyTokens.Subtitle to FontUnitTokens.Large,
            TypographyTokens.Body to FontUnitTokens.Normal,
            TypographyTokens.Info to FontUnitTokens.Small,
            TypographyTokens.Tag to FontUnitTokens.Normal,
            TypographyTokens.Tip to FontUnitTokens.Normal,
            TypographyTokens.Code to FontUnitTokens.Normal
        )
    }
}

data class FontUnits(
    val tiny: FontUnit = 10.sp lineHeight 14.sp,
    val small: FontUnit = 12.sp lineHeight 16.sp,
    val normal: FontUnit = 14.sp lineHeight 18.sp,
    val large: FontUnit = 16.sp lineHeight 24.sp,
    val extraLarge: FontUnit = 18.sp lineHeight 28.sp,
    val extremeLarge: FontUnit = 20.sp lineHeight 32.sp,
    val huge: FontUnit = 24.sp lineHeight 36.sp,
    val extraHuge: FontUnit = 28.sp lineHeight 40.sp
) {
    operator fun get(unit: FontUnitTokens): FontUnit = when (unit) {
        FontUnitTokens.Tiny -> tiny
        FontUnitTokens.Small -> small
        FontUnitTokens.Normal -> normal
        FontUnitTokens.Large -> large
        FontUnitTokens.ExtraLarge -> extraLarge
        FontUnitTokens.ExtremeLarge -> extremeLarge
        FontUnitTokens.Huge -> huge
        FontUnitTokens.ExtraHuge -> extraHuge
    }
}


enum class FontUnitTokens {
    Tiny,
    Small,
    Normal,
    Large,
    ExtraLarge,
    ExtremeLarge,
    Huge,
    ExtraHuge;
}

internal object TypographyDefaults {
    val FontUnits = FontUnits()

    val Header = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.huge,
        letterSpacing = 0.5.sp
    )
    val Title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.extraLarge,
        letterSpacing = 0.5.sp
    )
    val Subtitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.large,
        letterSpacing = 0.sp
    )
    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.normal,
        letterSpacing = 0.sp
    )
    val Info = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.small,
        letterSpacing = 0.5.sp
    )
    val Tip = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.normal,
        letterSpacing = 0.5.sp
    )
    val Tag = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.normal,
        letterSpacing = 0.5.sp
    )
    val Code = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontUnit = FontUnits.normal,
        letterSpacing = 0.sp
    )

    val ContentDefault = ContentTypography(
        header = Header,
        title = Title,
        subtitle = Subtitle,
        body = Body,
        info = Info,
        tag = Tag,
        tip = Tip,
        code = Code
    )
}

internal val LocalTypography = staticCompositionLocalOf { Typography() }

val LocalContentTypography = staticCompositionLocalOf {
    TypographyDefaults.ContentDefault
}

@Composable
fun ProvideContentTypography(
    typography: ContentTypography,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentTypography provides typography,
        content = content
    )
}
