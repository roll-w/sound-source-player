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

@file:Suppress("AnimatedContentLabel")

package tech.rollw.player.ui.player.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import tech.rollw.compose.material3.RoundTrack
import tech.rollw.player.R
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.audio.tag.Lyric
import tech.rollw.player.audio.tag.LyricParser
import tech.rollw.player.audio.tag.TagUtils.openTag
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.player.AudioUtils.formatDuration
import tech.rollw.player.ui.player.PlayerStateViewModel
import tech.rollw.player.ui.player.components.LyricView
import tech.rollw.player.ui.player.components.LyricViewTimestampPolicy
import tech.rollw.player.ui.player.dialog.AudioInfoDialog
import tech.rollw.player.ui.player.viewmodel.PlayerViewModel
import tech.rollw.player.ui.tools.ImageRequestUtils.imageRequestBuilder
import kotlin.math.absoluteValue

private const val TAG = "NowPlayingScreen"

internal object NowPlayingScreen {
    val SOURCE_BUNDLE = Bundle().apply {
        putString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE, TAG)
    }

    val LYRIC_SOURCE_BUNDLE = Bundle().apply {
        putString(AudioPlaylistProvider.EXTRA_CHANGE_SOURCE, "NowPlayingScreen.Lyric")
    }
}

private object NowPlayingScreenDefaults {
    val ImageSize = 300.dp
    val ImageCornerPercent = 5
}

