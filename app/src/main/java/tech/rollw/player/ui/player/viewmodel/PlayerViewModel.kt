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

import kotlinx.coroutines.flow.StateFlow

/**
 * Player view model. It provides the state of the player.
 *
 * @author RollW
 */
interface PlayerViewModel {
    val playing: StateFlow<Boolean>

    /**
     * Current audio position in milliseconds.
     */
    val audioPosition: StateFlow<Long>

    /**
     * Set the playing state of the player.
     *
     * Note: it has no effect on player.
     */
    fun setPlaying(playing: Boolean)

    /**
     * Set the audio position in milliseconds.
     *
     * Note: it has no effect on player.
     */
    fun setAudioPosition(position: Long)

    companion object {
        const val INVALID_POSITION = -1L
    }
}