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

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import tech.rollw.player.Switch
import tech.rollw.support.appcompat.AppActivity

abstract class BaseActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setViewStatusBarHeight(view: View) {
        view.layoutParams.height = getStatusBarHeight()
    }

    protected fun setStatusBar(
        @ColorInt colorBackground: Int = COLOR_DEFAULT,
        fullScreen: Boolean = false,
        lightStatusBar: Switch = Switch.AUTO
    ) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        WindowCompat.setDecorFitsSystemWindows(window, fullScreen)
        if (colorBackground != COLOR_DEFAULT) {
            window.statusBarColor = colorBackground
        }
        val lightness = if (colorBackground == COLOR_DEFAULT) 0.0 else
            computeColorLightness(colorBackground)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            val lightBarState = when (lightStatusBar) {
                Switch.ON -> true
                Switch.NONE, Switch.OFF -> false
                Switch.AUTO -> lightness >= 0.5f
            }
            if (lightStatusBar != Switch.NONE) {
                isAppearanceLightStatusBars = lightBarState
                isAppearanceLightNavigationBars = lightBarState
            }

            if (fullScreen) {
                hide(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    private fun computeColorLightness(@ColorInt color: Int): Double {
        if (color == Color.TRANSPARENT) {
            return 0.0
        }
        return ColorUtils.calculateLuminance(color)
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier(
            "status_bar_height", "dimen",
            "android"
        )
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    companion object {
        const val COLOR_DEFAULT = -1
    }
}
