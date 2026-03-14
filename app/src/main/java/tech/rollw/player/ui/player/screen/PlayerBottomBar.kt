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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import tech.rollw.compose.runtime.rememberPrevious
import tech.rollw.player.ui.player.PlayerStateViewModel

/**
 * @author RollW
 */
data class NavigationItem(
    val id: Int,
    val label: String,
    val icon: Int,
)

@Composable
fun PlayerBottomBar(
    navigationItems: List<NavigationItem>,
    systemNavigationHeight: Int = 0,
    scrollOffset: Int = PlayerStateViewModel.INVALID_SCROLL_OFFSET,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onSelect: (Int) -> Boolean = { false },
    showNowPlayingBar: Boolean = true,
    nowPlayingBar: @Composable () -> Unit = {}
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val lastOffset = rememberPrevious(scrollOffset)
    val offsetMinus = scrollOffset - (lastOffset ?: 0)

    // TODO: calculate the offset for the navigation bar
    var lastY by remember { mutableIntStateOf(0) }
    val y = if (scrollOffset == PlayerStateViewModel.INVALID_SCROLL_OFFSET) {
        0
    } else if (offsetMinus > 0) {
        navigationBarHeight - systemNavigationHeight
    } else {
        0
    }.also {
        lastY = it
    }

    val animatedColor by animateColorAsState(
        targetValue = backgroundColor
    )

    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(0, y),
        animationSpec = spring(1F)
    )

    Surface(
        color = animatedColor,
        modifier = Modifier
            .offset { animatedOffset }
            .fillMaxWidth()
    ) {
        Column {
            AnimatedVisibility(
                visible = showNowPlayingBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                nowPlayingBar()
            }
            NavigationBar(
                modifier = Modifier.onGloballyPositioned {
                    navigationBarHeight = it.size.height
                },
                containerColor = Color.Transparent,
                windowInsets = WindowInsets(left = 0)
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item.id,
                        onClick = {
                            val state = onSelect(item.id)
                            if (state) {
                                selectedItem = item.id
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        },
                    )
                }
            }
        }
    }
}