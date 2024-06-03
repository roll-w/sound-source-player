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

package tech.rollw.player.ui.setting.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.rollw.compose.foundation.layout.copy
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme

fun PreferenceScreenScope.preference(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    endWidget: @Composable (() -> Unit)? = null,
    bottomWidget: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    item(key = key, contentType = "Preference") {
        Preference(
            title = title,
            modifier = modifier,
            enabled = enabled,
            padding = padding,
            contentTypography = LocalContentTypography.current,
            shape = shape,
            icon = icon,
            summary = summary,
            endWidget = endWidget,
            bottomWidget = bottomWidget,
            onClick = onClick
        )
    }
}

@Composable
fun Preference(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    endWidget: @Composable (() -> Unit)? = null,
    bottomWidget: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    BasicPreference(
        text = {
            Column(
                modifier = Modifier.padding(
                    padding.copy(
                        start = if (icon != null) 0.dp else Dp.Unspecified,
                        end = if (endWidget != null) 0.dp else Dp.Unspecified,
                        bottom = if (bottomWidget != null) 0.dp else Dp.Unspecified
                    )
                )
            ) {
                PreferenceDefaults.TitleContainer(
                    title = title,
                    enabled = enabled,
                    style = contentTypography.subtitle
                )
                PreferenceDefaults.SummaryContainer(
                    summary = summary,
                    enabled = enabled,
                    style = contentTypography.body
                )
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        icon = {
            PreferenceDefaults.IconContainer(
                icon = icon,
                enabled = enabled
            )
        },
        endWidget = {
            endWidget?.invoke()
        },
        bottomWidget = {
            if (bottomWidget != null) {
                Box(
                    modifier = Modifier.padding(
                        padding.copy(
                            top = padding.calculateTopPadding() / 2,
                            start = if (icon != null) 0.dp else Dp.Unspecified,
                        )
                    ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    bottomWidget.invoke()
                }
            }
        },
        onClick = onClick
    )
}

internal object PreferenceDefaults {
    val Padding = 16.dp

    val PaddingValues: PaddingValues = PaddingValues(Padding)

    const val DisableOpacity = 0.38f

    @Composable
    fun IconContainer(
        icon: @Composable (() -> Unit)?,
        enabled: Boolean,
        excludedEndPadding: Dp = 0.dp
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .widthIn(min = 56.dp - excludedEndPadding)
                    .padding(
                        start = 16.dp,
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides PlayerTheme.colorScheme.onSurfaceVariant.let {
                        if (enabled) it else it.copy(alpha = DisableOpacity)
                    },
                    content = icon
                )
            }
        }
    }

    @Composable
    fun TitleContainer(
        title: @Composable () -> Unit,
        enabled: Boolean,
        style: TextStyle = PlayerTheme.typography.contentMedium.subtitle
    ) {
        CompositionLocalProvider(LocalContentColor provides
                PlayerTheme.colorScheme.onSurface.let {
                    if (enabled) it else it.copy(alpha = DisableOpacity)
                }
        ) {
            ProvideTextStyle(
                value = style,
                content = title
            )
        }
    }

    @Composable
    fun SummaryContainer(
        summary: (@Composable () -> Unit)?,
        enabled: Boolean,
        style: TextStyle = PlayerTheme.typography.contentMedium.body
    ) {
        if (summary != null) {
            CompositionLocalProvider(LocalContentColor provides
                    PlayerTheme.colorScheme.onSurfaceVariant.let {
                        if (enabled) it else it.copy(alpha = DisableOpacity)
                    }
            ) {
                ProvideTextStyle(
                    value = style,
                    content = summary
                )
            }
        }
    }
}

