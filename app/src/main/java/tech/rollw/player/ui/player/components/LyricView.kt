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

package tech.rollw.player.ui.player.components

import androidx.annotation.IntDef
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.rollw.compose.animation.core.animateTextStyleAsState
import tech.rollw.compose.foundation.interaction.collectIsInteractedAsState
import tech.rollw.compose.ui.graphics.FadingEdge
import tech.rollw.compose.ui.graphics.fadingEdge
import tech.rollw.player.audio.tag.Lyric
import tech.rollw.player.audio.tag.LyricRow
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.player.AudioUtils.formatDuration

/**
 * @author RollW
 */
object LyricViewDefaults {
    @Composable
    fun typography(
        timestamp: TextStyle = PlayerTheme.typography.contentLarge.tip,
        current: TextStyle = PlayerTheme.typography.contentLarge.title.copy(
            fontSize = 26.sp
        ),
        next: TextStyle = PlayerTheme.typography.contentLarge.title.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal
        ),
        previous: TextStyle = next
    ): LyricTypography = LyricTypography(
        timestamp = timestamp,
        current = current,
        next = next,
        previous = previous
    )
}


@IntDef(
    LyricViewTimestampPolicy.TIMESTAMP_POLICY_NONE,
    LyricViewTimestampPolicy.TIMESTAMP_POLICY_CURRENT,
    LyricViewTimestampPolicy.TIMESTAMP_POLICY_NEXT,
    LyricViewTimestampPolicy.TIMESTAMP_POLICY_PREVIOUS,
    LyricViewTimestampPolicy.TIMESTAMP_POLICY_ALL,
    flag = true
)
@Retention(AnnotationRetention.SOURCE)
annotation class LyricViewTimestampPolicy {
    companion object {
        const val TIMESTAMP_POLICY_NONE: Int = 0
        const val TIMESTAMP_POLICY_CURRENT: Int = 1
        const val TIMESTAMP_POLICY_NEXT: Int = 1 shl 1
        const val TIMESTAMP_POLICY_PREVIOUS: Int = 1 shl 2
        const val TIMESTAMP_POLICY_ALL: Int = 1 shl 3
    }
}

@Immutable
data class LyricTypography(
    /**
     * The style for the timestamp.
     */
    val timestamp: TextStyle,

    /**
     * The style for the current lyric.
     */
    val current: TextStyle,

    /**
     * The style for the next lyric.
     */
    val next: TextStyle,

    /**
     * The style for the previous lyric.
     */
    val previous: TextStyle = next
)

@JvmInline
private value class TimestampState(
    @LyricViewTimestampPolicy
    val policy: Int,
) {
    fun isShowAll(): Boolean {
        return policy == LyricViewTimestampPolicy.TIMESTAMP_POLICY_ALL
    }

    fun isShowCurrent(): Boolean {
        if (isShowAll()) {
            return true
        }
        return policy and LyricViewTimestampPolicy.TIMESTAMP_POLICY_CURRENT != 0
    }

    fun isShowNext(): Boolean {
        if (isShowAll()) {
            return true
        }
        return policy and LyricViewTimestampPolicy.TIMESTAMP_POLICY_NEXT != 0
    }

    fun isShowPrevious(): Boolean {
        if (isShowAll()) {
            return true
        }
        return policy and LyricViewTimestampPolicy.TIMESTAMP_POLICY_PREVIOUS != 0
    }

    fun shouldShowTimestamp(lineState: LineState): Boolean {
        if (isShowAll()) {
            return true
        }
        if (!lineState.isNear()) {
            return false
        }
        return when {
            isShowCurrent() && lineState.isCurrent() -> true
            isShowNext() && lineState.isNext() -> true
            isShowPrevious() && lineState.isPrevious() -> true
            else -> false
        }
    }
}

private const val LINE_CURRENT = 0
private const val LINE_NEXT = 1
private const val LINE_PREVIOUS = 1 shl 1

private const val LINE_OTHER = 1 shl 3
private const val LINE_NEAR = 1 shl 4

@JvmInline
private value class LineState(
    val state: Int
) {
    fun isCurrent(): Boolean =
        state == LINE_CURRENT

    fun isNext(): Boolean =
        state and LINE_NEXT != 0

    fun isPrevious(): Boolean =
        state and LINE_PREVIOUS != 0

    fun isNear(): Boolean {
        if (state == LINE_CURRENT) {
            return true
        }
        return state and LINE_NEAR != 0
    }

    fun isOther(): Boolean =
        state == LINE_OTHER

    override fun toString(): String {
        return when {
            isCurrent() -> "Current"
            isNear() && isNext() -> "Next Near"
            isNear() && isPrevious() -> "Previous Near"
            isNext() -> "Next"
            isPrevious() -> "Previous"
            else -> "Other"
        }
    }
}

