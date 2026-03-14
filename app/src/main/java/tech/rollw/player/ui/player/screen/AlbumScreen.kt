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

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.CachePolicy
import tech.rollw.compose.foundation.lazy.rememberCurrentOffset
import tech.rollw.player.analytics.LocalAnalytics
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.list.PlaylistType
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.components.ImageItem
import tech.rollw.player.ui.components.ImageItemTypography
import tech.rollw.player.ui.components.RoundedRow
import tech.rollw.player.ui.model.ImageItemModel
import tech.rollw.player.ui.player.AudioListType
import tech.rollw.player.ui.player.AudioListType.Companion.toAudioListType
import tech.rollw.player.ui.player.AudioUtils.formatDuration
import tech.rollw.player.ui.player.viewmodel.AudioListViewModel
import tech.rollw.player.ui.player.viewmodel.UIStateViewModel

/**
 * @author RollW
 */
// TODO: this is a temporary implementation, will be replaced by a more advanced one
@Composable
fun AlbumScreen(
    type: PlaylistType,
    state: LazyListState = rememberLazyListState(),
    audioListViewModel: AudioListViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        factory = AudioListViewModel.FACTORY
    ),
    uiStateViewModel: UIStateViewModel = viewModel(
        LocalContext.current as ComponentActivity
    )
) {
    val context = LocalContext.current
    val analytics = LocalAnalytics.current

    val playlists by audioListViewModel.getPlaylists(type)
        .collectAsState(initial = emptyList())
    val sortedPlaylists = playlists.sortedBy { it.name }

    val models = covert(sortedPlaylists)
    var selectedPlaylist by remember { mutableStateOf(Playlist.EMPTY) }

    ItemList(
        data = models,
        state = state,
        listType = type.toAudioListType(),
        statusBarHeight = 200,
        onItemClicked = { item, index ->
            item.id.toLongOrNull()?.let {
                selectedPlaylist = sortedPlaylists[index]
            }
        }
    )
}

@Composable
private fun ItemList(
    data: List<ImageItemModel>,
    listType: AudioListType,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    statusBarHeight: Int = 0,
    selecting: Boolean = false,
    selectedModels: List<ImageItemModel> = emptyList(),
    onScrollChanged: (Int) -> Unit = { },
    onSelectionChanged: (ImageItemModel, Boolean) -> Unit = { _, _ -> },
    onItemClicked: (ImageItemModel, Int) -> Unit = { _, _ -> },
    onItemLongClicked: (ImageItemModel, Int) -> Unit = { _, _ -> }
) {
    val scroll by rememberCurrentOffset(state)

    LaunchedEffect(scroll) {
        onScrollChanged(scroll)
    }

    val contentNormal = PlayerTheme.typography.contentNormal
    val contentTypography = contentNormal.copy(
        title = contentNormal.title.copy(
            fontWeight = FontWeight.SemiBold
        ),
    )
    val imageItemTypography =
        ImageItemTypography.fromContentTypography(contentTypography)

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "Header", contentType = "Header") {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) {
                        statusBarHeight.toDp()
                    })
            )
            Column {
                Text(
                    text = stringResource(listType.resId),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
        itemsIndexed(
            data,
            key = { _, item -> item.id },
            contentType = { _, _ -> "AudioColumn" }
        ) { index, item ->
            AnimatedVisibility(visible = true) {
                val selected = item in selectedModels
                RoundedRow(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                ) {
                    ImageItem(
                        item,
                        // FIXME: cannot scroll when item is long clicked
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onLongClick = {
                                    onItemLongClicked(item, index)
                                },
                                onClick = {
                                    onItemClicked(item, index)
                                }
                            ),
                        showCheckbox = selecting,
                        selected = selected,
                        typography = imageItemTypography,
                        onSelectChanged = {
                            onSelectionChanged(item, it)
                        },
                        imageBuilderConfigure = {
                            diskCacheKey("playlist_" + item.id)
                            memoryCacheKey("playlist_" + item.id)
                            memoryCachePolicy(CachePolicy.ENABLED)
                            diskCachePolicy(CachePolicy.DISABLED)
                        }
                    )
                }
            }
        }

        item(key = "Footer", contentType = "Footer") {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

private fun covert(
    list: List<Playlist>,
): List<ImageItemModel> {
    var index = 0
    return list.map {
        ImageItemModel(
            id = it.id.toString(),
            title = it.name.ifEmpty { "Unknown" },
            subtitle = "${it.count} | ${it.duration.formatDuration()}",
            image = it.coverPath,
            index = index++
        )
    }
}
