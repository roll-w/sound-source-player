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

package tech.rollw.player.audio.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import tech.rollw.player.PlayerApplication

/**
 * @author RollW
 */
@ExperimentalPlayerApi
object AudioPlayerFactory {
    fun createPlayer(engine: AudioPlayerEngine): AudioPlayer {
        return when (engine) {
            AudioPlayerEngine.MEDIA_PLAYER -> MediaPlayer
            AudioPlayerEngine.EXO -> TODO()
            AudioPlayerEngine.AUDIO_TRACK -> TODO()
            AudioPlayerEngine.OPEN_SL_ES -> TODO()
            AudioPlayerEngine.AAUDIO -> TODO()
        }
    }

    private fun createMediaPlayer(context: Context): AudioPlayer {
        return MediaAudioPlayer(context)
    }

    val MediaPlayer: AudioPlayer
        get() = createMediaPlayer(PlayerApplication.APPLICATION)

    val OpenSL: AudioPlayer
        get() = TODO()

    val AAudio: AudioPlayer
        get() = TODO()

    val ExoPlayer: AudioPlayer
        get() = TODO()

    @OptIn(UnstableApi::class)
    fun AudioPlayer.asMedia3Player(): Player {
        if (this is Player) {
            return this
        }
        return AudioPlayerBridge(this)
    }
}