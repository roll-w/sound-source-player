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

package tech.rollw.player.ui.setting.screen.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import tech.rollw.player.analytics.LocalAnalytics
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.support.analytics.Analytics
import tech.rollw.support.analytics.AnalyticsEvent
import tech.rollw.support.analytics.CombinedAnalytics
import tech.rollw.support.analytics.InMemoryAnalytics

/**
 * @author RollW
 */
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val analytics = LocalAnalytics.current
    val events by getAnalyticsEventsFlow(analytics)
        .collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        events.forEach { event ->
            AnalyticsEvent(
                event = event,
                contentTypography = contentTypography
            )
        }
    }
}

@Composable
private fun AnalyticsEvent(
    event: AnalyticsEvent,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = event.type,
            style = contentTypography.title,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )

        event.extras.forEach { (key, value) ->
            Text(
                text = "$key: $value",
                style = contentTypography.body
            )
        }
    }
}

private fun getAnalyticsEventsFlow(analytics: Analytics?): Flow<List<AnalyticsEvent>> {
    return when (analytics) {
        null -> {
            emptyFlow()
        }

        is CombinedAnalytics -> {
            getAnalyticsEventsFlow(
                analytics.findByType(InMemoryAnalytics::class.java)
            )
        }

        is InMemoryAnalytics -> {
            analytics.eventsFlow
        }

        else -> {
            emptyFlow()
        }
    }
}