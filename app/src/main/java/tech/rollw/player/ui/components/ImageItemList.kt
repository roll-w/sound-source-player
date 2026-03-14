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

package tech.rollw.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import tech.rollw.compose.foundation.lazy.rememberCurrentOffset
import tech.rollw.player.ui.model.ImageItemModel

/**
 * @author RollW
 */
object ImageItemListDefaults {

}

/**
 * Image item list.
 *
 * @param data The data list.
 * @param modifier The modifier.
 * @param onSelectionChanged Selection changed callback. Return true
 *                           to keep the selection state, false to reset
 *                           the selection state.
 */
@Composable
fun ImageItemList(
    data: List<ImageItemModel>,
    modifier: Modifier = Modifier,
    title: String = "",
    highlight: Int = -1,
    statusBarHeight: Int = 0,
    imageItemTypography: ImageItemTypography = ImageItemDefaults.typography(),
    onItemClicked: (ImageItemModel, Int) -> Unit = { _, _ -> },
    onSelectionChanged: (List<ImageItemModel>) -> Boolean = { _ -> true },
    onScroll: (Int) -> Unit = { _ -> },
    imageBuilderConfigure: ImageRequest.Builder.() -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scroll by rememberCurrentOffset(listState)

    var longClicked by remember { mutableStateOf(false) }

    LaunchedEffect(scroll) {
        onScroll(scroll)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) {
                        statusBarHeight.toDp()
                    })
            )
            Column {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }

        itemsIndexed(data, key = { _, item -> item.id }) { index, item ->
            AnimatedVisibility(visible = true) {
                RoundedRow(
                    modifier = Modifier.animateItemPlacement()
                ) {
                    ImageItem(
                        item,
                        // FIXME: cannot scroll when item is long clicked
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onLongClick = {
                                    longClicked = true
                                },
                                onClick = {
                                    if (!longClicked) {
                                        onItemClicked(item, index)
                                        return@combinedClickable
                                    }
                                }
                            ),
                        typography = imageItemTypography,
                        imageBuilderConfigure = imageBuilderConfigure
                    )
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}