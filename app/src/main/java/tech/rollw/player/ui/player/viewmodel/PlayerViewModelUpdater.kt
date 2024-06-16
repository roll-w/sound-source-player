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

import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player

/**
 * A helper class to update [PlayerViewModel] from [Player].
 *
 * @author RollW
 */
class PlayerViewModelUpdater(
    private val playerViewModel: PlayerViewModel,
    private val delayMilliseconds: Long = 100
) : Player.Listener {
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var player: Player

    fun init(player: Player) {
        this.player = player
        player.addListener(this)
        onPlay(player.isPlaying)
    }

    fun release() {
        if (!this::player.isInitialized) {
            return
        }
        player.removeListener(this)
        handler.removeCallbacksAndMessages(null)
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        playerViewModel.setAudioPosition(newPosition.positionMs)
    }

    private fun checkPlaybackPosition(
        delay: Long,
        player: Player
    ): Boolean = handler.postDelayed({
        playerViewModel.setAudioPosition(player.currentPosition)
        checkPlaybackPosition(delay, player)
    }, delay)

    override fun onIsPlayingChanged(isPlaying: Boolean) {
    }

    override fun onPlayWhenReadyChanged(
        playWhenReady: Boolean,
        reason: Int
    ) {
        onPlay(playWhenReady)
    }

    private fun onPlay(playing: Boolean) {
        playerViewModel.setPlaying(playing)
        playerViewModel.setAudioPosition(player.currentPosition)
        if (playing) {
            checkPlaybackPosition(delayMilliseconds, player)
        } else {
            handler.removeCallbacksAndMessages(null)
        }
    }
}