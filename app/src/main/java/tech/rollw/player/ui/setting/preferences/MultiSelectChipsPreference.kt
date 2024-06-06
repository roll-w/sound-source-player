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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme

fun <T> PreferenceScreenScope.multiSelectChipsPreference(
    state: MutableState<Set<T>>,
    values: List<T>,
    title: @Composable (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    allowNoSelection: Boolean = true,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = ChipsPreferenceDefaults.DefaultShape,
    icon: @Composable ((Set<T>) -> Unit)? = null,
    summary: @Composable ((Set<T>) -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: @Composable () -> FlowRowOverflow = { FlowRowOverflow.Clip },
    onValueChange: (Set<T>) -> Unit = {}
) {
    item(key = key, contentType = "MultiSelectChipsPreference") {
        val contentTypography = LocalContentTypography.current
        val value by state
        MultiSelectChipsPreference(
            state = state,
            values = values,
            title = { title(value) },
            modifier = modifier,
            allowNoSelection = allowNoSelection,
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
            overflow = overflow(),
            onValueChange = onValueChange
        )
    }
}

@Composable
fun <T> MultiSelectChipsPreference(
    state: MutableState<Set<T>>,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    allowNoSelection: Boolean = true,
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
    overflow: FlowRowOverflow = FlowRowOverflow.Clip,
    onValueChange: (Set<T>) -> Unit = {}
) {
    var value by state

    MultiSelectChipsPreference(
        value = value,
        onValueChange = {
            value = it
            onValueChange(it)
        },
        values = values,
        title = title,
        modifier = modifier,
        allowNoSelection = allowNoSelection,
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

@Composable
fun <T> MultiSelectChipsPreference(
    value: Set<T>,
    onValueChange: (Set<T>) -> Unit,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    allowNoSelection: Boolean = true,
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
                overflow = overflow,
                horizontalArrangement = Arrangement.spacedBy(
                    ChipsPreferenceDefaults.HorizontalPadding
                )
            ) {
                values.forEach {
                    val selected by derivedStateOf {
                        it in value
                    }
                    ChipsPreferenceDefaults.Chip(
                        selected = selected,
                        enabled = if (enabled) optionEnabled(it) else false,
                        onSelected = onClick@{
                            if (!confirmValueChange(it)) {
                                return@onClick
                            }
                            val newValue = value.toMutableSet()
                            if (selected) {
                                newValue.remove(it)
                            } else {
                                newValue.add(it)
                            }
                            if (!allowNoSelection && newValue.isEmpty()) {
                                return@onClick
                            }
                            onValueChange(newValue)
                        },
                        label = valueToText(it),
                        contentTypography = contentTypography,
                        shape = shape
                    )
                }
            }
        }
    )
}