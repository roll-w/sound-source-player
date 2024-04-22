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
import android.content.Context
import androidx.core.app.NotificationCompat
import tech.rollw.player.R

/**
 * @author RollW
 */
class WorkerNotificationProvider(
    private val context: Context,
    private val channelConfig: ChannelConfig
) {
    fun createNotification(
        title: String?,
        content: String? = null,
        progress: Int = -1,
        onNotificationBuild: (NotificationCompat.Builder.() -> Unit) = {}
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelConfig.id)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply(onNotificationBuild)

        if (progress in 0..99) {
            builder
                .setProgress(100, progress, false)
                .setOngoing(true)
        }

        return builder.build()
    }
}