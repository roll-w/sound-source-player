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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.CachePolicy
import tech.rollw.compose.foundation.lazy.rememberCurrentOffset
import tech.rollw.coroutines.flow.lastCacheOrNull
import tech.rollw.player.R
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.player.AudioPlayerManager
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.applicationService
import tech.rollw.player.ui.components.ImageItem
import tech.rollw.player.ui.components.ImageItemTypography
import tech.rollw.player.ui.components.RoundedRow
import tech.rollw.player.ui.model.ImageItemModel
import tech.rollw.player.ui.player.AudioListType
import tech.rollw.player.ui.player.AudioOrder
import tech.rollw.player.ui.player.AudioUtils.formatDuration
import tech.rollw.player.ui.player.dialog.AudioInfoDialog
import tech.rollw.player.ui.player.screen.SelectionKeys.createTextIconItem
import tech.rollw.player.ui.player.viewmodel.AudioListViewModel
import tech.rollw.player.ui.player.viewmodel.UIStateViewModel

/**
 * @author RollW
 */
private class AudioListScreen {
    companion object {
        const val TAG = "AudioListScreen"

        val SOURCE_BUNDLE = Bundle().apply {
            putString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE, TAG)
            putBoolean(AudioPlayerManager.EXTRA_PLAY, true)
        }
    }
}

private object AudioListScreenDefaults {

}

// TODO: refactor

@Composable
fun AudioListScreen(
    audioListType: AudioListType,
    playlistId: Long? = null,
    state: LazyListState = rememberLazyListState(),
    statusBarHeight: Int = 0,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    audioListViewModel: AudioListViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
        factory = AudioListViewModel.FACTORY
    ),
    uiStateViewModel: UIStateViewModel = viewModel(
        LocalContext.current as ComponentActivity
    ),
    onNavigateNowPlaying: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioPlaylistProvider by context.applicationService<AudioPlaylistProvider>()

    val order = when (audioListType) {
        AudioListType.RECENTLY_ADDED -> AudioOrder.LastModified
        AudioListType.ALL_SONGS -> AudioOrder.Title
        else -> AudioOrder.Title
    }
    val reverse = when (audioListType) {
        AudioListType.RECENTLY_ADDED -> true
        else -> false
    }

    val audioContentsSorted = audioListViewModel.getAudioContents(order, reverse)

    val audioContents by audioContentsSorted
        .collectAsStateWithLifecycle(
            audioContentsSorted.lastCacheOrNull() ?: emptyList()
        )

    val models = covert(audioContents)

    var selecting by remember { mutableStateOf(false) }
    val selectedModels = remember {
        mutableStateMapOf<ImageItemModel, Unit>()
    }

    BackHandler(enabled = selecting) {
        selecting = false
        selectedModels.clear()
    }

    fun selectModel(model: ImageItemModel) {
        if (selectedModels.containsKey(model)) {
            selectedModels.remove(model)
        } else {
            selectedModels[model] = Unit
        }
    }

    LaunchedEffect(selecting) {
        uiStateViewModel.setSelecting(selecting)
    }

    ItemList(
        data = models,
        state = state,
        listType = audioListType,
        statusBarHeight = statusBarHeight,
        onScrollChanged = { offset ->
            uiStateViewModel.setScrollOffset(offset)
        },
        selecting = selecting,
        selectedModels = selectedModels.keys.toList(),
        onSelectionChanged = { model, selected ->
            if (selecting) {
                selectModel(model)
            }
        },
        onItemClicked = onItemClicked@{ model, index ->
            if (selecting) {
                selectModel(model)
                return@onItemClicked
            }

            audioPlaylistProvider.setPlaylist(
                playlist = audioContents,
                playlistInfo = Playlist.All,
                index = index,
                extras = AudioListScreen.SOURCE_BUNDLE
            )
            onNavigateNowPlaying()
        },
        onItemLongClicked = { item, index ->
            selecting = true
            selectModel(item)
        }
    )

    AudioListSelectionMenu(
        selecting,
        selectedModels,
        total = audioContents.size,
        onSelectingChanged = { selecting = it },
        requireAudioContent = {
            audioContents[it]
        }
    )
}

