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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil.request.ImageRequest
import tech.rollw.compose.foundation.lazy.rememberCurrentOffset
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.components.ImageItem
import tech.rollw.player.ui.components.ImageItemTypography
import tech.rollw.player.ui.model.ImageItemModel

/**
 * @author RollW
 */
@Composable
fun <T> ImageItemList(
    data: List<T>,
    map: (T) -> ImageItemModel,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    selecting: Boolean = false,
    selectedModels: List<T> = emptyList(),
    imageBuilderConfigure: ImageRequest.Builder.() -> Unit = {},
    onScrollChanged: (Int) -> Unit = {},
    onSelectionChanged: (T, Boolean) -> Unit = { _, _ -> },
    onItemClicked: (T, Int) -> Unit = { _, _ -> },
    onItemLongClicked: (T, Int) -> Unit = { _, _ -> },
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {}
) {
    val scroll by rememberCurrentOffset(state)

    val imageItems = data.map(map)

    LaunchedEffect(scroll) {
        onScrollChanged(scroll)
    }
    val imageItemTypography =
        ImageItemTypography.fromContentTypography(contentTypography)

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "Header", contentType = "Header") {
            header()
        }
        itemsIndexed(
            imageItems,
            key = { _, item -> item.id },
            contentType = { _, _ -> "Content" }
        ) { index, item ->
            AnimatedVisibility(visible = true) {
                val selected = selectedModels.contains(data[index])
                Column(
                    modifier = modifier
                        .clip(RoundedCornerShape(25))
                        .animateItem(
                            fadeInSpec = spring(),
                            fadeOutSpec = spring(),
                            placementSpec = spring()
                        ),
                ) {
                    ImageItem(
                        item,
                        // FIXME: cannot scroll when item is long clicked
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onLongClick = {
                                    onItemLongClicked(data[index], index)
                                },
                                onClick = {
                                    onItemClicked(data[index], index)
                                }
                            ),
                        showCheckbox = selecting,
                        selected = selected,
                        typography = imageItemTypography,
                        onSelectChanged = {
                            onSelectionChanged(data[index], it)
                        },
                        imageBuilderConfigure = imageBuilderConfigure
                    )
                }
            }
        }

        item(key = "Footer", contentType = "Footer") {
            footer()
        }
    }
}
