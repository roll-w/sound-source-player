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

package tech.rollw.player.ui.setting.preferences

import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.LocalContentTypography
import tech.rollw.player.ui.PlayerTheme

/**
 * @param state The state that holds the final value of the slider.
 * @param sliderState The state that holds the current value of the slider.
 */
fun PreferenceScreenScope.sliderPreference(
    state: MutableState<Float>,
    title: @Composable (Float) -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    sliderState: MutableState<Float> = mutableFloatStateOf(state.value),
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    shape: Shape = RectangleShape,
    icon: @Composable ((Float) -> Unit)? = null,
    summary: @Composable ((Float) -> Unit)? = null,
    bottomWidget: @Composable ((Float) -> Unit)? = null,
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = remember { MutableInteractionSource() },
            colors = SliderDefaults.colors(),
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = SliderDefaults.colors(),
            enabled = enabled,
            sliderState = sliderState
        )
    }
) {
    item(key = key, contentType = "SliderPreference") {
        val contentTypography = LocalContentTypography.current
        val value by state

        SliderPreference(
            state = state,
            title = { title(value) },
            modifier = modifier,
            sliderState = sliderState,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            padding = padding,
            contentTypography = contentTypography,
            shape = shape,
            icon = icon?.let { { icon(value) } },
            summary = summary?.let { { summary(value) } },
            bottomWidget = bottomWidget?.let { { bottomWidget(value) } },
            thumb = thumb,
            track = track
        )
    }
}

/**
 * @param state The state that holds the final value of the slider.
 * @param sliderState The state that holds the current value of the slider.
 */
@Composable
fun SliderPreference(
    state: MutableState<Float>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    sliderState: MutableState<Float> = remember { mutableFloatStateOf(state.value) },
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    bottomWidget: @Composable (() -> Unit)? = null,
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = remember { MutableInteractionSource() },
            colors = SliderDefaults.colors(),
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = SliderDefaults.colors(),
            enabled = enabled,
            sliderState = sliderState
        )
    }
) {
    var value by state
    var sliderValue by sliderState

    SliderPreference(
        value = value,
        onValueChange = { value = it },
        sliderValue = sliderValue,
        onSliderValueChange = { sliderValue = it },
        title = title,
        modifier = modifier,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        padding = padding,
        contentTypography = contentTypography,
        shape = shape,
        icon = icon,
        summary = summary,
        bottomWidget = bottomWidget,
        thumb = thumb,
        track = track
    )
}

@Composable
fun SliderPreference(
    value: Float,
    onValueChange: (Float) -> Unit,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    enabled: Boolean = true,
    padding: PaddingValues = PreferenceDefaults.PaddingValues,
    contentTypography: ContentTypography = PlayerTheme.typography.contentMedium,
    shape: Shape = RectangleShape,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    bottomWidget: @Composable (() -> Unit)? = null,
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = remember { MutableInteractionSource() },
            colors = SliderDefaults.colors(),
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = SliderDefaults.colors(),
            enabled = enabled,
            sliderState = sliderState
        )
    }
) {
    var lastValue by remember { mutableFloatStateOf(value) }

    SideEffect {
        if (value != lastValue) {
            onSliderValueChange(value)
            lastValue = value
        }
    }

    Preference(
        title = title,
        summary = summary,
        shape = shape,
        bottomWidget = {
            Column {
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                ) {
                    var latestSliderValue = sliderValue
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            onSliderValueChange(it)
                            latestSliderValue = it
                        },
                        modifier = Modifier
                            .padding(vertical = 5.dp),
                        enabled = enabled,
                        valueRange = valueRange,
                        steps = steps,
                        onValueChangeFinished = { onValueChange(latestSliderValue) },
                        thumb = thumb,
                        track = track
                    )
                }
                bottomWidget?.invoke()
            }
        },
        modifier = modifier,
        enabled = enabled,
        padding = padding,
        contentTypography = contentTypography,
        icon = icon
    )
}