@JvmInline
internal value class NowPlayingScreenPage private constructor(private val serial: Int) {
    companion object {
        val Song = NowPlayingScreenPage(0)

        val Lyric = NowPlayingScreenPage(1)
    }

    object PageSaver : Saver<NowPlayingScreenPage, Int> {
        override fun restore(value: Int): NowPlayingScreenPage {
            return when (value) {
                0 -> Song
                1 -> Lyric
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }

        override fun SaverScope.save(value: NowPlayingScreenPage): Int? {
            return value.serial
        }
    }
}

@Composable
fun NowPlayingScreen(
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    playerStateViewModel: PlayerStateViewModel = viewModel(
        LocalContext.current as ComponentActivity
    ),
    onClickPlayPause: () -> Unit = {},
    onSwitch: (Int, extras: Bundle?) -> Unit = { _, _ -> },
    onSeek: (Long) -> Unit = {},
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
    val index by playerStateViewModel.index.collectAsState()
    val playlist by playerStateViewModel.playlist.collectAsState()
    val playing by playerStateViewModel.playing.collectAsState(false)
    val audioPosition by playerStateViewModel.audioPosition.collectAsState()

    if (playlist.isEmpty()) {
        Text(
            text = "This should not open without a playlist.",
            modifier = Modifier.padding(30.dp)
        )
        return
    }

    var currentScreen by rememberSaveable(
        stateSaver = NowPlayingScreenPage.PageSaver
    ) {
        mutableStateOf(NowPlayingScreenPage.Song)
    }

    val pagerState = rememberPagerState(
        pageCount = { playlist.size },
        initialPage = index.data
    )

    val currentAudio = playlist[index.data]

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect {
            if (it == index.data) {
                return@collect
            }
            onSwitch(it, NowPlayingScreen.SOURCE_BUNDLE)
        }
    }

    LaunchedEffect(playerStateViewModel.index) {
        snapshotFlow { index }
            .collect {
                if (it.source == TAG || isDragged) {
                    return@collect
                }
                coroutineScope.launch {
                    pagerState.animateScrollToPage(it.data)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        AnimatedContent(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (it) {
                    NowPlayingScreenPage.Song -> {
                        SongPage(
                            state = pagerState,
                            playlist = playlist,
                            contentTypography = contentTypography,
                            playing = playing,
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .fillMaxWidth(),
                            onClickCover = {
                                currentScreen = NowPlayingScreenPage.Lyric
                            }
                        )
                    }

                    NowPlayingScreenPage.Lyric -> {
                        LyricPage(
                            audioContent = currentAudio,
                            audioPosition = audioPosition,
                            modifier = Modifier
                                .padding(start = 14.dp, end = 14.dp, top = 20.dp),
                            onSeek = onSeek,
                            onClickBack = {
                                currentScreen = NowPlayingScreenPage.Song
                            }
                        )
                    }
                }
            }
        }

        PlayerControlComponent(
            audioContent = currentAudio,
            currentPosition = audioPosition,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            playing = playing,
            onClickPlayPause = onClickPlayPause,
            onClickPrevious = {
                when (currentScreen) {
                    NowPlayingScreenPage.Song -> coroutineScope.launch {
                        pagerState.animateScrollToPage(index.data - 1)
                    }

                    NowPlayingScreenPage.Lyric -> {
                        onSwitch(index.data - 1, NowPlayingScreen.LYRIC_SOURCE_BUNDLE)
                    }
                }
            },
            onClickNext = {
                when (currentScreen) {
                    NowPlayingScreenPage.Song -> coroutineScope.launch {
                        pagerState.animateScrollToPage(index.data + 1)
                    }

                    NowPlayingScreenPage.Lyric -> {
                        onSwitch(index.data + 1, NowPlayingScreen.LYRIC_SOURCE_BUNDLE)
                    }
                }
            },
            onSeek = onSeek,
            windowAdaptiveInfo = windowAdaptiveInfo
        )
    }
}

@Composable
private fun SongPage(
    state: PagerState,
    playlist: List<AudioContent>,
    playing: Boolean,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onClickCover: () -> Unit = {}
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        beyondViewportPageCount = 1
    ) { page ->
        val audioContent = playlist[page]
        Row(
            modifier = Modifier.graphicsLayer {
                val pageOffset = (
                        (state.currentPage - page) + state
                            .currentPageOffsetFraction
                        ).absoluteValue
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
                scaleX = lerp(
                    start = 0.7f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
                scaleY = lerp(
                    start = 0.7f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            }
        ) {
            AudioContentView(
                audioContent = audioContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable(
                        onClick = onClickCover,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentTypography = contentTypography
            )
        }
    }
}


@Composable
private fun LyricPage(
    audioContent: AudioContent,
    audioPosition: Long,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onSeek: (Long) -> Unit = {},
    onClickBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lyric = loadLyrics(audioContent, context)

    BackHandler(true) {
        onClickBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LyricView(
            lyric = lyric,
            activeIndex = lyric.findAt(audioPosition),
            modifier = Modifier,
            timestampPolicy = LyricViewTimestampPolicy.TIMESTAMP_POLICY_NONE,
            nextAlpha = 0.5f,
            previousAlpha = 0.25f,
            onClick = {
                onSeek(it.timestamp)
            }
        )
    }
}


/**
 * Calculate the button size based on the window size class
 *
 * @return Triple of primary button size, primary button padding, secondary button size
 */
private fun calculateButtonSize(windowSizeClass: WindowSizeClass): ButtonSizes {
    return when (windowSizeClass.windowHeightSizeClass) {
        WindowHeightSizeClass.MEDIUM -> {
            ButtonSizes(60.dp, 16.dp, 45.dp)
        }

        WindowHeightSizeClass.EXPANDED -> {
            ButtonSizes(80.dp, 20.dp, 60.dp)
        }

        else -> {
            ButtonSizes(45.dp, 12.dp, 30.dp)
        }
    }
}

@Immutable
private data class ButtonSizes(
    val primarySize: Dp,
    val primaryPadding: Dp,
    val secondarySize: Dp
)

@Composable
private fun PlayerControlComponent(
    audioContent: AudioContent,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    playing: Boolean = false,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onSeek: (Long) -> Unit = {},
    onClickPrevious: () -> Unit = {},
    onClickNext: () -> Unit = {},
    onClickPlayPause: () -> Unit = {},
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
    val (primarySize, primaryPadding, secondarySize) = calculateButtonSize(windowAdaptiveInfo.windowSizeClass)

    val duration = audioContent.audio.duration

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

    Column(modifier = modifier.padding(20.dp)) {
        AudioInfoBox(
            audioContent = audioContent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp),
            contentTypography = contentTypography,
            onClick = {
                showInfoDialog = true
            }
        )

        Seekbar(
            duration = duration,
            currentPosition = currentPosition,
            modifier = Modifier
                .fillMaxWidth(),
            onSeek = onSeek
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                modifier = Modifier.size(secondarySize),
                onClick = { onClickPrevious() }
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    imageVector = ImageVector
                        .vectorResource(id = R.drawable.ic_baseline_skip_previous_24),
                    contentDescription = "Previous"
                )
            }

            IconButton(
                modifier = Modifier
                    .padding(primaryPadding),
                onClick = { onClickPlayPause() }
            ) {
                val iconModifier = Modifier
                    .size(primarySize)
                    .padding(5.dp)

                AnimatedContent(targetState = playing) {
                    when (it) {
                        true -> Icon(
                            modifier = Modifier
                                .then(iconModifier),
                            imageVector = ImageVector
                                .vectorResource(R.drawable.ic_baseline_pause_24),
                            contentDescription = "Pause"
                        )

                        false -> Icon(
                            modifier = Modifier
                                .then(iconModifier),
                            imageVector = ImageVector
                                .vectorResource(R.drawable.ic_baseline_play_arrow_24),
                            contentDescription = "Play"
                        )
                    }
                }
            }

            IconButton(
                modifier = Modifier.size(secondarySize),
                onClick = { onClickNext() }
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    imageVector = ImageVector
                        .vectorResource(id = R.drawable.ic_baseline_skip_next_24),
                    contentDescription = "Next"
                )
            }
        }
    }

}

