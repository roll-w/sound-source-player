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

import androidx.annotation.IntDef
import tech.rollw.player.audio.AudioContent

/**
 * Audio player interface for playing audio content.
 *
 * @author RollW
 */
@ExperimentalPlayerApi
interface AudioPlayer {
    fun prepare(audioContent: AudioContent)

    fun play()

    fun pause()

    fun stop()

    fun release()

    val playerState: PlayerState

    @PlaybackState
    val playbackState: Int

    fun setAudioVolume(left: Float, right: Float = left)

    fun getAudioVolume(): Pair<Float, Float>

    fun setSpeed(speed: Float)

    fun getSpeed(): Float

    fun seekTo(position: Long)

    val position: Long

    enum class PlayerState {
        IDLE,
        PREPARED,
        PLAYING,
        PAUSED,
        STOPPED,
        RELEASED;

        fun isPlaying(): Boolean {
            return this == PLAYING
        }

        fun isPaused(): Boolean {
            return this != PLAYING
        }
    }

    @IntDef(
        PlaybackState.IDLE,
        PlaybackState.BUFFERING,
        PlaybackState.PLAYING,
        PlaybackState.PAUSED,
        PlaybackState.ENDED,
        PlaybackState.STOPPED
    )
    @Retention(AnnotationRetention.SOURCE)
    @MustBeDocumented
    annotation class PlaybackState {
        companion object {
            const val IDLE = 0
            const val BUFFERING = 1
            const val PLAYING = 2
            const val PAUSED = 3
            const val ENDED = 4
            const val STOPPED = 5
        }
    }
}