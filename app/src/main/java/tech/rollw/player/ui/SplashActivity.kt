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

package tech.rollw.player.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import tech.rollw.player.BuildConfig
import tech.rollw.player.R
import tech.rollw.player.data.setting.AppSettings
import tech.rollw.player.data.setting.DebugSettings
import tech.rollw.player.data.setting.SettingValue
import tech.rollw.player.ui.player.MainActivity
import tech.rollw.player.ui.player.SetupActivity
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.support.appcompat.AppActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val alphaAnimator = ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.ALPHA,
                1f,
                0f
            )
            alphaAnimator.interpolator = AccelerateInterpolator()
            alphaAnimator.duration = 500L
            alphaAnimator.doOnEnd {
                splashScreenViewProvider.remove()
            }
            alphaAnimator.start()
        }

        setStatusBar(Color.TRANSPARENT, true)
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            SoundSourceTheme {
                Content(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (isShowSetup()) {
            startActivity(Intent(this, SetupActivity::class.java).apply {
                putExtra(SetupActivity.EXTRA_NAVIGATE_TO_MAIN, true)
            }, DURATION)
            return
        }

        val source = intent?.getStringExtra(EXTRA_SOURCE)
        val mainActivityIntent = getOrCreateActivityIntent(MainActivity::class.java)
        when (source) {
            SOURCE_NOTIFICATION,
            SOURCE_WIDGET,
            SOURCE_SHORTCUT,
            SOURCE_TILE -> {
                startActivity(mainActivityIntent, 0)
                return
            }

            else -> startActivity(mainActivityIntent, DURATION)
        }
    }

    private fun isShowSetup(): Boolean {
        val alwaysShowSetup by SettingValue(DebugSettings.AlwaysShowSetup, this)
        if (alwaysShowSetup == true) {
            return true
        }
        val appSetup by SettingValue(AppSettings.AppSetup, this)
        if (appSetup == null) {
            return true
        }
        return !appSetup!!
    }

    private fun startActivity(intent: Intent, delay: Long = DURATION) {
        fun start() {
            startActivity(intent)
            this@SplashActivity.finish()
        }

        if (delay <= 0) {
            start()
            return
        }

        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                start()
            }, delay)
        }
    }

    @Composable
    private fun Content(
        modifier: Modifier = Modifier,
    ) {
        var size by remember { mutableStateOf(IntSize.Zero) }

        val stdWidth by remember {
            derivedStateOf {
                size.width.coerceAtMost(size.height)
            }
        }

        val alpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 5000,
                easing = LinearOutSlowInEasing
            )
        )

        ConstraintLayout(
            modifier = modifier
                .alpha(alpha)
                .onGloballyPositioned {
                    size = it.size
                },
        ) {
            val (contentRef, bottomTextRef) = createRefs()
            Image(
                modifier = Modifier
                    .constrainAs(contentRef) {
                        linkTo(parent.start, parent.end)
                        linkTo(parent.top, parent.bottom)
                    }
                    .width(with(LocalDensity.current) {
                        stdWidth.toDp()
                    })
                    .height(with(LocalDensity.current) {
                        stdWidth.toDp()
                    }),
                painter = painterResource(id = R.mipmap.ic_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )

            Text(
                text = "${getString(R.string.splash_copyright)} ${BuildConfig.VERSION_NAME}",
                modifier = Modifier
                    .constrainAs(bottomTextRef) {
                        linkTo(parent.start, parent.end)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(bottom = 26.dp, start = 16.dp, end = 16.dp),
                style = PlayerTheme.typography.contentNormal.info,
                color = PlayerTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }

    companion object {
        private const val DURATION = 1500L

        const val EXTRA_SOURCE = "source"

        const val SOURCE_NOTIFICATION = "notification"
        const val SOURCE_WIDGET = "widget"
        const val SOURCE_SHORTCUT = "shortcut"
        const val SOURCE_TILE = "tile"
    }
}