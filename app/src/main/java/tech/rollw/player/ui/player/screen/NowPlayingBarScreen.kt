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

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.SliderDefaults.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import tech.rollw.player.R
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.ui.components.RoundedRow
import tech.rollw.player.ui.player.PlayerStateViewModel
import tech.rollw.support.SourcedData

/**
 * @author RollW
 */
class NowPlayingBarScreen {
    companion object {
        const val TAG = "NowPlayingBarScreen"

        val SOURCE_BUNDLE = Bundle().apply {
            putString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE, TAG)
        }
    }
}

private object NowPlayingBarScreenDefaults {
    val ImageSize = 50.dp
    val ImageCornerPercent = 25
    val TitleTextSize = 14.sp
    val SubtitleTextSize = TitleTextSize
    val Padding = 10.dp

    // TODO: theme colors
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingBarScreen(
    playerStateViewModel: PlayerStateViewModel = viewModel(LocalContext.current as ComponentActivity),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClickPlayPause: () -> Unit = {},
    onSwitch: (Int) -> Unit = {},
    onSeek: (Long) -> Unit = {}
) {
    val playlist by playerStateViewModel.playlist
        .collectAsState(emptyList())
    val position by playerStateViewModel.index
        .collectAsState(SourcedData(0))

    val pagerState = rememberPagerState(pageCount = {
        if (playlist.isEmpty()) {
            1
        } else playlist.size
    }, initialPage = position.data)

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            if (it == position.data) {
                return@collect
            }
            onSwitch(it)
        }
    }

    LaunchedEffect(playerStateViewModel.index) {
        snapshotFlow { position }
            .collect {
                if (it.source == NowPlayingBarScreen.TAG || isDragged) {
                    return@collect
                }
                coroutineScope.launch {
                    pagerState.animateScrollToPage(it.data)
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        if (playlist.isEmpty()) {
            EmptyView()
            return@HorizontalPager
        }
        val playing by playerStateViewModel.playing
            .collectAsState(false)

        val audioContent = playlist[page]

        RoundedRow {
            AudioContentView(
                audioContent = audioContent,
                playing = playing,
                onClickPlayPause = onClickPlayPause,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
    val currentPosition by playerStateViewModel.audioPosition
        .collectAsState(0L)

    if (playlist.isNotEmpty()) {
        val audioContent = playlist[position.data]
        PlayerSeekbar(
            duration = audioContent.audio.duration,
            currentPosition = currentPosition,
            onSeek = onSeek,
            modifier = Modifier
                .padding(
                    bottom = 10.dp,
                    start = 10.dp, end = 10.dp
                )
        )
    }
}

@Composable
private fun PlayerSeekbar(
    duration: Long,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit = {}
) {
    val progress = currentPosition.toFloat()

    var inputValue by remember {
        mutableFloatStateOf(progress)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged

    @SuppressLint("UnrememberedMutableState")
    // remember the sliderValue will cause a bug that cannot
    // update the sliderValue
    val sliderValue by derivedStateOf {
        if (isInteracting) {
            inputValue
        } else {
            progress
        }
    }

    Slider(
        sliderValue,
        onValueChange = {
            inputValue = it
        },
        onValueChangeFinished = {
            onSeek(inputValue.toLong())
        },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(
                RoundedCornerShape(25)
            ),
        interactionSource = interactionSource,
        valueRange = 0F..duration.toFloat(),
        thumb = {
            val thumbInteractionSource = remember { MutableInteractionSource() }

            SliderDefaults.Thumb(
                interactionSource = thumbInteractionSource,
                thumbSize = DpSize.Zero,
                colors = colors()
            )
        }
    )
}


/**
 * @param onClickPlayPause return true if the audio is playing
 *                         after the click
 */
@Composable
private fun AudioContentView(
    audioContent: AudioContent,
    playing: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClickPlayPause: () -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(NowPlayingBarScreenDefaults.Padding)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(audioContent.path)
                .memoryCacheKey("${audioContent.audio.id}")
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(NowPlayingBarScreenDefaults.ImageSize)
                .clip(
                    RoundedCornerShape(
                        NowPlayingBarScreenDefaults.ImageCornerPercent
                    )
                )
        )
        Column(
            modifier = Modifier.padding(start = 10.dp)
                .weight(1F),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${audioContent.audio.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = NowPlayingBarScreenDefaults.TitleTextSize
            )

            Text(
                text = "${audioContent.audio.artist} - ${audioContent.audio.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = NowPlayingBarScreenDefaults.SubtitleTextSize
            )
        }
        IconButton(
            onClick = {
                onClickPlayPause()
            },
            modifier = Modifier.padding(10.dp),
        ) {
            PlayPauseIcon(
                playState = playing,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

/**
 * @param playState true if playing
 */
@Composable
private fun PlayPauseIcon(
    playState: Boolean,
    modifier: Modifier = Modifier
) {
    if (playState) {
        Icon(
            imageVector = ImageVector.vectorResource(
                id = R.drawable.ic_baseline_pause_24
            ),
            contentDescription = "Play/Pause",
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = ImageVector.vectorResource(
                id = R.drawable.ic_baseline_play_arrow_24
            ),
            contentDescription = "Play/Pause",
            modifier = modifier
        )
    }
}

@Composable
private fun EmptyView() {
    Text(
        text = "No Playlist Available",
        modifier = Modifier.fillMaxWidth()
            .padding(20.dp),
    )
}
