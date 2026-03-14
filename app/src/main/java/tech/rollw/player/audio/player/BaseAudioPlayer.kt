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

import android.util.Log
import tech.rollw.player.audio.AudioContent

/**
 * @author RollW
 */
@ExperimentalPlayerApi
abstract class BaseAudioPlayer : AudioPlayer {
    private var _audioContent: AudioContent? = null
    private var _playerState = AudioPlayer.PlayerState.IDLE
    private var _playbackState = AudioPlayer.PlaybackState.IDLE

    val audioContent: AudioContent?
        get() = _audioContent

    final override fun prepare(audioContent: AudioContent) {
//        if (playerState.isPlaying() &&
//            _playbackState != AudioPlayer.PlaybackState.ENDED
//        ) {
//            handlePause(AudioPlayer.PlayerState.PLAYING)
//        }

        _audioContent = audioContent

        val beforeState = playerState

        Log.d("BaseAudioPlayer", "prepare: before=$beforeState")

        handlePrepare(audioContent, beforeState)

        _playerState = AudioPlayer.PlayerState.PREPARED
        _playbackState = AudioPlayer.PlaybackState.IDLE

        invokeListeners()
    }

    protected abstract fun handlePrepare(
        audioContent: AudioContent,
        beforeState: AudioPlayer.PlayerState
    )

    final override fun play() {
        Log.d("BaseAudioPlayer", "play: before=$playerState")
        if (playerState == AudioPlayer.PlayerState.PLAYING) {
            return
        }
        if (_audioContent == null) {
            throw AudioPlayerException("Audio player is not prepared.")
        }

        when (playerState) {
            AudioPlayer.PlayerState.PREPARED,
            AudioPlayer.PlayerState.PAUSED -> {
                // Do nothing
            }

            AudioPlayer.PlayerState.STOPPED -> {
                prepare(_audioContent!!)
            }

            else -> {
                throw AudioPlayerException("Audio player is not prepared.")
            }
        }

        val beforeState = playerState

        Log.d("BaseAudioPlayer", "handlePlay: before=$beforeState")

        handlePlay(beforeState)

        _playerState = AudioPlayer.PlayerState.PLAYING
        _playbackState = AudioPlayer.PlaybackState.PLAYING

        invokeListeners()
    }

    protected abstract fun handlePlay(beforeState: AudioPlayer.PlayerState)

    final override fun pause() {
        Log.d("BaseAudioPlayer", "pause: before=$playerState")

        if (playerState == AudioPlayer.PlayerState.PAUSED) {
            return
        }
        if (playerState != AudioPlayer.PlayerState.PLAYING) {
            return
        }
        val beforeState = playerState

        Log.d("BaseAudioPlayer", "handlePause: before=$beforeState")
        handlePause(beforeState)

        _playerState = AudioPlayer.PlayerState.PAUSED
        _playbackState = AudioPlayer.PlaybackState.PAUSED

        invokeListeners()
    }

    protected abstract fun handlePause(beforeState: AudioPlayer.PlayerState)

    final override fun stop() {
        Log.d("BaseAudioPlayer", "stop: before=$playerState")
        if (playerState == AudioPlayer.PlayerState.STOPPED ||
            playerState == AudioPlayer.PlayerState.IDLE
        ) {
            return
        }

        val beforeState = playerState

        Log.d("BaseAudioPlayer", "handleStop: before=$beforeState")
        handleStop(beforeState)

        _playerState = AudioPlayer.PlayerState.STOPPED
        _playbackState = AudioPlayer.PlaybackState.STOPPED

        invokeListeners()
    }

    protected abstract fun handleStop(beforeState: AudioPlayer.PlayerState)

    final override fun release() {
        Log.d("BaseAudioPlayer", "release: before=$playerState")
        if (playerState == AudioPlayer.PlayerState.RELEASED) {
            return
        }
        val beforeState = playerState
        Log.d("BaseAudioPlayer", "handleRelease: before=$beforeState")
        handleRelease(beforeState)

        _playerState = AudioPlayer.PlayerState.RELEASED
        _playbackState = AudioPlayer.PlaybackState.STOPPED
    }

    protected abstract fun handleRelease(beforeState: AudioPlayer.PlayerState)

    final override val playerState: AudioPlayer.PlayerState
        get() = _playerState

    final override val playbackState: AudioPlayer.PlaybackState
        get() = _playbackState

    override fun setAudioVolume(left: Float, right: Float) {
    }

    override fun getAudioVolume(): Pair<Float, Float> {
        // TODO
        return Pair(1f, 1f)
    }

    final override fun setSpeed(speed: Float) {
        if (speed <= 0) {
            throw AudioPlayerException("Speed must be greater than 0.")
        }
        if (playerState == AudioPlayer.PlayerState.IDLE ||
            playerState == AudioPlayer.PlayerState.RELEASED
        ) {
            return
        }
        handleSetSpeed(speed)
    }

    protected abstract fun handleSetSpeed(speed: Float)

    final override fun getSpeed(): Float {
        return handleGetSpeed()
    }

    protected abstract fun handleGetSpeed(): Float

    private val listeners = mutableSetOf<AudioPlayer.Listener>()

    private fun invokeListeners(action: (AudioPlayer.Listener) -> Unit) {
        listeners.forEach(action)
    }

    private fun invokeListeners() {
        invokeListeners {
            it.onPlayerStateChanged(playerState)
            it.onPlaybackStateChanged(playbackState)
        }
    }

    final override fun addListener(listener: AudioPlayer.Listener) {
        if (listeners.contains(listener)) {
            return
        }
        listeners.add(listener)
    }

    final override fun removeListener(listener: AudioPlayer.Listener) {
        listeners.remove(listener)
    }

    final override fun seekTo(position: Long) {
        if (_audioContent == null) {
            throw AudioPlayerException("Audio player is not prepared.")
        }

        when (playerState) {
            AudioPlayer.PlayerState.IDLE,
            AudioPlayer.PlayerState.RELEASED -> {
                return
            }

            AudioPlayer.PlayerState.PREPARED,
            AudioPlayer.PlayerState.PLAYING,
            AudioPlayer.PlayerState.PAUSED -> {
                handleSeekTo(position)
            }

            AudioPlayer.PlayerState.STOPPED -> {
                prepare(_audioContent!!)
                handleSeekTo(position)
            }
        }
    }

    protected abstract fun handleSeekTo(position: Long)

    final override val position: Long
        get() {
            if (playerState == AudioPlayer.PlayerState.IDLE ||
                playerState == AudioPlayer.PlayerState.RELEASED ||
                playerState == AudioPlayer.PlayerState.STOPPED
            ) {
                return 0
            }
            return handleGetPosition()
        }

    protected abstract fun handleGetPosition(): Long

    /**
     * The implementation of this class should call [onPlaybackComplete]
     * when the playback is completed.
     */
    protected fun onPlaybackComplete() {
        _playbackState = AudioPlayer.PlaybackState.ENDED

        listeners.forEach {
            it.onPlaybackStateChanged(AudioPlayer.PlaybackState.ENDED)
        }
    }
}