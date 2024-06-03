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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

internal fun PreferenceScreenScope.basicPreference(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    icon: @Composable () -> Unit = {},
    endWidget: @Composable () -> Unit = {},
    bottomWidget: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    item {
        BasicPreference(
            text = text,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            icon = icon,
            endWidget = endWidget,
            bottomWidget = bottomWidget,
            onClick = onClick
        )
    }
}

@Composable
internal fun BasicPreference(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    icon: @Composable () -> Unit = {},
    endWidget: @Composable () -> Unit = {},
    bottomWidget: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(enabled, onClick = onClick)
                } else {
                    Modifier.clip(shape)
                }
            ),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Box(
                modifier = Modifier.weight(1f)
            ) {
                text()
            }
            endWidget()
        }

        bottomWidget()
    }
}
