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

package tech.rollw.player.ui.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.rollw.player.ui.player.PlayerScreenLabel

/**
 * @author RollW
 */
class UIStateViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(PlayerScreenLabel.MEDIA_STORE)

    val currentScreen: StateFlow<String> = _currentScreen

    private val _selecting = MutableStateFlow(false)

    val selecting: StateFlow<Boolean> = _selecting

    private val _scrollOffset = MutableStateFlow(0)

    /**
     * Current scroll offset of the screen
     */
    val scrollOffset: StateFlow<Int> = _scrollOffset

    fun setCurrentScreen(@PlayerScreenLabel screen: String) {
        viewModelScope.launch {
            _currentScreen.value = screen
        }
    }

    fun setSelecting(selecting: Boolean) {
        viewModelScope.launch {
            _selecting.value = selecting
        }
    }

    /**
     * Set current scroll offset
     *
     * @see scrollOffset
     */
    fun setScrollOffset(offset: Int = INVALID_SCROLL_OFFSET) {
        viewModelScope.launch {
            _scrollOffset.value = offset
        }
    }

    companion object {
        const val INVALID_SCROLL_OFFSET = Int.MIN_VALUE
    }
}