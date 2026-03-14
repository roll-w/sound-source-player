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

package tech.rollw.player.ui.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import tech.rollw.compose.ui.graphics.convertHsv
import tech.rollw.player.R
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.PlaylistType
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.audio.player.PlayerUtils.playPause
import tech.rollw.player.data.media.toBlurredContentImage
import tech.rollw.player.data.setting.DebugSettings
import tech.rollw.player.ui.MediaControllerDelegate
import tech.rollw.player.ui.PlayerAppActivity
import tech.rollw.player.ui.applicationService
import tech.rollw.player.ui.components.AsyncImageBackground
import tech.rollw.player.ui.components.Background
import tech.rollw.player.ui.components.BrushBackground
import tech.rollw.player.ui.components.ColorFilterOverlay
import tech.rollw.player.ui.player.screen.*
import tech.rollw.player.ui.player.viewmodel.AudioListViewModel
import tech.rollw.player.ui.player.viewmodel.PlayerViewModelUpdater
import tech.rollw.player.ui.player.viewmodel.PlaylistViewModelUpdater
import tech.rollw.player.ui.player.viewmodel.UIStateViewModel
import tech.rollw.player.ui.setting.SettingActivity
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.player.ui.tools.ComposeImageUtils
import tech.rollw.player.ui.tools.ImageRequestUtils.cacheKey
import tech.rollw.player.ui.tools.PaletteUtils
import tech.rollw.player.ui.tools.rememberSetting
import tech.rollw.support.Switch

/**
 * @author RollW
 */
class MainActivity : PlayerAppActivity() {
    private val playerStateViewModel by viewModels<PlayerStateViewModel>()
    private val uiStateViewModel by viewModels<UIStateViewModel>()
    private val audioListViewModel by viewModels<AudioListViewModel>(
        factoryProducer = { AudioListViewModel.FACTORY }
    )

    private val audioPlaylistProvider by applicationService<AudioPlaylistProvider>()
    private val playlistViewModelUpdater by lazy {
        PlaylistViewModelUpdater(playerStateViewModel)
    }

