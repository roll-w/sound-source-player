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

package tech.rollw.player.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.support.io.FileType
import tech.rollw.support.io.Files.fileType
import java.io.File

// TODO: supports more file types and provides more configurations.
/**
 * A Composable to view a file.
 */
@Composable
fun FileViewer(
    file: File,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    if (!file.exists() || !file.isFile) {
        Text(
            text = "File not found.",
            modifier = modifier
        )
        return
    }

    val fileType = file.fileType()
    when (fileType) {
        FileType.OTHER,
        FileType.TEXT -> {
            TextFileViewer(
                file = file,
                modifier = modifier,
                style = contentTypography.code
            )
        }

        else -> {
            Text(
                text = "Unsupported file type.",
                modifier = modifier
            )
        }
    }
}

@Composable
fun TextFileViewer(
    file: File,
    modifier: Modifier = Modifier,
    style: TextStyle = PlayerTheme.typography.contentNormal.code
) {
    // TODO: free scroll
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()

    val text = try {
        file.readText()
    } catch (e: Exception) {
        "Error: Cannot read file. ${e.message}"
    }

    SelectionContainer {
        Text(
            text = text,
            style = style,
            modifier = modifier
                .verticalScroll(vScrollState)
                .horizontalScroll(hScrollState)
        )
    }
}