@Composable
private fun AudioListSelectionMenu(
    selecting: Boolean,
    selectedModels: SnapshotStateMap<ImageItemModel, Unit>,
    total: Int = 0,
    onSelectingChanged: (Boolean) -> Unit = {},
    requireAudioContent: (Int) -> AudioContent = { AudioContent.EMPTY }
) {
    var audioContent by remember { mutableStateOf(AudioContent.EMPTY) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    if (showInfoDialog) {
        AudioInfoDialog(
            audioContent = audioContent,
            onDismissRequest = {
                showInfoDialog = false
            },
            contentTypography = PlayerTheme.typography.contentMedium
        )
    }

    val selectState by remember {
        derivedStateOf {
            when (selectedModels.size) {
                0 -> SelectState.Empty
                1 -> SelectState.Single
                else -> SelectState.Multiple
            }
        }
    }

    AnimatedVisibility(
        visible = selecting,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(400f),
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            val menuItems = listOf(
                SelectionKeys.ADD_PLAYLIST.createTextIconItem(
                    "加入播放列表",
                    icon = ImageVector.vectorResource(R.drawable.ic_baseline_close_24),
                    enabled = selectState != SelectState.Empty
                ),
                SelectionKeys.PLAY_NEXT.createTextIconItem(
                    "下一首播放",
                    icon = ImageVector.vectorResource(R.drawable.ic_baseline_close_24),
                    enabled = selectState != SelectState.Empty
                ),
                SelectionKeys.DELETE.createTextIconItem(
                    textRes = R.string.delete,
                    iconRes = R.drawable.ic_baseline_close_24,
                    enabled = selectState != SelectState.Empty
                ),
                SelectionKeys.SHARE.createTextIconItem(
                    "分享",
                    icon = ImageVector.vectorResource(R.drawable.ic_baseline_close_24),
                    enabled = selectState != SelectState.Empty
                ),
                SelectionKeys.AUDIO_INFO.createTextIconItem(
                    "详情",
                    icon = ImageVector.vectorResource(R.drawable.ic_baseline_close_24),
                    enabled = selectState == SelectState.Single
                )
            )

            SelectionMenuSheet(
                selectedCount = selectedModels.size,
                total = total,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium),
                items = menuItems,
                onClickClose = {
                    onSelectingChanged(false)
                    selectedModels.clear()
                },
                onClick = { item, index ->
                    when (item.key) {
                        SelectionKeys.ADD_PLAYLIST -> {
                            true
                        }

                        SelectionKeys.AUDIO_INFO -> {
                            selectedModels.keys.firstOrNull()?.let {
                                audioContent = requireAudioContent(it.index)
                                showInfoDialog = true
                            }
                            true
                        }

                        else -> false
                    }
                }
            )
        }
    }
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
            val selected = item in selectedModels

            RoundedRow(
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(),
                    fadeOutSpec = spring(),
                    placementSpec = spring()
                )
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
                        diskCacheKey("audio_content_" + item.id)
                        memoryCacheKey("audio_content_" + item.id)
                        memoryCachePolicy(CachePolicy.ENABLED)
                        diskCachePolicy(CachePolicy.DISABLED)
                    }
                )
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
    list: List<AudioContent>,
): List<ImageItemModel> {
    // TODO: move to viewModel or anywhere else
    var index = 0
    return list.map {
        val audio = it.audio
        ImageItemModel(
            id = audio.id.toString(),
            title = audio.title ?: "",
            subtitle = "${audio.artist} - ${audio.album}",
            info = "${audio.formatDuration()} | ${audio.type.name}",
            image = it.path,
            index = index++
        )
    }
}