    private val playerViewModelUpdater by lazy {
        PlayerViewModelUpdater(playerStateViewModel)
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
        if (state == MediaControllerDelegate.STATE_CONNECTED) {
            runOnUiThread { playerViewModelUpdater.init(mediaController) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: replace with edgeToEdge
        setStatusBar(
            colorBackground = 0,
            lightBar = Switch.ON
        )
        playlistViewModelUpdater.init(audioPlaylistProvider)

        setContent {
            val navController = rememberNavController()
            val statusBarHeight = getStatusBarHeight()

            val debugBackgroundImage by rememberSetting(DebugSettings.BackgroundImageEnabled)
            val debugBackgroundGradient by rememberSetting(DebugSettings.BackgroundGradientEnabled)
            val debugBackgroundGradientMask by rememberSetting(DebugSettings.BackgroundGradientMask)

            val scrollOffset by uiStateViewModel.scrollOffset.collectAsState()

            SoundSourceTheme(
                containerOpacity = 0.35f
            ) {
                val index by playerStateViewModel.index.collectAsState()
                val playlist by playerStateViewModel.playlist.collectAsState()

                val audioContent by remember {
                    derivedStateOf {
                        if (playlist.isEmpty()) {
                            null
                        } else {
                            playlist[index.data]
                        }
                    }
                }

                val currentScreen by uiStateViewModel.currentScreen
                    .collectAsState()
                val selecting by uiStateViewModel.selecting
                    .collectAsState(false)

                val showNowPlayingBar =
                    currentScreen != PlayerScreenLabel.NOW_PLAYING && !selecting
                if (!showNowPlayingBar) {
                    // TODO: check state in BottomBar
                    uiStateViewModel.setScrollOffset()
                }

                Scaffold(
                    bottomBar = {
                        setNavigationBar(
                            colorBackground = MaterialTheme.colorScheme
                                .primaryContainer.toArgb()
                        )
                        BottomBar(
                            selecting = selecting,
                            scrollOffset = scrollOffset,
                            showNowPlayingBar = showNowPlayingBar,
                            navController = navController
                        )
                    }
                ) { paddingValues ->

                    MainPlayerSurface(
                        audioContent = audioContent,
                        showImage = debugBackgroundImage ?: true,
                        showGradient = debugBackgroundGradient ?: false,
                        gradientMask = debugBackgroundGradientMask ?: true
                    ) {
                        MainNavHost(
                            navController = navController,
                            statusBarHeight = statusBarHeight,
                            modifier = Modifier.fillMaxWidth(),
                            paddingValues = paddingValues
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomBar(
        selecting: Boolean,
        scrollOffset: Int,
        showNowPlayingBar: Boolean,
        navController: NavHostController
    ) {
        AnimatedVisibility(
            visible = !selecting,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            PlayerBottomBar(
                scrollOffset = scrollOffset,
                navigationItems = listOf(
                    NavigationItem(
                        id = 0,
                        label = "Media Store",
                        icon = R.drawable.ic_baseline_play_arrow_24
                    ),
                    NavigationItem(
                        id = 1,
                        label = "Settings",
                        icon = R.drawable.ic_baseline_settings_24
                    )
                ),
                systemNavigationHeight = getNavigationBarHeight(),
                backgroundColor = if (showNowPlayingBar) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                showNowPlayingBar = showNowPlayingBar,
                onSelect = {
                    return@PlayerBottomBar when (it) {
                        0 -> {
                            val pop = navController.popBackStack(
                                ROUTE_MEDIA_STORE,
                                false,
                                saveState = true
                            )
                            if (!pop) {
                                navController.navigate(ROUTE_MEDIA_STORE) {
                                    launchSingleTop = true
                                }
                            }
                            true
                        }

                        1 -> {
                            startActivity(buildSettingActivityIntent())
                            false
                        }

                        else -> {
                            false
                        }
                    }
                }
            ) {
                NowPlayingBarScreen(
                    playerStateViewModel = playerStateViewModel,
                    onClick = {
                        navController.navigate(ROUTE_NOW_PLAYING) {
                            launchSingleTop = true
                        }
                    },
                    onClickPlayPause = {
                        mediaController.playPause()
                    },
                    onSwitch = { position, extras ->
                        audioPlaylistProvider.setIndex(position, extras)
                    },
                    onSeek = {
                        mediaController.seekTo(it)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        playerViewModelUpdater.release()
        playlistViewModelUpdater.release()
        super.onDestroy()
    }

    private fun buildSettingActivityIntent(): Intent {
        return Intent(this, SettingActivity::class.java).apply {
        }
    }

    @Composable
    private fun MainPlayerSurface(
        audioContent: AudioContent? = null,
        showImage: Boolean = true,
        showGradient: Boolean = false,
        gradientMask: Boolean = true,
        content: @Composable () -> Unit
    ) {
        if (audioContent == null || !showImage && !showGradient) {
            content()
            return
        }

        val coroutine = rememberCoroutineScope()
        val context = LocalContext.current

        val defaultColors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surface
        )

        data class Colors(
            val colors: List<Color> = defaultColors
        ) {
        }
        // TODO: like shit, needs refactor
        if (showGradient && !showImage) {
            var colors by remember {
                mutableStateOf(
                    Colors(defaultColors)
                )
            }
            LaunchedEffect(audioContent) {
                coroutine.launch {
                    val palette = PaletteUtils.getPalettes(
                        imageLoader,
                        ImageRequest.Builder(this@MainActivity)
                            .data(audioContent.path)
                            .memoryCacheKey(audioContent.cacheKey)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()
                    )
                    if (palette == null) {
                        colors = Colors()
                        return@launch
                    }

                    fun Color.defaultHsl() = convertHsv(
                        saturation = 0.30f,
                        value = 0.85f
                    )

                    colors = Colors(
                        palette.map {
                            it.defaultHsl()
                        }
                    )
                }

            }

            val animation = rememberInfiniteTransition()
            val offset by animation.animateFloat(
                initialValue = 0F,
                targetValue = 100000F,
                animationSpec = infiniteRepeatable(
                    tween(100000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            val disturb by animation.animateFloat(
                initialValue = 0F,
                targetValue = 15000F,
                animationSpec = infiniteRepeatable(
                    tween(200000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            val radius = if (gradientMask) 160000F else 5000F

            Background(
                content = content,
                background = {
                    ColorFilterOverlay(
                        alpha = 0.7F,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        BrushBackground(
                            brush = Brush.radialGradient(
                                colors = colors.colors,
                                center = Offset(
                                    offset + disturb,
                                    offset - disturb
                                ),
                                radius = radius,
                                tileMode = TileMode.Mirror
                            )
                        ) {
                        }
                    }
                }
            )
            return
        }
        // TODO: saturation and brightness control

        AsyncImageBackground(
            imageRequest = ImageRequest.Builder(context)
                .data(audioContent.path.toBlurredContentImage(50))
                .memoryCacheKey(audioContent.cacheKey + "_blur")
                .diskCacheKey(audioContent.cacheKey + "_blur")
                .build(),
            colorOverlayAlpha = 0.55F,
            colorOverlay = MaterialTheme.colorScheme.surface,
            colorFilter = ColorFilter.colorMatrix(
                ColorMatrix(
                    ComposeImageUtils.createColorMatrix(
                        brightness = 55F,
                    )
                )
            ),
            filterQuality = FilterQuality.High
        ) {
            content()
        }
    }

    @Composable
    private fun MainNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        statusBarHeight: Int = 0,
        paddingValues: PaddingValues = PaddingValues(0.dp),
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = ROUTE_MEDIA_STORE,
            enterTransition = {
                if (targetState.destination.route == ROUTE_NOW_PLAYING)
                    slideInVertically { it }
                else fadeIn() + slideInHorizontally { it }
            },
            exitTransition = {
                if (targetState.destination.route == ROUTE_NOW_PLAYING)
                    slideOutVertically { it }
                else fadeOut() + slideOutHorizontally { -it }
            },
            popEnterTransition = {
                if (initialState.destination.route == ROUTE_NOW_PLAYING)
                    slideInVertically { -it }
                else fadeIn() + slideInHorizontally { -it }
            },
            popExitTransition = {
                if (initialState.destination.route == ROUTE_NOW_PLAYING)
                    slideOutVertically { -it }
                else fadeOut() + slideOutHorizontally { it }
            }
        ) {
            composable(route = ROUTE_MEDIA_STORE) {
                label = PlayerScreenLabel.MEDIA_STORE
                uiStateViewModel.setCurrentScreen(PlayerScreenLabel.MEDIA_STORE)
                uiStateViewModel.setScrollOffset()
                MediaStoreScreen(
                    // TODO: replace with paddingValues
                    statusBarHeight = statusBarHeight,
                ) {
                    uiStateViewModel.setScrollOffset()
                    when (it.list) {
                        AudioListType.ALBUM -> navController.navigate(ROUTE_ALBUM)
                        AudioListType.ARTIST -> navController.navigate(ROUTE_ARTIST)
                        else -> navController.navigate(buildAudioListRoute(it.list))
                    }
                }
            }

            composable(
                route = ROUTE_AUDIO_LIST,
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument("id") {
                        defaultValue = Long.MIN_VALUE
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                label = PlayerScreenLabel.AUDIO_LIST
                uiStateViewModel.setCurrentScreen(PlayerScreenLabel.AUDIO_LIST)

                val playlistType = backStackEntry.arguments?.getString("type")?.let {
                    AudioListType.fromParam(
                        it
                    )
                } ?: throw IllegalArgumentException("Type is required for playlist screen.")
                val id = backStackEntry.arguments?.getLong("id")
                AudioListScreen(
                    audioListType = playlistType,
                    playlistId = if (id == Long.MIN_VALUE) null else id,
                    statusBarHeight = statusBarHeight,
                    audioListViewModel = audioListViewModel
                ) {
                    navController.navigate(ROUTE_NOW_PLAYING) {
                        launchSingleTop = true
                    }
                }
            }

            composable(ROUTE_NOW_PLAYING) {
                label = PlayerScreenLabel.NOW_PLAYING
                uiStateViewModel.setCurrentScreen(PlayerScreenLabel.NOW_PLAYING)
                NowPlayingScreen(
                    onSeek = {
                        mediaController.seekTo(it)
                    },
                    onClickPlayPause = {
                        mediaController.playPause()
                    },
                    onSwitch = { position, extras ->
                        audioPlaylistProvider.setIndex(
                            position,
                            extras
                        )
                    }
                )
            }

            composable(ROUTE_ALBUM) {
                label = "Album"
                uiStateViewModel.setCurrentScreen(PlayerScreenLabel.AUDIO_LIST)

                AlbumScreen(PlaylistType.ALBUM)
            }

            composable(ROUTE_ARTIST) {
                label = "Artists"
                uiStateViewModel.setCurrentScreen(PlayerScreenLabel.AUDIO_LIST)

                AlbumScreen(PlaylistType.ARTIST)
            }
        }
    }

    companion object {
        const val ROUTE_MEDIA_STORE = "player/main/media_store"
        const val ROUTE_NOW_PLAYING = "player/main/now_playing"
        const val ROUTE_ALBUM = "player/main/albums"
        const val ROUTE_ARTIST = "player/main/artists"

        private const val ROUTE_AUDIO_LIST = "player/main/list/{type}?id={id}"

        private fun buildAudioListRoute(type: AudioListType, id: Long? = null): String {
            return "player/main/list/${type.value}${if (id != null) "?id=$id" else ""}"
        }
    }

}