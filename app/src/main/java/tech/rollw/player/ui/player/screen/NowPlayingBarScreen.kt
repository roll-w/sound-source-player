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
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import tech.rollw.player.R
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.ui.components.RoundedRow
import tech.rollw.player.ui.player.PlayerStateViewModel
import tech.rollw.player.ui.player.viewmodel.PlayerViewModel
import tech.rollw.player.ui.tools.ImageRequestUtils.imageRequestBuilder
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
    val AudioContentPaddingX = 10.dp
    val AudioContentPaddingY = 5.dp

    val PlayPauseIconSize = 30.dp
    val SeekbarHeight = 4.dp
    val SeekbarPadding = 2.dp
}

@Composable
fun NowPlayingBarScreen(
    playerStateViewModel: PlayerStateViewModel = viewModel(LocalContext.current as ComponentActivity),
    onClick: (AudioContent) -> Unit = {},
    onLongClick: (AudioContent) -> Unit = {},
    onClickPlayPause: (AudioContent) -> Unit = {},
    onSwitch: (Int, extras: Bundle?) -> Unit = { _, _ -> },
    onSeek: (Long) -> Unit = {}
) {
    val playlist by playerStateViewModel.playlist
        .collectAsState(emptyList())
    val position by playerStateViewModel.index
        .collectAsState(SourcedData(0))
    val currentPosition by playerStateViewModel.audioPosition
        .collectAsState(PlayerViewModel.INVALID_POSITION)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (playlist.isEmpty()) {
            EmptyView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            return
        }
        val playing by playerStateViewModel.playing
            .collectAsState(false)
        val audioContent = playlist[position.data]

        Seekbar(
            duration = audioContent.audio.duration,
            currentPosition = currentPosition,
            onSeek = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = NowPlayingBarScreenDefaults.SeekbarPadding)
        )

        PlayerBarPager(
            playlist = playlist,
            index = position,
            playing = playing,
            onClick = onClick,
            onLongClick = onLongClick,
            onClickPlayPause = onClickPlayPause,
            onSwitch = onSwitch
        )
    }
}

@Composable
private fun PlayerBarPager(
    playlist: List<AudioContent>,
    index: SourcedData<Int>,
    playing: Boolean,
    onClick: (AudioContent) -> Unit,
    onLongClick: (AudioContent) -> Unit,
    onClickPlayPause: (AudioContent) -> Unit,
    onSwitch: (Int, extras: Bundle?) -> Unit = { _, _ -> }
) {
    val pagerState = rememberPagerState(pageCount = {
        if (playlist.isEmpty()) {
            1
        } else playlist.size
    }, initialPage = index.data)

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            if (it == index.data) {
                return@collect
            }
            onSwitch(it, NowPlayingBarScreen.SOURCE_BUNDLE)
        }
    }

    LaunchedEffect(index) {
        snapshotFlow { index }
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
        modifier = Modifier.fillMaxWidth(),
        beyondViewportPageCount = 1,
    ) { page ->
        if (playlist.isEmpty()) {
            return@HorizontalPager
        }
        val pageContent = playlist[page]
        RoundedRow {
            AudioContentView(
                audioContent = pageContent,
                playing = playing,
                onClickPlayPause = onClickPlayPause,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
}

@Composable
private fun Seekbar(
    duration: Long,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit = {}
) {
    if (currentPosition == PlayerViewModel.INVALID_POSITION) {
        return
    }
    val progress = currentPosition.toFloat()

    var inputValue by remember {
        mutableFloatStateOf(progress)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged

    @SuppressLint("UnrememberedMutableState")
    // TODO: remember derivedStateOf sliderValue will cause a bug that cannot
    //  update the sliderValue
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
        onValueChangeFinished = finish@{
            if (inputValue < 0) {
                return@finish
            }
            onSeek(inputValue.toLong())
        },
        modifier = modifier
            .fillMaxWidth()
            .height(NowPlayingBarScreenDefaults.SeekbarHeight),
        interactionSource = interactionSource,
        valueRange = 0F..duration.toFloat(),
        thumb = {
        },
        track = {
            SliderDefaults.Track(
                it,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NowPlayingBarScreenDefaults.SeekbarHeight),
                thumbTrackGapSize = 0.dp,
                trackInsideCornerSize = 0.dp,
                drawStopIndicator = {}
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
    onClick: (AudioContent) -> Unit = {},
    onLongClick: (AudioContent) -> Unit = {},
    onClickPlayPause: (AudioContent) -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(audioContent) },
                onLongClick = { onLongClick(audioContent) }
            )
            .padding(
                vertical = NowPlayingBarScreenDefaults.AudioContentPaddingY,
                horizontal = NowPlayingBarScreenDefaults.AudioContentPaddingX
            )
    ) {
        AsyncImage(
            model = audioContent
                .imageRequestBuilder(context)
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
            modifier = Modifier
                .padding(start = 10.dp)
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
                onClickPlayPause(audioContent)
            },
            modifier = Modifier.padding(10.dp),
        ) {
            PlayPauseIcon(
                playState = playing,
                modifier = Modifier.size(
                    NowPlayingBarScreenDefaults.PlayPauseIconSize
                )
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
    AnimatedContent(playState) {
        when (it) {
            true -> {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.ic_baseline_pause_24
                    ),
                    contentDescription = stringResource(R.string.pause),
                    modifier = modifier
                )
            }

            false -> {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.ic_baseline_play_arrow_24
                    ),
                    contentDescription = stringResource(R.string.play),
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun EmptyView(
    modifier: Modifier = Modifier
) {
    Text(
        text = "No Playlist",
        modifier = modifier,
    )
}
