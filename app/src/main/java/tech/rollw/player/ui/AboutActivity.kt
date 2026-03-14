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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import tech.rollw.player.BuildConfig
import tech.rollw.player.R
import tech.rollw.player.data.setting.DebugSettings
import tech.rollw.player.data.setting.SettingValue
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.support.Switch
import tech.rollw.support.appcompat.AppActivity

/**
 * @author RollW
 */
class AboutActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar(
            colorBackground = 0,
            lightBar = Switch.NONE
        )
        setNavigationBar(
            colorBackground = 0,
            lightBar = Switch.NONE
        )

        var debugEnabled by SettingValue(DebugSettings.DebugEnabled, this)

        setContent {
            SoundSourceTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.about),
                                    fontSize = 20.sp
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        finish()
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(
                                            R.drawable.ic_baseline_arrow_back_24
                                        ),
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            }
                        )
                    },
                ) {
                    var clickCount by remember { mutableIntStateOf(0) }

                    LaunchedEffect(clickCount) {
                        if (clickCount == 10) {
                            toast("Debug mode unlocked.")
                            clickCount = 0
                            debugEnabled = true
                            return@LaunchedEffect
                        }

                        if (10 - clickCount <= 3) {
                            toast("Click ${10 - clickCount} more times to unlock Debug mode.")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(it),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = BitmapPainter(
                                ResourcesCompat.getDrawable(resources, R.mipmap.ic_logo, null)!!
                                    .toBitmap()
                                    .asImageBitmap()
                            ),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .size(200.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    }
                                ) {
                                    if (debugEnabled != true) {
                                        clickCount++
                                    }
                                }
                        )
                        Text(
                            text = "${getString(R.string.app_name)}\n${BuildConfig.VERSION_NAME}",
                            modifier = Modifier.padding(top = 20.dp),
                            textAlign = TextAlign.Center,
                            style = PlayerTheme.typography.contentLarge.title
                        )
                        Text(
                            text = "${BuildConfig.BUILD_TYPE}\nABI: ${BuildConfig.ABI}",
                            modifier = Modifier.padding(top = 20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}