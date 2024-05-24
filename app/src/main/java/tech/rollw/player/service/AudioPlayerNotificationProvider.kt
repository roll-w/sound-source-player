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

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import com.google.common.collect.ImmutableList
import tech.rollw.player.R
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.data.storage.CommonResources
import tech.rollw.player.data.storage.LocalImageLoader
import tech.rollw.player.ui.SplashActivity
import tech.rollw.player.ui.applicationService
import tech.rollw.support.io.ContentPath

/**
 * @author RollW
 */
@UnstableApi
class AudioPlayerNotificationProvider(
    private val context: Service,
    private val notificationId: Int,
    private val audioPlaylistProvider: AudioPlaylistProvider
) : MediaNotification.Provider {
    private val localImageLoader by context.applicationService<LocalImageLoader>()
    private val commonResources by context.applicationService<CommonResources>()

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        val current = audioPlaylistProvider.current
            ?: return MediaNotification(
                notificationId, Notification()
            )

        return MediaNotification(
            notificationId, buildNotification(
                current,
                mediaSession
            )
        )
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        return false
    }

    private fun buildNotification(
        audioContent: AudioContent,
        mediaSession: MediaSession
    ): Notification {
        val activityIntent = Intent(context, SplashActivity::class.java)
        activityIntent.putExtra(
            SplashActivity.EXTRA_SOURCE,
            SplashActivity.SOURCE_NOTIFICATION
        )
        val startActivityIntent = PendingIntent.getActivity(
            context, 0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mediaCover = loadCover(audioContent.path)

        val notificationBuilder = NotificationCompat.Builder(
            context, AudioPlayerSessionService.CHANNEL_CONFIG.id
        ).setSmallIcon(R.drawable.ic_icon)
            .setContentTitle(audioContent.audio.title)
            .setContentText("${audioContent.audio.artist} - ${audioContent.audio.album}")
            .setContentIntent(startActivityIntent)
            .setLargeIcon(mediaCover)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setVisibility(getNotificationVisibility())
            .setOngoing(mediaSession.player.isPlaying)
            .addAction(
                R.drawable.ic_baseline_skip_previous_24,
                "Previous", AudioPlayerActionReceiver.buildMediaPendingIntent(
                    context, mediaSession, Player.COMMAND_SEEK_TO_PREVIOUS
                )
            )
            .addAction(buildPlayPauseAction(mediaSession))
            .addAction(
                R.drawable.ic_baseline_skip_next_24,
                "Next", AudioPlayerActionReceiver.buildMediaPendingIntent(
                    context, mediaSession, Player.COMMAND_SEEK_TO_NEXT
                )
            )
            .addAction(
                R.drawable.ic_baseline_close_24,
                "Stop", AudioPlayerActionReceiver.buildMediaPendingIntent(
                    context, mediaSession, Player.COMMAND_STOP
                )
            )
            .setStyle(
                MediaStyle(mediaSession)
                    .setShowActionsInCompactView(1, 2, 3)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        AudioPlayerActionReceiver.buildMediaPendingIntent(
                            context, mediaSession,
                            Player.COMMAND_STOP
                        )
                    )
            )

        return notificationBuilder.build()
    }

    private fun loadCover(contentPath: ContentPath): Bitmap {
        val raw = localImageLoader.load(contentPath)
        if (raw != null && raw.isNotEmpty()) {
            return BitmapFactory.decodeByteArray(raw, 0, raw.size)
        }
        return commonResources.logo
    }

    private fun buildPlayPauseAction(
        mediaSession: MediaSession
    ): NotificationCompat.Action {
        val playing = mediaSession.player.isPlaying ||
                mediaSession.player.playWhenReady

        val playPauseIcon = if (playing) {
            R.drawable.ic_baseline_pause_24
        } else {
            R.drawable.ic_baseline_play_arrow_24
        }
        val playPauseTitle = if (playing) {
            context.getString(R.string.action_pause)
        } else {
            context.getString(R.string.action_play)
        }
        return NotificationCompat.Action.Builder(
            playPauseIcon, playPauseTitle,
            AudioPlayerActionReceiver.buildMediaPendingIntent(
                context, mediaSession, Player.COMMAND_PLAY_PAUSE
            )
        ).build()
    }

    private fun getNotificationVisibility(): Int {
        return NotificationCompat.VISIBILITY_SECRET
    }
}