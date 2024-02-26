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

package tech.rollw.player.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * @author RollW
 */
@Immutable
data class PlayerColors(
    // TODO: PlayerColors
    val primary: Color
)

val DefaultPlayerColor = PlayerColors(
    primary = Color(0xFFEE6B13),
)

val LocalPlayerColors = staticCompositionLocalOf {
    DefaultPlayerColor
}

// TODO: may change some of colors later
val primaryLight = Color(0xFFEE6B13)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFBEAE0)
val onPrimaryContainerLight = Color(0xFF340000)
val secondaryLight = Color(0xFF735853)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFFDFDA)
val onSecondaryContainerLight = Color(0xFF5D4440)
val tertiaryLight = Color(0xFF81515C)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFC9D4)
val onTertiaryContainerLight = Color(0xFF5F343F)
val errorLight = Color(0xFFA4020D)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFDA342E)
val onErrorContainerLight = Color(0xFFFFFFFF)
val backgroundLight = Color(0xFFFFF8F6)
val onBackgroundLight = Color(0xFF261816)
val surfaceLight = Color(0xFFFFF8F6)
val onSurfaceLight = Color(0xFF261816)
val surfaceVariantLight = Color(0xFFFFDAD4)
val onSurfaceVariantLight = Color(0xFF5A413D)
val outlineLight = Color(0xFF8D706C)
val outlineVariantLight = Color(0xFFE2BFB9)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF3C2D2A)
val inverseOnSurfaceLight = Color(0xFFFFEDEA)
val inversePrimaryLight = Color(0xFFFFB4A8)

val primaryDark = Color(0xFFFFB4A8)
val onPrimaryDark = Color(0xFF690000)
val primaryContainerDark = Color(0xFF451C16)
val onPrimaryContainerDark = Color(0xFFFFFFFF)
val secondaryDark = Color(0xFFFFFFFF)
val onSecondaryDark = Color(0xFF412B27)
val secondaryContainerDark = Color(0xFFF0CCC6)
val onSecondaryContainerDark = Color(0xFF523935)
val tertiaryDark = Color(0xFFFFF6F6)
val onTertiaryDark = Color(0xFF4C242F)
val tertiaryContainerDark = Color(0xFFFBBDCA)
val onTertiaryContainerDark = Color(0xFF582E39)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFFDA342E)
val onErrorContainerDark = Color(0xFFFFFFFF)
val backgroundDark = Color(0xFF1D100E)
val onBackgroundDark = Color(0xFFF7DDD9)
val surfaceDark = Color(0xFF1D100E)
val onSurfaceDark = Color(0xFFF7DDD9)
val surfaceVariantDark = Color(0xFF5A413D)
val onSurfaceVariantDark = Color(0xFFE2BFB9)
val outlineDark = Color(0xFFA98A85)
val outlineVariantDark = Color(0xFF5A413D)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFF7DDD9)
val inverseOnSurfaceDark = Color(0xFF3C2D2A)
val inversePrimaryDark = Color(0xFFB12C1E)
