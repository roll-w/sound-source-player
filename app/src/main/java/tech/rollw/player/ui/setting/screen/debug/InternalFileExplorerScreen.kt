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

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.components.FileViewer
import tech.rollw.player.ui.setting.SettingNavigations
import tech.rollw.player.util.formatTimestamp
import tech.rollw.support.io.Files.formatSize
import java.io.File

/**
 * @author RollW
 */
object InternalFileExplorerScreen  {
    const val EXTRA_PATH = "path"
}

@Composable
fun InternalFileExplorerScreen(
    modifier: Modifier = Modifier,
    path: String? = null,
    navController: NavController = rememberNavController(),
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    Column(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Internal File Explorer",
                style = contentTypography.header,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = path ?: "Root",
                style = contentTypography.subtitle,
            )
        }

        FileExploreScreen(path, contentTypography = contentTypography) {
            navController.navigate(
                SettingNavigations.buildInternalFileExplorerRoute(
                    Uri.encode(it.path)
                )
            )
        }
    }
}

private fun loadFiles(context: Context, path: String? = null): List<FileItem> {
    if (path == null) {
        return loadRoot(context)
    }
    val file = File(path)
    if (!file.exists() || !file.isDirectory) {
        return emptyList()
    }
    val files = file.listFiles() ?: return emptyList()
    return files.map {
        FileItem(
            name = it.name,
            path = it.absolutePath,
            size = it.length(),
            lastModified = it.lastModified()
        )
    }
}

private fun loadRoot(context: Context): List<FileItem> {
    val files = mutableListOf<FileItem>()
    val root = context.filesDir.parentFile
    if (root != null) {
        files.add(
            FileItem(
                name = root.name,
                path = root.absolutePath,
                lastModified = root.lastModified()
            )
        )
    }
    val external = context.externalCacheDir?.parentFile
    if (external != null) {
        files.add(
            FileItem(
                name = external.name,
                path = external.absolutePath,
                size = external.length(),
                lastModified = external.lastModified()
            )
        )
    }
    return files
}

@Composable
private fun FileExploreScreen(
    path: String?,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onClick: (FileItem) -> Unit = {}
) {
    val context = LocalContext.current

    val files = loadFiles(context, path)
    val file = if (path != null) {
        File(path)
    } else null

    if (files.isEmpty() && (file == null || file.isDirectory)) {
        Text(
            text = "No files found.",
            modifier = modifier.fillMaxSize(),
            style = contentTypography.title,
            textAlign = TextAlign.Center
        )
        return
    }
    if (file != null && file.isFile) {
        FileDetailsView(
            file,
            contentTypography = contentTypography
        )
        return
    }

    FileListView(
        files = files,
        modifier = modifier,
        onClick = onClick,
        contentTypography = contentTypography
    )
}

@Composable
private fun FileListView(
    files: List<FileItem>,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onClick: (FileItem) -> Unit = {}
) {
    LazyColumn(modifier = modifier) {
        items(files, { it.path }) { file ->
            FileItemView(
                file = file,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                contentTypography = contentTypography
            )
        }
    }
}

@Composable
private fun FileItemView(
    file: FileItem,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onClick: (FileItem) -> Unit = {}
) {
    Column(modifier = modifier
        .clickable { onClick(file) }
        .padding(20.dp)
    ) {
        Text(
            text = file.name,
            style = contentTypography.title,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = file.path,
            style = contentTypography.body,
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = "${file.size.formatSize()} | ${file.lastModified.formatTimestamp()}",
            style = contentTypography.info,
        )
    }
}

@Composable
private fun FileDetailsView(
    file: File,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    if (!file.exists()) {
        Text(
            text = "File not found.",
            modifier = modifier.fillMaxSize(),
            style = contentTypography.title,
            textAlign = TextAlign.Center,
        )
        return
    }

    FileViewer(
        file = file,
        modifier = Modifier.padding(horizontal = 20.dp),
        contentTypography = contentTypography
    )
}

private data class FileItem(
    val name: String,
    val path: String,
    val size: Long = 0,
    val lastModified: Long = 0
)
