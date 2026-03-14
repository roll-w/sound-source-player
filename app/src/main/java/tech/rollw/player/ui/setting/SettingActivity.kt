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

package tech.rollw.player.ui.setting

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.material.elevation.SurfaceColors
import tech.rollw.compose.ui.text.FontUnit.Companion.lineHeight
import tech.rollw.compose.ui.text.copy
import tech.rollw.player.R
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.setting.screen.DebugScreen
import tech.rollw.player.ui.setting.screen.SettingMenuScreen
import tech.rollw.player.ui.setting.screen.StorageScreen
import tech.rollw.player.ui.setting.screen.UIScreen
import tech.rollw.player.ui.setting.screen.debug.*
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.support.Switch
import tech.rollw.support.appcompat.AppActivity

/**
 * @author RollW
 */
class SettingActivity : AppActivity() {
    private val settingViewModel by viewModels<SettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivityLauncher()

        val colorSurface = SurfaceColors.SURFACE_0.getColor(this)
        val lightBar = Switch.of(!isNightMode())
        setStatusBar(colorBackground = 0, lightBar = lightBar)
        setNavigationBar(colorBackground = colorSurface, lightBar = lightBar)

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                navigateIfExtras(navController)
            }

            SoundSourceTheme {
                val rawContentTypography = PlayerTheme.typography.contentMedium
                val preferenceContentTypography = PlayerTheme.typography.contentNormal.copy(
                    title = rawContentTypography.title.copy(
                        fontWeight = FontWeight.Normal,
                        fontUnit = 24.sp lineHeight 34.sp
                    ),
                    subtitle = rawContentTypography.subtitle.copy(
                        fontWeight = FontWeight.Normal,
                        fontUnit = 18.sp lineHeight 28.sp
                    ),
                    body = rawContentTypography.body.copy(
                        fontWeight = FontWeight.Normal,
                        fontUnit = 14.sp lineHeight 20.sp
                    )
                )

                val title by settingViewModel.title.collectAsState("")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                AnimatedContent(
                                    targetState = title,
                                    transitionSpec = {
                                        fadeIn().togetherWith(fadeOut())
                                    }
                                ) {
                                    Text(text = it)
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressedDispatcher.onBackPressed()
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_arrow_back_24),
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    finish()
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_close_24),
                                        contentDescription = stringResource(R.string.close)
                                    )
                                }
                            }
                        )
                    }
                ) {
                    SettingNavHost(
                        navController = navController,
                        paddingValues = it,
                        contentTypography = preferenceContentTypography
                    )
                }
            }
        }
    }

    private fun navigateIfExtras(navController: NavController) {
        if (intent.hasExtra(EXTRA_ROUTE)) {
            val route = intent.getStringExtra(EXTRA_ROUTE) ?: return
            navController.navigate(route)
        }
    }

    private fun setupActivityLauncher() {
        registerActivityLauncher(ActivityResultContracts.OpenDocumentTree())
        registerActivityLauncher(ActivityResultContracts.CreateDocument("*/*"))
        registerActivityLauncher(ActivityResultContracts.OpenDocument())
    }

    @Composable
    private fun SettingNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        paddingValues: PaddingValues = PaddingValues(0.dp),
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
    ) = NavHost(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        navController = navController,
        startDestination = SettingNavigations.ROUTE_MENU,
        enterTransition = {
            fadeIn() + slideInHorizontally { it }
        },
        exitTransition = {
            fadeOut() + slideOutHorizontally { -it }
        },
        popEnterTransition = {
            fadeIn() + slideInHorizontally { -it }
        },
        popExitTransition = {
            fadeOut() + slideOutHorizontally { it }
        }
    ) {
        val screenModifier = Modifier.fillMaxSize()
        composable(route = SettingNavigations.ROUTE_MENU) {
            settingViewModel.setTitle(stringResource(id = R.string.setting))
            SettingMenuScreen(
                navController = navController,
                modifier = screenModifier,
                contentTypography = contentTypography
            )
        }
        composable(route = SettingNavigations.ROUTE_UI) {
            settingViewModel.setTitle(stringResource(id = R.string.setting_ui))
            UIScreen(
                navController = navController,
                modifier = screenModifier,
                contentTypography = contentTypography
            )
        }
        composable(route = SettingNavigations.ROUTE_DEBUG) {
            settingViewModel.setTitle(stringResource(id = R.string.setting_debug))
            DebugScreen(
                navController = navController,
                modifier = screenModifier,
                contentTypography = contentTypography
            )
        }

        composable(route = SettingNavigations.ROUTE_STORAGE) {
            settingViewModel.setTitle(stringResource(id = R.string.setting_storage))
            StorageScreen(
                navController = navController,
                modifier = screenModifier,
                contentTypography = contentTypography
            )
        }

        composable(route = SettingNavigations.ROUTE_AUDIO_DEVICES) {
            settingViewModel.setTitle("Audio Devices")
            AudioDevicesScreen(
                modifier = screenModifier,
                contentTypography = PlayerTheme.typography.contentNormal
            )
        }

        composable(route = SettingNavigations.ROUTE_ANALYTICS_EVENTS) {
            settingViewModel.setTitle("Analytics Events")
            AnalyticsScreen(
                modifier = screenModifier,
                contentTypography = PlayerTheme.typography.contentNormal
            )
        }

        composable(route = SettingNavigations.ROUTE_THEME_VIEWER) {
            settingViewModel.setTitle("Theme Viewer")
            ThemeViewerScreen(
                modifier = screenModifier,
                contentTypography = PlayerTheme.typography.contentNormal
            )
        }

        composable(route = SettingNavigations.ROUTE_BACKGROUND_TASKS) {
            settingViewModel.setTitle("Background Tasks")
            BackgroundTasksScreen(
                modifier = screenModifier,
                contentTypography = PlayerTheme.typography.contentNormal
            )
        }

        composable(
            route = SettingNavigations.ROUTE_INTERNAL_FILE_EXPLORER,
            arguments = listOf(
                navArgument(InternalFileExplorerScreen.EXTRA_PATH) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
        ) {
            settingViewModel.setTitle("Internal File Explorer")
            val path = it.arguments?.getString(InternalFileExplorerScreen.EXTRA_PATH)
            InternalFileExplorerScreen(
                modifier = screenModifier,
                path = path,
                navController = navController,
                contentTypography = PlayerTheme.typography.contentNormal
            )
        }
    }

    companion object {
        private const val TAG = "SettingActivity"

        /**
         * Indicate the route to show.
         */
        const val EXTRA_ROUTE = "SettingActivity.route"
        const val EXTRA_SETTING_KEY = "SettingActivity.setting_key"
    }
}