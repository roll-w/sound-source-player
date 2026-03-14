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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.rollw.player.R
import tech.rollw.player.data.setting.UserSettings
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.components.RoundedRow
import tech.rollw.player.ui.player.AudioListType
import tech.rollw.player.ui.player.model.MediaStoreModel
import tech.rollw.player.ui.theme.LocalContainerOpacity
import tech.rollw.player.ui.tools.rememberSetting
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
    val username by rememberSetting(UserSettings.Username)

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
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.media_library),
                        style = PlayerTheme.typography.contentMedium.header
                    )
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                    Text(
                        text = loadGreetingText(username ?: "user"),
                        style = PlayerTheme.typography.contentMedium.body
                    )
                }

                Row(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val defaultColors = IconButtonDefaults.filledTonalIconButtonColors()

                    val colors = defaultColors.copy(
                        containerColor = defaultColors.containerColor.copy(
                            alpha = LocalContainerOpacity.current
                        )
                    )

                    FilledTonalIconButton(
                        onClick = {},
                        shape = RoundedCornerShape(25),
                        colors = colors
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.ic_baseline_search_24
                            ),
                            contentDescription = stringResource(id = R.string.search),
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

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = PlayerTheme.typography.contentMedium.title,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 5.dp
                    )
                )
                StatisticsView(
                    modifier = Modifier
                        .padding(10.dp)
                        .heightIn(
                            max = 1000.dp
                        )
                )
            }

        }

        item {
            Spacer(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
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
                    style = PlayerTheme.typography.contentNormal.subtitle
                )
            }
        }
    }

}

@Composable
private fun RecommendationView(
    modifier: Modifier = Modifier,
) {
    val state = rememberCarouselState {
        10
    }

    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = 200.dp,
        itemSpacing = 20.dp
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
            }) {
            Image(
                painter = BitmapPainter(
                    image = ImageBitmap.imageResource(
                        id = R.mipmap.ic_logo
                    )
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(Color.Gray, BlendMode.SrcOver),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Item $it",
                modifier = Modifier.padding(10.dp)
            )
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
            1, AudioListType.ALL_SONGS
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            2, AudioListType.ALBUM
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            3, AudioListType.ARTIST
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            4, AudioListType.FOLDER
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            5, AudioListType.PLAYLIST
        ),
        ofMediaStoreModel(
            R.drawable.ic_baseline_menu_24,
            6, AudioListType.RECENTLY_ADDED
        ),
    )

@Composable
private fun ofMediaStoreModel(
    icon: Int,
    identifier: Int,
    param: AudioListType
) = MediaStoreModel(
    stringResource(param.resId), icon,
    identifier, param
)