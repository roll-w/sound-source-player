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
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import tech.rollw.player.audio.AudioContent

/**
 * @author RollW
 */
@ExperimentalPlayerApi
class MediaAudioPlayer(
    context: Context
) : BaseAudioPlayer(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener {

    private val context = context.applicationContext

    private var mediaPlayer: MediaPlayer = newMediaPlayer()

    private fun newMediaPlayer() = MediaPlayer().apply {
        setupMediaPlayer(this)
    }

    private var prepared = false

    override fun handlePrepare(
        audioContent: AudioContent,
        beforeState: AudioPlayer.PlayerState
    ) {
        if (beforeState == AudioPlayer.PlayerState.RELEASED) {
            mediaPlayer = newMediaPlayer()
        } else if (beforeState != AudioPlayer.PlayerState.IDLE) {
            mediaPlayer.reset()
        }

        prepared = false
        mediaPlayer.setDataSource(context, audioContent.path.toUri())
        mediaPlayer.prepare()
    }

    override fun onPrepared(mp: MediaPlayer) {
        prepared = true
    }

    private fun setupMediaPlayer(mediaPlayer: MediaPlayer) {
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnPreparedListener(this)
    }

    override fun handlePlay(beforeState: AudioPlayer.PlayerState) {
        mediaPlayer.start()
    }

    override fun handlePause(beforeState: AudioPlayer.PlayerState) {
        mediaPlayer.pause()
    }

    override fun handleStop(beforeState: AudioPlayer.PlayerState) {
        mediaPlayer.stop()
    }

    override fun handleRelease(beforeState: AudioPlayer.PlayerState) {
        mediaPlayer.release()
    }

    override fun handleSetSpeed(speed: Float) {
        mediaPlayer.playbackParams.speed = speed
        // mediaPlayer.playbackParams = mediaPlayer.playbackParams
    }

    override fun handleGetSpeed(): Float {
        return mediaPlayer.playbackParams.speed
    }

    override fun setAudioVolume(left: Float, right: Float) {
        mediaPlayer.setVolume(left, right)
    }

    override fun handleSeekTo(position: Long) {
        mediaPlayer.seekTo(position.toInt())
    }

    override fun handleGetPosition(): Long {
        if (!prepared) {
            return 0
        }
        return mediaPlayer.currentPosition.toLong()
    }

    override val playerEngine: AudioPlayerEngine
        get() = AudioPlayerEngine.MEDIA_PLAYER

    override fun onCompletion(mp: MediaPlayer) {
        if (mp.duration < 0 || mp.currentPosition < 0) {
            return
        }
        onPlaybackComplete()
    }

    override fun onError(
        mp: MediaPlayer,
        what: Int,
        extra: Int
    ): Boolean {
        val humanReadableError = when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "MEDIA_ERROR_UNKNOWN"
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MEDIA_ERROR_SERVER_DIED"
            else -> "Unknown error $what"
        }
        val humanReadableExtra = when (extra) {
            MediaPlayer.MEDIA_ERROR_IO -> "MEDIA_ERROR_IO"
            MediaPlayer.MEDIA_ERROR_MALFORMED -> "MEDIA_ERROR_MALFORMED"
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "MEDIA_ERROR_UNSUPPORTED"
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "MEDIA_ERROR_TIMED_OUT"
            else -> "Unknown extra $extra"
        }
        Log.d(
            TAG,
            "onError: what=$humanReadableError, extra=$humanReadableExtra"
        )
        return true
    }

    companion object {
        private const val TAG = "MediaAudioPlayer"
    }
}