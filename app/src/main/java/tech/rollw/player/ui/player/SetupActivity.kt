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
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import tech.rollw.player.R
import tech.rollw.player.data.setting.AppSettings
import tech.rollw.player.data.setting.SettingValue
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.support.Switch
import tech.rollw.support.appcompat.AppActivity

/**
 * Start if the user has not set up the app, the user will be asked to
 * set up the app (select the music folder, etc.) and then enter
 * the main interface.
 *
 * @author RollW
 */
class SetupActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar(colorBackground = 0, lightBar = Switch.NONE)
        setNavigationBar(colorBackground = 0, lightBar = Switch.NONE)
        val extras = intent.extras

        val navigateMainActivity = extras?.getBoolean(EXTRA_NAVIGATE_TO_MAIN)
            ?: false

        var appSetup by SettingValue(AppSettings.AppSetup, this)


        setContent {
            SoundSourceTheme {
                SetupScreen(
                    modifier = Modifier.fillMaxSize(),
                    contentTypography = PlayerTheme.typography.contentLarge
                ) {
                    appSetup = true

                    if (navigateMainActivity) {
                        startMainActivity()
                    }
                    finish()
                }
            }
        }
    }

    private fun startMainActivity() {
        val mainActivityIntent = getOrCreateActivityIntent(MainActivity::class.java)
        startActivity(mainActivityIntent)
        Intent.EXTRA_INTENT
    }

    companion object {
        private const val PAGER_COUNT = 4

        const val EXTRA_NAVIGATE_TO_MAIN = "tech.rollw.player.NAVIGATE_TO_MAIN"
    }

    @Composable
    private fun SetupScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onFinish: () -> Unit = {}
    ) {
        var currentPage by remember { mutableIntStateOf(0) }
        val pagerState = rememberPagerState(
            initialPage = 0
        ) {
            PAGER_COUNT
        }

        LaunchedEffect(currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }

        val onClickPrev: () -> Unit = onClickPrev@{
            if (currentPage == 0) {
                onFinish()
                return@onClickPrev
            }

            if (currentPage > 0) {
                currentPage--
            }
        }

        LaunchedEffect(Unit) {
            onBackPressedDispatcher.addCallback(owner = this@SetupActivity) {
                onClickPrev()
            }
        }

        val onClickContinue: () -> Unit = onClickContinue@{
            if (currentPage == PAGER_COUNT - 1) {
                onFinish()
                return@onClickContinue
            }

            if (currentPage < PAGER_COUNT - 1) {
                currentPage++
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = modifier,
            userScrollEnabled = false,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (it) {
                0 -> WelcomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp, vertical = 40.dp),
                    contentTypography = contentTypography,
                    onClickContinue = onClickContinue,
                    onClickPrev = onClickPrev
                )

                1 -> PermissionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp, vertical = 40.dp),
                    contentTypography = contentTypography,
                    onClickContinue = onClickContinue,
                    onClickPrev = onClickPrev
                )

                2 -> AudioScanScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp, vertical = 40.dp),
                    contentTypography = contentTypography,
                    onClickContinue = onClickContinue,
                    onClickPrev = onClickPrev
                )

                3 -> EndSetupScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp, vertical = 40.dp),
                    contentTypography = contentTypography,
                    onClickContinue = onClickContinue,
                    onClickPrev = onClickPrev
                )
            }
        }
    }

    /**
     * The Welcome screen shows the app logo and a button to start the setup process.
     */
    @Composable
    private fun WelcomeScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue,
            backLabel = stringResource(R.string.skip)
        ) {
            Image(
                painter = BitmapPainter(
                    ResourcesCompat.getDrawable(resources, R.mipmap.ic_logo, null)!!
                        .toBitmap()
                        .asImageBitmap()
                ),
                contentDescription = "Logo",
                modifier = Modifier
                    .padding(top = 40.dp)
                    .size(200.dp)
            )

            Text(
                text = stringResource(R.string.setup_welcome_title),
                modifier = Modifier.padding(top = 40.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.title
            )

            Text(
                text = stringResource(R.string.setup_welcome_desc),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.body
            )
        }
    }

    @Composable
    private fun PermissionScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue
        ) {
            Text(
                text = stringResource(R.string.setup_permission_title),
                modifier = Modifier.padding(top = 40.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.title
            )

            Text(
                text = stringResource(R.string.setup_permission_desc),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.body
            )

            Column(
                modifier = Modifier.padding(top = 40.dp),
            ) {
                PermissionItem(
                    title = stringResource(R.string.setup_permission_storage_title),
                    description = stringResource(R.string.setup_permission_storage_desc),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    onClick = {}
                )
                PermissionItem(
                    title = stringResource(R.string.setup_permission_notification_title),
                    description = stringResource(R.string.setup_permission_notification_desc),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    onClick = {}
                )
            }
        }
    }

    @Composable
    private fun AudioScanScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue
        ) {
            Text(
                text = stringResource(R.string.setup_audio_scan_title),
                modifier = Modifier.padding(top = 40.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.title
            )
            Text(
                text = stringResource(R.string.setup_audio_scan_desc),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.body
            )
        }
    }

    @Composable
    private fun EndSetupScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue,
            continueLabel = stringResource(R.string.complete)
        ) {
            Text(
                text = stringResource(R.string.setup_complete_title),
                modifier = Modifier.padding(top = 40.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.title
            )

            Text(
                text = stringResource(R.string.setup_complete_desc),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center,
                style = contentTypography.body
            )
        }
    }

    @Composable
    private fun SetupScreenLayout(
        modifier: Modifier = Modifier,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {},
        backLabel: String = stringResource(R.string.back),
        continueLabel: String = stringResource(R.string.scontinue),
        content: @Composable ColumnScope.() -> Unit
    ) {
        ConstraintLayout(
            modifier = modifier
        ) {
            val (contentRef, bottomButtonRef) = createRefs()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(contentRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomButtonRef.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }

            BottomNavigateButtons(
                onClickContinue = onClickContinue,
                onClickPrev = onClickPrev,
                backLabel = backLabel,
                continueLabel = continueLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .constrainAs(bottomButtonRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }
    }

    @Composable
    private fun PermissionItem(
        title: String,
        description: String,
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClick: () -> Unit = {}
    ) {
        Row(
            modifier = modifier
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Start,
                    style = contentTypography.title
                )
                Text(
                    text = description,
                    modifier = Modifier.padding(top = 10.dp),
                    textAlign = TextAlign.Start,
                    style = contentTypography.body
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 10.dp)
            )
            FilledTonalButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onClick
            ) {
                Text(
                    text = stringResource(R.string.allow)
                )
            }
        }

    }

    @Composable
    private fun BottomNavigateButtons(
        onClickContinue: () -> Unit,
        onClickPrev: () -> Unit,
        modifier: Modifier = Modifier,
        backLabel: String = stringResource(R.string.back),
        continueLabel: String = stringResource(R.string.scontinue)
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onClickPrev
            ) {
                Text(
                    text = backLabel
                )
            }

            FilledTonalButton(
                onClick = onClickContinue
            ) {
                Text(
                    text = continueLabel
                )
            }
        }
    }
}