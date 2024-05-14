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

package tech.rollw.player.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import tech.rollw.player.R
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.audio.player.withAudioPlaylistProvider
import tech.rollw.player.ui.applicationService

/**
 * @author RollW
 */
@OptIn(UnstableApi::class)
class AudioPlayerSessionService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    private lateinit var notificationProvider: MediaNotification.Provider
    private lateinit var notificationManager: NotificationManagerCompat

    private val audioPlaylistProvider by applicationService<AudioPlaylistProvider>()

    companion object {
        private const val TAG = "AudioPlayerSessionService"

        val CHANNEL_CONFIG = NotificationChannels.AudioPlayerChannel

        private const val NOTIFICATION_ID = 24001

        private var instance: AudioPlayerSessionService? = null

        private fun isServiceCreated(): Boolean {
            return try {
                // If instance was not cleared but the service was destroyed,
                // an Exception will be thrown
                instance != null && instance!!.ping()
            } catch (e: Exception) {
                // destroyed/not-started
                false
            }
        }

        fun startAudioPlayerSessionService(context: Context) {
            if (isServiceCreated()) {
                return
            }
            val appCtx = context.applicationContext
            val intent = Intent(
                appCtx,
                AudioPlayerSessionService::class.java
            )
            try {
                appCtx.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Start AudioPlayerSessionService failed: ${e.message}", e)
            }
        }

    }

    /**
     * Simply returns true. If the service is still active,
     * this method will be accessible.
     *
     * @return true
     */
    private fun ping(): Boolean {
        return true
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)

        initNotificationConfig()

        val callback = SessionCallback()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
                    .build(), false
            )
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .withAudioPlaylistProvider(audioPlaylistProvider)
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(callback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        instance = null
        super.onDestroy()
    }

    private fun initNotificationConfig() {
        NotificationChannels.createChannel(
            this,
            CHANNEL_CONFIG
        )
        notificationProvider = AudioPlayerNotificationProvider(
            this, NOTIFICATION_ID,
            audioPlaylistProvider
        )
        setMediaNotificationProvider(notificationProvider)
    }

    private class SessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return super.onConnect(session, controller)
        }

        companion object {
            private const val TAG = "SessionCallback"
        }
    }

    private fun getButtonLayout(
        playing: Boolean = false
    ): List<CommandButton> {
        return listOf(
            CommandButton.Builder()
                .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
                .setIconResId(R.drawable.ic_baseline_skip_previous_24)
                .setDisplayName("Previous")
                .build(),
            CommandButton.Builder()
                .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                .setIconResId(R.drawable.ic_baseline_play_arrow_24)
                .setDisplayName("Play/Pause")
                .build(),
            CommandButton.Builder()
                .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT)
                .setIconResId(R.drawable.ic_baseline_skip_next_24)
                .setDisplayName("Next")
                .build(),
            CommandButton.Builder()
                .setSessionCommand(
                    SessionCommand(
                        "STOP",
                        Bundle.EMPTY,
                    )
                )
                .setIconResId(R.drawable.ic_baseline_close_24)
                .setDisplayName("Stop")
                .build()
        )
    }
}