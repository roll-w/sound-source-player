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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme

/**
 * @author RollW
 */
fun <T> PreferenceScreenScope.chipsPreference(
    state: MutableState<T>,
    values: List<T>,
    title: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = ChipsPreferenceDefaults.DefaultShape,
    icon: @Composable ((T) -> Unit)? = null,
    summary: @Composable ((T) -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: @Composable () -> FlowRowOverflow = { FlowRowOverflow.Clip }
) {
    item(key = key, contentType = "ChipsPreference") {
        val contentTypography = LocalContentTypography.current
        val value by state
        ChipsPreference(
            state = state,
            values = values,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled,
            padding = padding,
            contentTypography = contentTypography,
            shape = shape,
            icon = icon?.let { { icon(value) } },
            summary = summary?.let { { summary(value) } },
            valueToText = valueToText,
            optionEnabled = optionEnabled,
            confirmValueChange = confirmValueChange,
            maxItemsInEachRow = maxItemsInEachRow,
            maxLines = maxLines,
            overflow = overflow()
        )
    }
}


@Composable
fun <T> ChipsPreference(
    state: MutableState<T>,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = ChipsPreferenceDefaults.DefaultShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: FlowRowOverflow = FlowRowOverflow.Clip
) {
    var value by state
    ChipsPreference(
        value = value,
        onValueChange = { value = it },
        values = values,
        title = title,
        modifier = modifier,
        enabled = enabled,
        padding = padding,
        contentTypography = contentTypography,
        shape = shape,
        icon = icon,
        summary = summary,
        valueToText = valueToText,
        optionEnabled = optionEnabled,
        confirmValueChange = confirmValueChange,
        maxItemsInEachRow = maxItemsInEachRow,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 *  @param values A list of possible values that this preference can hold.
 *  @param title A Composable function that defines the title of this preference.
 *  @param modifier The Modifier to be applied to this preference.
 *  @param shape The shape to be used for the chips in this preference.
 *  @param valueToText A function that converts the value of this preference to a String.
 *  @param optionEnabled A function that determines whether a particular option is enabled or not.
 *  @param confirmValueChange A function that confirms whether the value of this preference can be changed or not.
 *  @param maxItemsInEachRow The maximum number of items that can be displayed in each row of the FlowRow.
 *  @param maxLines The maximum number of lines that the FlowRow can have.
 *  @param overflow The overflow behavior of the FlowRow.
 */
@Composable
fun <T> ChipsPreference(
    value: T,
    onValueChange: (T) -> Unit,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = ChipsPreferenceDefaults.DefaultShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: FlowRowOverflow = FlowRowOverflow.Clip
) {
    Preference(
        title = title,
        modifier = modifier,
        enabled = enabled,
        padding = padding,
        contentTypography = contentTypography,
        icon = icon,
        summary = summary,
        bottomWidget = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = maxItemsInEachRow,
                maxLines = maxLines,
                overflow = overflow
            ) {
                values.forEach {
                    val selected by derivedStateOf {
                        value == it
                    }
                    ChipsPreferenceDefaults.Chip(
                        modifier = Modifier.padding(end = 2.dp, bottom = 1.dp),
                        selected = selected,
                        enabled = if (enabled) optionEnabled(it) else false,
                        onSelected = {
                            if (confirmValueChange(it)) {
                                onValueChange(it)
                            }
                        },
                        label = valueToText(it),
                        shape = shape,
                        contentTypography = contentTypography
                    )
                }
            }
        }
    )
}

internal object ChipsPreferenceDefaults {
    val DefaultShape = ShapeDefaults.Small

    @Composable
    fun Chip(
        selected: Boolean,
        onSelected: () -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        shape: Shape = DefaultShape
    ) {
        val alpha by animateFloatAsState(
            targetValue = if (enabled) 1f else PreferenceDefaults.DisabledOpacity
        )

        FilterChip(
            modifier = modifier,
            shape = shape,
            selected = selected,
            enabled = enabled,
            onClick = onSelected,
            label = {
                Text(
                    text = label,
                    style = contentTypography.body,
                )
            }
        )
    }
}