@Composable
fun LyricView(
    lyric: Lyric,
    activeIndex: Int,
    modifier: Modifier = Modifier,
    onClick: (LyricRow) -> Unit = {},
    onLongClick: (LyricRow) -> Unit = {},
    nextAlpha: Float = 0.5f,
    previousAlpha: Float = 0.5f,
    typography: LyricTypography = LyricViewDefaults.typography(),
    @LyricViewTimestampPolicy
    timestampPolicy: Int = LyricViewTimestampPolicy.TIMESTAMP_POLICY_NONE,
    emptyContent: @Composable () -> Unit = {
        Text(
            text = "No lyric available",
            style = typography.current
        )
    }
) {
    val timestamp = TimestampState(timestampPolicy)
    val listState = rememberLazyListState()
    val isInteracted by listState.interactionSource.collectIsInteractedAsState()

    var height by remember { mutableIntStateOf(0) }
    var activeItemHeight by remember { mutableIntStateOf(0) }

    val typography = typography.copy(
        current = typography.current.copy(
            shadow = Shadow(
                color = PlayerTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                offset = Offset(0f, 0f),
                blurRadius = 50f
            )
        )
    )

    LaunchedEffect(activeIndex) {
        snapshotFlow {
            activeIndex
        }.collect { index ->
            if (isInteracted) {
                return@collect
            }
            val scrollTo = if (index < 0) 0 else index + 1
            // TODO: add a offset = (prevActiveHeight - prevHeight) / 2
            val itemInfo = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == scrollTo }

            when {
                itemInfo != null -> {
                    val center = listState.layoutInfo.viewportEndOffset / 2
                    val childCenter = itemInfo.offset + itemInfo.size / 2
                    listState.animateScrollBy((childCenter - center).toFloat())
                }

                else -> {
                    listState.animateScrollToItem(
                        scrollTo,
                        (-height / 2) + (activeItemHeight / 2)
                    )
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .fadingEdge(FadingEdge.vertical(0.15F))
            .onGloballyPositioned { coordinates ->
                height = coordinates.size.height
            }
    ) {
        item {
            Spacer(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        (height / 2).toDp()
                    }
                )
            )
        }

        if (lyric.body.isEmpty()) {
            item {
                emptyContent()
            }
        }

        itemsIndexed(
            lyric.body,
            key = { i, _ -> i },
        ) { index, row ->
            val line = when {
                index == activeIndex -> LINE_CURRENT
                index == activeIndex + 1 -> LINE_NEXT or LINE_NEAR
                index == activeIndex - 1 -> LINE_PREVIOUS or LINE_NEAR
                index < activeIndex - 1 -> LINE_PREVIOUS
                index > activeIndex + 1 -> LINE_NEXT
                else -> LINE_OTHER
            }

            val lineState = LineState(line)

            val lyricTextStyle = when {
                lineState.isCurrent() -> typography.current
                lineState.isNext() -> typography.next
                lineState.isPrevious() -> typography.previous
                else -> typography.next
            }

            val alpha = when {
                lineState.isCurrent() -> 1f
                lineState.isNext() -> nextAlpha
                lineState.isPrevious() -> previousAlpha
                else -> nextAlpha
            }

            LyricLine(
                row = row,
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        if (lineState.isCurrent()) {
                            activeItemHeight = coordinates.size.height
                        }
                    }
                    .padding(bottom = 4.dp),
                onClick = onClick,
                onLongClick = onLongClick,
                textStyle = lyricTextStyle,
                textAlpha = alpha,
                showTimestamp = timestamp.shouldShowTimestamp(lineState),
                timestampStyle = typography.timestamp,
                timestampAlpha = alpha,
                finishListener = {
                    // TODO: after finish, try animate scroll again
                }
            )
        }

        item {
            Spacer(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        (height / 2).toDp()
                    }
                )
            )
        }
    }
}

@Suppress("AnimateAsStateLabel")
@Composable
private fun LyricLine(
    row: LyricRow,
    modifier: Modifier = Modifier,
    onClick: (LyricRow) -> Unit = {},
    onLongClick: (LyricRow) -> Unit = {},
    textStyle: TextStyle = PlayerTheme.typography.contentLarge.title,
    textAlpha: Float = 1f,
    showTimestamp: Boolean = false,
    timestampStyle: TextStyle = PlayerTheme.typography.contentLarge.tip,
    timestampAlpha: Float = 1f,
    finishListener: (TextStyle) -> Unit = {}
) {
    val animateTextStyle by animateTextStyleAsState(
        targetValue = textStyle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = finishListener
    )

    val timestampColor by animateColorAsState(
        targetValue = timestampStyle.color.copy(alpha = timestampAlpha)
    )

    val animateAlpha by animateFloatAsState(targetValue = textAlpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(row) },
                    onLongClick = { onLongClick(row) }
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            AnimatedVisibility(showTimestamp) {
                Text(
                    text = row.timestamp.formatDuration(),
                    modifier = Modifier,
                    style = timestampStyle,
                    color = timestampColor
                )
            }
            Text(
                text = row.contents.joinToString("\n"),
                modifier = Modifier
                    .alpha(animateAlpha),
                style = animateTextStyle,
            )
        }
    }
}