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

    val playbackState: PlaybackState

    fun setAudioVolume(left: Float, right: Float = left)

    fun getAudioVolume(): Pair<Float, Float>

    fun setSpeed(speed: Float)

    fun getSpeed(): Float

    fun seekTo(position: Long)

    val position: Long

    interface Listener {
        fun onPlaybackStateChanged(state: PlaybackState) {}

        fun onPlayerStateChanged(state: PlayerState) {}
    }

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

    @JvmInline
    value class PlayerState private constructor(private val state: Int) {
        fun isPlaying(): Boolean {
            return this == PLAYING
        }

        fun isPaused(): Boolean {
            return this != PLAYING
        }

        override fun toString(): String = when (state) {
            0 -> "IDLE"
            1 -> "PREPARED"
            2 -> "PLAYING"
            3 -> "PAUSED"
            4 -> "STOPPED"
            5 -> "RELEASED"
            else -> "UNKNOWN"
        }

        companion object {
            val IDLE = PlayerState(0)
            val PREPARED = PlayerState(1)
            val PLAYING = PlayerState(2)
            val PAUSED = PlayerState(3)
            val STOPPED = PlayerState(4)
            val RELEASED = PlayerState(5)
        }
    }

    @JvmInline
    value class PlaybackState private constructor(private val state: Int) {
        override fun toString(): String = when (state) {
            0 -> "IDLE"
            1 -> "BUFFERING"
            2 -> "PLAYING"
            3 -> "PAUSED"
            4 -> "ENDED"
            5 -> "STOPPED"
            else -> "UNKNOWN"
        }

        companion object {
            val IDLE = PlaybackState(0)
            val BUFFERING = PlaybackState(1)
            val PLAYING = PlaybackState(2)
            val PAUSED = PlaybackState(3)
            val ENDED = PlaybackState(4)
            val STOPPED = PlaybackState(5)
        }
    }
}