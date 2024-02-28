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

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import tech.rollw.player.R

/**
 * @author RollW
 */
object NotificationChannels {
    private const val PLAYER_CHANNEL_ID = "tech.rollw.player.AudioPlayerSession"
    private const val PLAYER_CHANNEL_GROUP_ID = "tech.rollw.player.Player"

    val AudioPlayerChannel: ChannelConfig = ResourceChannelConfig(
        PLAYER_CHANNEL_ID,
        R.string.notification_player_channel_name,
        R.string.notification_player_channel_desc,
        NotificationManager.IMPORTANCE_LOW,
        ResourceChannelGroupConfig(
            PLAYER_CHANNEL_GROUP_ID,
            R.string.notification_player_channel_group_name
        )
    )

    private const val WORKER_CHANNEL_ID = "tech.rollw.player.WorkerService"
    private const val WORKER_CHANNEL_GROUP_ID = "tech.rollw.player.Worker"

    val WorkerChannel: ChannelConfig = ResourceChannelConfig(
        WORKER_CHANNEL_ID,
        R.string.notification_worker_channel_name,
        R.string.notification_worker_channel_desc,
        NotificationManager.IMPORTANCE_DEFAULT,
        StringChannelGroupConfig(
            WORKER_CHANNEL_GROUP_ID,
            "Tasks"
        )
    )

    fun createChannel(
        context: Context,
        channelConfig: ChannelConfig
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        val channelGroupContext = channelConfig.channelGroup
        if (channelGroupContext != null) {
            val channelGroup = NotificationChannelGroupCompat
                .Builder(channelGroupContext.id)
                .setName(channelGroupContext.getName(context))
                .build()
            notificationManager.createNotificationChannelGroup(channelGroup)
        }
        val channel = NotificationChannelCompat
            .Builder(channelConfig.id, channelConfig.importance)
            .setName(channelConfig.getName(context))
            .setDescription(channelConfig.getDescription(context))
            .setGroup(channelGroupContext?.id)
            .build()
        notificationManager.createNotificationChannel(channel)
    }
}

interface ChannelGroupConfig {
    val id: String

    fun getName(context: Context): String
}

private data class ResourceChannelGroupConfig(
    override val id: String,
    val name: Int
) : ChannelGroupConfig {
    override fun getName(context: Context) =
        context.getString(name)
}

private data class StringChannelGroupConfig(
    override val id: String,
    val name: String
) : ChannelGroupConfig {
    override fun getName(context: Context) = name
}

interface ChannelConfig {
    val id: String
    val importance: Int

    fun getName(context: Context): String

    fun getDescription(context: Context): String

    val channelGroup: ChannelGroupConfig?
        get() = null
}

private data class ResourceChannelConfig(
    override val id: String,
    val name: Int,
    val description: Int,
    override val importance: Int,
    override val channelGroup: ChannelGroupConfig? = null
) : ChannelConfig {

    override fun getName(context: Context) =
        context.getString(name)

    override fun getDescription(context: Context) =
        context.getString(description)
}

private data class StringChannelConfig(
    override val id: String,
    val name: String,
    val description: String,
    override val importance: Int,
    override val channelGroup: ChannelGroupConfig? = null
) : ChannelConfig {

    override fun getName(context: Context) = name

    override fun getDescription(context: Context) = description
}