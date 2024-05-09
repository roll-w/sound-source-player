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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import tech.rollw.player.ui.PlayerAppActivity
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.support.Switch

/**
 * @author RollW
 */
class MainActivity : PlayerAppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBar(
            colorBackground = 0,
            lightBar = Switch.ON
        )

        setContent {
            val navController = rememberNavController()

            SoundSourceTheme {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    companion object {
    }
}
