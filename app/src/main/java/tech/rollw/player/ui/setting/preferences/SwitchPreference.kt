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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import tech.rollw.compose.foundation.layout.copy
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme

fun PreferenceScreenScope.switchPreference(
    state: MutableState<Boolean>,
    title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = RectangleShape,
    icon: @Composable ((Boolean) -> Unit)? = null,
    summary: @Composable ((Boolean) -> Unit)? = null,
    bottomWidget: @Composable ((Boolean) -> Unit)? = null
) {
    item(key = key, contentType = "SwitchPreference") {
        val contentTypography = LocalContentTypography.current
        val value by state
        SwitchPreference(
            state = state,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled,
            padding = padding,
            contentTypography = contentTypography,
            shape = shape,
            icon = icon?.let { { icon(value) } },
            summary = summary?.let { { summary(value) } },
            bottomWidget = bottomWidget?.let { { bottomWidget(value) } }
        )
    }
}

@Composable
fun SwitchPreference(
    state: MutableState<Boolean>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    bottomWidget: @Composable (() -> Unit)? = null
) {
    var value by state
    SwitchPreference(
        value = value,
        onValueChange = { value = it },
        title = title,
        modifier = modifier,
        enabled = enabled,
        padding = padding,
        contentTypography = contentTypography,
        shape = shape,
        icon = icon,
        summary = summary,
        bottomWidget = bottomWidget
    )
}

@Composable
fun SwitchPreference(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    bottomWidget: @Composable (() -> Unit)? = null
) {
    Preference(
        title = title,
        modifier = modifier
            .clip(shape)
            .toggleable(
                value = value,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onValueChange
            ),
        enabled = enabled,
        padding = padding,
        contentTypography = contentTypography,
        icon = icon,
        summary = summary,
        endWidget = {
            Switch(
                checked = value,
                onCheckedChange = null,
                modifier = Modifier.padding(
                    padding.copy(start = PreferenceDefaults.Padding)
                ),
                enabled = enabled
            )
        },
        bottomWidget = bottomWidget
    )
}
