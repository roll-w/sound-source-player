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

package tech.rollw.player.ui.setting.screen.debug

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.rollw.compose.ui.text.FontUnit
import tech.rollw.compose.ui.text.FontUnit.Companion.lineHeight
import tech.rollw.compose.ui.text.copy
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme

/**
 * @author RollW
 */
@Composable
fun ThemeViewerScreen(
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    // TODO: view all styles (such as typography, colors, shapes, etc.)

    var fontSize by remember { mutableStateOf(14.sp) }
    var lineHeight by remember { mutableStateOf(20.sp) }

    val fontUnit by remember {
        derivedStateOf {
            fontSize lineHeight lineHeight
        }
    }

    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .border(1.dp, Color.Gray)
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            FontUnitViewer(
                fontUnit = fontUnit,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Font Size: ${fontSize.value}")
            Slider(
                value = fontSize.value,
                onValueChange = { fontSize = TextUnit(it, TextUnitType.Sp) },
                valueRange = 8f..50f,
                steps = 41
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Line Height: ${lineHeight.value}")
            Slider(
                value = lineHeight.value,
                onValueChange = { lineHeight = TextUnit(it, TextUnitType.Sp) },
                valueRange = 8f..60f,
                steps = 51
            )
        }
    }
}

@Composable
private fun FontUnitViewer(
    fontUnit: FontUnit,
    modifier: Modifier = Modifier
) {
    val textStyle = LocalTextStyle.current
    Column(modifier = modifier) {
        Text(
            text = "Size: ${fontUnit.textSize}, Line Height: ${fontUnit.lineHeight}",
            style = textStyle,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "A fox jumps over the lazy dog, 0123456789.",
            style = textStyle.copy(fontUnit = fontUnit)
        )
    }
}

@Composable
private fun ResizablePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        content()
    }
}
