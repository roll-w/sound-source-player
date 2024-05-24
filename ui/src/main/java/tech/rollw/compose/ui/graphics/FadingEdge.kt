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

package tech.rollw.compose.ui.graphics

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

fun interface FadingEdge {
    fun createBrush(): Brush

    companion object {
        fun vertical(
            topBias: Float,
            bottomBias: Float
        ): FadingEdge = VerticalFadingEdge(topBias, bottomBias)

        fun vertical(
            bias: Float = 0.15f
        ): FadingEdge = VerticalFadingEdge(bias, bias)

        fun horizontal(
            startBias: Float = 0.15f,
            endBias: Float = startBias
        ): FadingEdge = HorizontalFadingEdge(startBias, endBias)

        fun horizontal(
            bias: Float = 0.15f
        ): FadingEdge = HorizontalFadingEdge(bias, bias)
    }
}

internal class VerticalFadingEdge(
    private val topBias: Float,
    private val bottomBias: Float
) : FadingEdge {
    override fun createBrush(): Brush = Brush.verticalGradient(
        0f to Color.Transparent,
        topBias to Color.White,
        1f - bottomBias to Color.White,
        1f to Color.Transparent
    )
}

internal class HorizontalFadingEdge(
    private val startBias: Float,
    private val endBias: Float,
) : FadingEdge {
    override fun createBrush(): Brush = Brush.horizontalGradient(
        0f to Color.Transparent,
        startBias to Color.White,
        1f - endBias to Color.White,
        1f to Color.Transparent
    )
}

/**
 *  Apply a fading-edge effect to the content of this [Modifier].
 *
 *  @param fadingEdge The fading-edge configuration.
 */
fun Modifier.fadingEdge(fadingEdge: FadingEdge) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = fadingEdge.createBrush(), blendMode = BlendMode.DstIn)
    }