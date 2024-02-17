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

package tech.rollw.player.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @author RollW
 */
class PlayerScreenViewModel(
    private val application: Application
) : AndroidViewModel(application) {
    private val _scrollOffset = MutableStateFlow(0)

    /**
     * Current scroll offset of the screen
     */
    val scrollOffset: StateFlow<Int> = _scrollOffset

    /**
     * Set current scroll offset
     *
     * @see scrollOffset
     */
    fun setScrollOffset(offset: Int) {
        viewModelScope.launch {
            _scrollOffset.value = offset
        }
    }
}