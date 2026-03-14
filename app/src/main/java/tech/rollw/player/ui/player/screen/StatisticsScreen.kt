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

package tech.rollw.player.ui.player.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.theme.LocalContainerOpacity

/**
 * @author RollW
 */
object StatisticsScreen {

}

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier
) {
    StatisticsView()
}

@Composable
fun StatisticsView(
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = lazyGridState,
        modifier = modifier
    ) {
        item {
            StatisticsItemView(
                name = "Total Songs",
                value = "123"
            )
        }

        item {
            StatisticsItemView(
                name = "Total Albums",
                value = "75"
            )
        }

        item {
            StatisticsItemView(
                name = "Total Artists",
                value = "41"
            )
        }

        item {
            StatisticsItemView(
                name = "Total Playlists",
                value = "8"
            )
        }
    }
}


@Composable
private fun StatisticsItemView(
    name: String,
    value: String,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    onClick: () -> Unit = {},
) {
    val defaultColors = CardDefaults.cardColors()

    val colors = defaultColors.copy(
        containerColor = defaultColors.containerColor.copy(
            alpha = LocalContainerOpacity.current
        )
    )

    Card(
        modifier = modifier
            .padding(5.dp),
        colors = colors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 14.dp,
                    vertical = 20.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    style = contentTypography.info
                )
                Text(
                    text = value,
                    modifier = Modifier.padding(top = 5.dp),
                    style = contentTypography.title
                )
            }
        }
    }
}
