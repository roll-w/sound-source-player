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

import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import tech.rollw.compose.runtime.toNonNull
import tech.rollw.player.data.setting.SettingSpec
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.tools.rememberSetting

/**
 * @param settingSpec setting specification. Value cannot be null.
 */
fun PreferenceScreenScope.checkboxPreference(
    settingSpec: SettingSpec<Boolean, Boolean>,
    title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = RectangleShape,
    icon: @Composable ((Boolean) -> Unit)? = null,
    summary: @Composable ((Boolean) -> Unit)? = null,
    bottomWidget: @Composable ((Boolean) -> Unit)? = null
) {
    item(key = settingSpec.keyName, contentType = "CheckboxPreference") {
        val contentTypography = LocalContentTypography.current
        val settingValue = rememberSetting(settingSpec = settingSpec)
            .toNonNull()
        val value by settingValue

        CheckboxPreference(
            state = settingValue,
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

/**
 * @param settingSpec setting specification. Value cannot be null.
 */
fun PreferenceScreenScope.switchPreference(
    settingSpec: SettingSpec<Boolean, Boolean>,
    title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = RectangleShape,
    icon: @Composable ((Boolean) -> Unit)? = null,
    summary: @Composable ((Boolean) -> Unit)? = null,
    bottomWidget: @Composable ((Boolean) -> Unit)? = null
) {
    item(key = settingSpec.keyName, contentType = "SwitchPreference") {
        val contentTypography = LocalContentTypography.current
        val settingValue = rememberSetting(settingSpec = settingSpec)
            .toNonNull()
        val value by settingValue

        SwitchPreference(
            state = settingValue,
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

/**
 * @param settingSpec setting specification. Value cannot be null.
 */
fun PreferenceScreenScope.chipsPreference(
    settingSpec: SettingSpec<String, String>,
    title: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = ShapeDefaults.Small,
    icon: @Composable ((String) -> Unit)? = null,
    summary: @Composable ((String) -> Unit)? = null,
    valueToText: (String) -> String = { it },
    optionEnabled: (String) -> Boolean = { true },
    confirmValueChange: (String) -> Boolean = { true },
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: @Composable () -> FlowRowOverflow = { FlowRowOverflow.Clip },
    onValueChange: (String) -> Unit = {}
) {
    item(key = settingSpec.keyName, contentType = "SwitchPreference") {
        val contentTypography = LocalContentTypography.current
        val settingValue = rememberSetting(settingSpec = settingSpec)
            .toNonNull()
        val value by settingValue

        ChipsPreference(
            state = settingValue,
            values = settingSpec.valueEntries,
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
            overflow = overflow(),
            onValueChange = onValueChange
        )
    }
}
