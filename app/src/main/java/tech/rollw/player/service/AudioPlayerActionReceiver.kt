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

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaButtonReceiver
import androidx.media3.session.MediaSession

/**
 * @author RollW
 */
@OptIn(UnstableApi::class)
class AudioPlayerActionReceiver : BroadcastReceiver() {
    private val delegate = MediaButtonReceiver()

    override fun onReceive(context: Context, intent: Intent?) {
        // TODO: override this method to handle media button actions
        delegate.onReceive(context, intent)
    }

    companion object {
        /**
         * Build a [PendingIntent] for media button action.
         */
        // from androidx/media3/session/DefaultActionFactory.java
        fun buildMediaPendingIntent(
            service: Service,
            mediaSession: MediaSession,
            @Player.Command command: Int
        ): PendingIntent {
            val keyCode = toKeyCode(command)
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                setComponent(ComponentName(service, service::class.java))
                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            }
            return if (Build.VERSION.SDK_INT >= 26 &&
                command == Player.COMMAND_PLAY_PAUSE &&
                !mediaSession.player.playWhenReady
            ) {
                PendingIntent.getForegroundService(
                    service, keyCode, intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    service,
                    keyCode,
                    intent,
                    if (Util.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
                )
            }
        }

        private fun toKeyCode(action: @Player.Command Int): Int =
            when (action) {
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_NEXT -> {
                    KeyEvent.KEYCODE_MEDIA_NEXT
                }

                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS -> {
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                }

                Player.COMMAND_STOP -> {
                    KeyEvent.KEYCODE_MEDIA_STOP
                }

                Player.COMMAND_SEEK_FORWARD -> {
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                }

                Player.COMMAND_SEEK_BACK -> {
                    KeyEvent.KEYCODE_MEDIA_REWIND
                }

                Player.COMMAND_PLAY_PAUSE -> {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                }

                else -> KeyEvent.KEYCODE_UNKNOWN
            }
    }
}