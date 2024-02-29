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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest

/**
 * Provide a background for the player.
 */
@Composable
fun ImageBackground(
    painter: Painter,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}

@Composable
fun ColorBackground(
    modifier: Modifier = Modifier,
    color: Color = Color.Transparent,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = color,
            ),
    ) {
        content()
    }
}

/**
 * ImageBackground that uses [AsyncImage] to load the image.
 */
@Composable
fun AsyncImageBackground(
    imageRequest: ImageRequest,
    modifier: Modifier = Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    colorOverlay: Color = Color.Unspecified,
    colorOverlayAlpha: Float = DefaultAlpha,
    content: @Composable () -> Unit = {}
) = Background(
    modifier = modifier,
    content = content,
    background = {
        ColorFilterOverlay(
            color = colorOverlay,
            alpha = colorOverlayAlpha
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                transform = transform,
                onState = onState,
                alignment = alignment,
                alpha = 1f,
                colorFilter = colorFilter,
                filterQuality = filterQuality
            )
        }

    }
)


@Composable
fun Background(
    modifier: Modifier = Modifier,
    background: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        background()
        content()
    }
}

@Composable
fun ColorFilterOverlay(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
    alpha: Float = 0F,
    content: @Composable () -> Unit
) {
    if (color == Color.Unspecified) {
        content()
        return
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        content()
        Image(
            painter = ColorPainter(Color.White),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = alpha,
            colorFilter = ColorFilter.tint(
                color,
                blendMode
            ),
            modifier = Modifier.fillMaxSize()
                .alpha(alpha)
        )
    }
}