@Composable
private fun AudioInfoBox(
    audioContent: AudioContent,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = LocalContentTypography.current,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        Crossfade(targetState = audioContent.audio) {
            Text(
                text = "${it.title}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = contentTypography.title,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                modifier = Modifier.basicMarquee(),
                color = PlayerTheme.colorScheme.onSurface
            )
        }

        Crossfade(targetState = audioContent.audio) {
            Text(
                text = "${it.artist} - ${it.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = contentTypography.subtitle,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .basicMarquee()
                    .alpha(0.8f),
                color = PlayerTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun Seekbar(
    duration: Long,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    typography: ContentTypography = PlayerTheme.typography.contentNormal,
    onSeek: (Long) -> Unit = {},
    height: Dp = 6.dp
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
    val sliderValue by derivedStateOf {
        if (isInteracting) {
            inputValue
        } else {
            progress
        }
    }
    val activeColor = PlayerTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            interactionSource = interactionSource,
            valueRange = 0F..duration.toFloat(),
            thumb = {},
            track = {
                SliderDefaults.RoundTrack(
                    it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    colors = SliderDefaults.colors(
                        activeTrackColor = activeColor,
                        inactiveTrackColor = PlayerTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f),
                    ),
                    height = height
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 3.dp, end = 3.dp, top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = sliderValue.toLong().formatDuration(),
                style = typography.info,
                color = activeColor
            )
            Text(
                text = duration.formatDuration(),
                style = typography.info,
                color = activeColor
            )
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(100))
            .clickable(
                role = Role.Button,
                onClick = { onClick() },
                enabled = true,
                interactionSource = interactionSource,
                indication = ripple(bounded = false)
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun loadLyrics(
    audio: AudioContent,
    context: Context
): Lyric {
    // TODO: replace with cache
    audio.openTag(context).use {
        val lyric = LyricParser.createFrom(it)?.parse()
        return lyric ?: Lyric.EMPTY
    }
}

@Composable
private fun AudioContentView(
    audioContent: AudioContent,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val context = LocalContext.current

    var contentSize by remember { mutableStateOf(IntSize(0, 0)) }
    var subContentSize by remember { mutableStateOf(IntSize(0, 0)) }

    // calculate the standard width for the image and text
    val stdWidth by remember {
        derivedStateOf {
            contentSize.width.coerceAtMost(contentSize.height - subContentSize.height)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                contentSize = it.size
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SubcomposeAsyncImage(
            model = audioContent
                .imageRequestBuilder(context)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.25f),
                    tint = PlayerTheme.colorScheme.onSurface
                )
            },
            error = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.25f),
                    tint = PlayerTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .width(with(LocalDensity.current) {
                    stdWidth.toDp()
                })
                .height(with(LocalDensity.current) {
                    stdWidth.toDp()
                })
                .clip(
                    RoundedCornerShape(
                        NowPlayingScreenDefaults.ImageCornerPercent
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(with(LocalDensity.current) {
                    stdWidth.toDp()
                })
                .onGloballyPositioned {
                    subContentSize = it.size
                }
                .padding(top = 10.dp)
        ) {
        }
    }
}