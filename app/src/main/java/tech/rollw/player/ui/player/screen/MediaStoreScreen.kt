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

package tech.rollw.player.ui.player.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.rollw.player.R
import tech.rollw.player.ui.components.RoundedRow
import tech.rollw.player.ui.player.AudioListParam
import tech.rollw.player.ui.player.model.MediaStoreModel
import java.time.LocalDateTime

/**
 * @author RollW
 */
@Composable
fun MediaStoreScreen(
    statusBarHeight: Int = 0,
    onNavigateToList: (MediaStoreModel) -> Unit = {}
) {
    val mediaStoreModels = getMediaStoreModels()
    MediaStoreList(
        mediaStoreModels,
        statusBarHeight = statusBarHeight
    ) {
        onNavigateToList.invoke(it)
    }
}

@Composable
private fun MediaStoreList(
    mediaStoreModels: List<MediaStoreModel>,
    modifier: Modifier = Modifier,
    statusBarHeight: Int = 0,
    onItemClicked: (MediaStoreModel) -> Unit = {}
) {
    var active by remember {
        mutableStateOf<MediaStoreModel?>(null)
    }
    // TODO: apply theme color/typography

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) {
                        statusBarHeight.toDp()
                    })
            )
        }
        item {
            Row(
                modifier = Modifier.padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.media_store),
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        // TODO: load username from setting
                        text = loadGreetingText(),
                        fontSize = 14.sp,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {}
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.ic_baseline_search_24
                            ),
                            contentDescription = "Options",
                        )
                    }
                }
            }
        }

        items(mediaStoreModels, { it.identifier }, { MediaStoreModel::class }) { item ->
            MediaStoreItem(
                item,
                modifier = Modifier.clickable {
                    if (active == null) {
                        active = item
                    }
                }
            )
        }
    }
    active?.let {
        onItemClicked.invoke(it)
        active = null
    }
}

@Composable
fun MediaStoreItem(
    data: MediaStoreModel,
    modifier: Modifier = Modifier
) {
    RoundedRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = modifier.fillMaxWidth()) {
            Image(
                imageVector = ImageVector.vectorResource(id = data.icon),
                contentDescription = null,
                modifier = Modifier.padding(20.dp)
            )
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = data.name, fontSize = 16.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

}

@Composable
private fun loadGreetingText(username: String = "user"): String {
    return when (LocalDateTime.now().hour) {
        in 0..4 -> stringResource(R.string.greeting_dawn, username)
        in 5..9 -> stringResource(R.string.greeting_morning, username)
        in 10..13 -> stringResource(R.string.greeting_noon, username)
        in 14..18 -> stringResource(R.string.greeting_afternoon, username)
        in 18..21 -> stringResource(R.string.greeting_evening, username)
        in 22..24 -> stringResource(R.string.greeting_night, username)
        else -> stringResource(R.string.greeting_default, username)
    }
}

@Composable
private fun getMediaStoreModels() =
    listOf(
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            1, AudioListParam.ALL_MUSIC
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            2, AudioListParam.ALBUM
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            3, AudioListParam.ARTIST
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            4, AudioListParam.FOLDER
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            5, AudioListParam.PLAYLIST
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            6, AudioListParam.RECENTLY_ADDED
        ),
    )

@Composable
private fun ofMediaStoreModel(
    icon: Int,
    identifier: Int,
    param: AudioListParam
) = MediaStoreModel(
    stringResource(param.resId), icon,
    identifier, param
)