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
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import tech.rollw.player.BuildConfig
import tech.rollw.player.R
import tech.rollw.player.data.setting.AppSettings
import tech.rollw.player.data.setting.DebugSettings
import tech.rollw.player.data.setting.SettingValue
import tech.rollw.player.databinding.ActivitySplashBinding
import tech.rollw.player.ui.player.MainActivity
import tech.rollw.player.ui.player.SetupActivity
import tech.rollw.support.appcompat.AppActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppActivity() {
    private lateinit var binding: ActivitySplashBinding

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

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBar(Color.TRANSPARENT, true)
        window.navigationBarColor = Color.TRANSPARENT

        setupView()
        // overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)

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


    @SuppressLint("SetTextI18n")
    private fun setupView() {
        val logo = ResourcesCompat.getDrawable(resources, R.mipmap.ic_logo, null)
        val currentWidth = window.decorView.width
        val screenImage = binding.activitySplashScreenImage

        screenImage.apply {
            visibility = View.VISIBLE
            alpha = 0f
            val animator = animate().alpha(1f)
            animator.duration = 1000L
            animator.interpolator = AccelerateDecelerateInterpolator()

            adjustViewBounds = true
            maxWidth = currentWidth / 2
            setBackgroundColor(Color.TRANSPARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageDrawable(logo)
        }

        val screenText = binding.activitySplashScreenText
        screenText.apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            typeface = Typeface.DEFAULT
            text = "${getString(R.string.splash_copyright)} ${BuildConfig.VERSION_NAME}"
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