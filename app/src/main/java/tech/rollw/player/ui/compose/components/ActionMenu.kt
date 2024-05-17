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

package tech.rollw.player.ui.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.rollw.player.ui.PlayerTheme

/**
 * @author RollW
 */
object ActionMenuDefaults {
    val Elevation: Dp = 3.0.dp
    val containerColor: Color @Composable get() = MaterialTheme.colorScheme.surface

    @Composable
    fun colors(): ActionItemColors {
        return ActionItemColors(
            normalIconColor = MaterialTheme.colorScheme.onSurface,
            normalTextColor = MaterialTheme.colorScheme.onSurface,
            normalIndicatorColor = MaterialTheme.colorScheme.primary,
            disabledIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

private const val ItemAnimationDurationMillis: Int = 100

private val ActionMenuHeight: Dp = 60.0.dp

internal val ActionMenuItemPadding: Dp = 6.dp

@Composable
fun ActionMenu(
    modifier: Modifier = Modifier,
    containerColor: Color = ActionMenuDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = ActionMenuDefaults.Elevation,
    header: @Composable () -> Unit = {},
    content: LazyGridScope.() -> Unit
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        modifier = modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = ActionMenuHeight),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            header()

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                content = {
                    content()
                },
                contentPadding = PaddingValues(
                    all = ActionMenuItemPadding
                )
            )
        }
    }
}

@Composable
fun ActionMenuItem(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: ActionItemColors = ActionMenuDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val styledIcon = @Composable {
        val iconColor by colors.iconColor(enabled = enabled)
        // If there's a label, don't have a11y services repeat the icon description.
        val clearSemantics = label != null && alwaysShowLabel
        Box(modifier = if (clearSemantics) Modifier.clearAndSetSemantics {} else Modifier) {
            CompositionLocalProvider(
                LocalContentColor provides iconColor,
                content = icon
            )
        }
    }

    val styledLabel: @Composable (() -> Unit)? = label?.let {
        @Composable {
            val style = PlayerTheme.typography.contentNormal.info
            val textColor by colors.textColor(enabled = enabled)
            val mergedStyle = LocalTextStyle.current.merge(style)
            CompositionLocalProvider(
                LocalContentColor provides textColor,
                LocalTextStyle provides mergedStyle,
                content = label
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true,
    ) {
        ActionItemLayout(
            onClick = onClick,
            icon = styledIcon,
            label = styledLabel,
            enabled = enabled,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun ActionItemLayout(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val clickableModifier =
        Modifier.clickable(
            enabled = enabled,
            onClick = onClick,
            interactionSource = interactionSource,
            indication = LocalIndication.current
        )

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .then(clickableModifier)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            label?.invoke()
        }
    }
}

@Stable
data class ActionItemColors(
    val normalIconColor: Color,
    val normalTextColor: Color,
    val normalIndicatorColor: Color,
    val disabledIconColor: Color,
    val disabledTextColor: Color,
) {
    /**
     * Represents the icon color for this item, depending on whether it is [enabled].
     *
     * @param enabled whether the item is enabled
     */
    @Composable
    internal fun iconColor(enabled: Boolean): State<Color> {
        val targetValue = when {
            !enabled -> disabledIconColor
            else -> normalIconColor
        }
        return animateColorAsState(
            targetValue = targetValue,
            animationSpec = tween(ItemAnimationDurationMillis)
        )
    }

    /**
     * Represents the text color for this item, depending on whether it is [enabled].
     *
     * @param enabled whether the item is enabled
     */
    @Composable
    internal fun textColor(enabled: Boolean): State<Color> {
        val targetValue = when {
            !enabled -> disabledTextColor
            else -> normalTextColor
        }
        return animateColorAsState(
            targetValue = targetValue,
            animationSpec = tween(ItemAnimationDurationMillis)
        )
    }
}