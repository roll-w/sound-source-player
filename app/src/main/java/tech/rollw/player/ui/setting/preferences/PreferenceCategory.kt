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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme


fun PreferenceScreenScope.preferenceCategory(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    icon: @Composable (() -> Unit)? = null
) {
    item(key = key, contentType = "PreferenceCategory") {
        PreferenceCategory(
            title = title,
            modifier = modifier
        )
    }
}

private val PreferenceCategoryPadding = PaddingValues(
    start = 16.dp,
    top = 24.dp,
    end = 16.dp,
    bottom = 8.dp
)

@Composable
fun PreferenceCategory(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PreferenceCategoryPadding,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    icon: @Composable (() -> Unit)? = null
) {
    BasicPreference(
        text = {
            Box(
                modifier = Modifier.padding(padding),
                contentAlignment = Alignment.CenterStart
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides PlayerTheme.colorScheme.secondary
                ) {
                    ProvideTextStyle(
                        value = contentTypography.title,
                        content = title
                    )
                }
            }
        },
        modifier = modifier,
        icon = {
            icon?.invoke()
        }
    )
}

