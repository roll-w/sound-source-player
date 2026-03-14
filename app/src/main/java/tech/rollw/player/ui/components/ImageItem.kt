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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import tech.rollw.player.R
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.model.ImageItemModel

/**
 * @author RollW
 */
object ImageItemDefaults {
    internal val ImageSize = 120.dp
    internal val ImageShape = RoundedCornerShape(15)
    internal val ImagePadding = 10.dp
    internal val TextPadding = 6.dp

    @Composable
    fun typography(
        title: ImageItemTextStyle = PlayerTheme.typography
            .contentNormal.title.itemTextStyle(),
        subtitle: ImageItemTextStyle = PlayerTheme.typography
            .contentNormal.subtitle.itemTextStyle(),
        info: ImageItemTextStyle = PlayerTheme.typography
            .contentNormal.tip.itemTextStyle()
    ) = ImageItemTypography(
        title = title,
        subtitle = subtitle,
        info = info
    )
}

private fun TextStyle.itemTextStyle(
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) = ImageItemTextStyle(
    textStyle = this,
    maxLines = maxLines,
    overflow = overflow
)

@Immutable
data class ImageItemTypography(
    val title: ImageItemTextStyle,
    val subtitle: ImageItemTextStyle,
    val info: ImageItemTextStyle
) {
    companion object {
        fun fromContentTypography(
            contentTypography: ContentTypography
        ): ImageItemTypography {
            return ImageItemTypography(
                title = contentTypography.title.itemTextStyle(),
                subtitle = contentTypography.subtitle.itemTextStyle(),
                info = contentTypography.info.itemTextStyle()
            )
        }
    }
}

@Immutable
data class ImageItemTextStyle(
    val textStyle: TextStyle,
    val maxLines: Int = 1,
    val overflow: TextOverflow = TextOverflow.Ellipsis
)

@Composable
fun ImageItem(
    model: ImageItemModel,
    modifier: Modifier = Modifier,
    showCheckbox: Boolean = false,
    selected: Boolean = false,
    onSelectChanged: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Transparent,
    typography: ImageItemTypography = ImageItemDefaults.typography(),
    imageBuilderConfigure: ImageRequest.Builder.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model.image)
                    .apply(imageBuilderConfigure)
                    .build(),
                contentDescription = null,
                loading = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.25f),
                        tint = PlayerTheme.colorScheme.onSurface
                    )
                },
                error = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.25f),
                        tint = PlayerTheme.colorScheme.onSurface
                    )
                },
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .padding(ImageItemDefaults.ImagePadding)
                    .size(ImageItemDefaults.ImageSize)
                    .clip(ImageItemDefaults.ImageShape),
            )

            // TODO: when kotlin fixed, remove the full package name
            // a bug with kotlin compiler, needs to use full package name
            androidx.compose.animation.AnimatedVisibility(
                visible = showCheckbox,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Column(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(100))
                        .background(PlayerTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = onSelectChanged
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(ImageItemDefaults.ImagePadding)
                .fillMaxHeight(),
        ) {
            Text(
                text = model.title,
                maxLines = typography.title.maxLines,
                overflow = typography.title.overflow,
                style = typography.title.textStyle,
                modifier = Modifier.padding(bottom = ImageItemDefaults.TextPadding)
            )
            AnimatedVisibility(model.subtitle != null) {
                Text(
                    text = model.subtitle ?: "",
                    maxLines = typography.subtitle.maxLines,
                    overflow = typography.subtitle.overflow,
                    style = typography.subtitle.textStyle,
                    modifier = Modifier.padding(
                        bottom = ImageItemDefaults.TextPadding + 2.dp
                    )
                )
            }
            AnimatedVisibility(model.info != null) {
                Text(
                    text = model.info ?: "",
                    maxLines = typography.info.maxLines,
                    overflow = typography.info.overflow,
                    style = typography.info.textStyle
                )
            }
        }
    }
}
