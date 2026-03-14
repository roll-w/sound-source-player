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
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowColumnOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme


fun <T> PreferenceScreenScope.radioListPreference(
    title: @Composable (T) -> Unit,
    state: MutableState<T>,
    values: List<T>,
    modifier: Modifier = Modifier,
    key: String? = null,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = RectangleShape,
    icon: @Composable ((T) -> Unit)? = null,
    summary: @Composable ((T) -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachColumn: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: @Composable () -> FlowColumnOverflow = { FlowColumnOverflow.Clip }
) {
    item(key = key, contentType = "RadioListPreference") {
        val contentTypography = LocalContentTypography.current
        val value by state

        RadioListPreference(
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
            maxItemsInEachColumn = maxItemsInEachColumn,
            maxLines = maxLines,
            overflow = overflow()
        )
    }
}

@Composable
fun <T> RadioListPreference(
    state: MutableState<T>,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachColumn: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: FlowColumnOverflow = FlowColumnOverflow.Clip
) {
    var value by state
    RadioListPreference(
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
        maxItemsInEachColumn = maxItemsInEachColumn,
        maxLines = maxLines,
        overflow = overflow
    )
}


@Composable
fun <T> RadioListPreference(
    value: T,
    onValueChange: (T) -> Unit,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (T) -> String = { it.toString() },
    optionEnabled: (T) -> Boolean = { true },
    confirmValueChange: (T) -> Boolean = { true },
    maxItemsInEachColumn: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: FlowColumnOverflow = FlowColumnOverflow.Clip
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
            FlowColumn(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachColumn = maxItemsInEachColumn,
                maxLines = maxLines,
                overflow = overflow
            ) {
                values.forEach {
                    val selected = value == it
                    RadioListPreferenceDefaults.RadioItem(
                        selected = selected,
                        onSelected = {
                            if (confirmValueChange(it)) {
                                onValueChange(it)
                            }
                        },
                        label = valueToText(it),
                        contentTypography = contentTypography,
                        enabled = if (enabled) optionEnabled(it) else false,
                        shape = shape
                    )
                }
            }
        },
    )
}


@PublishedApi
internal object RadioListPreferenceDefaults {
    @Composable
    fun RadioItem(
        selected: Boolean,
        onSelected: () -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
        shape: Shape = RectangleShape
    ) {
        val alpha by animateFloatAsState(
            targetValue = if (enabled) 1f else PreferenceDefaults.DisabledOpacity
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(shape)
                .selectable(selected, enabled, Role.RadioButton, onSelected)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = label,
                style = contentTypography.body,
                color = PlayerTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = alpha
                )
            )
        }
    }
}

