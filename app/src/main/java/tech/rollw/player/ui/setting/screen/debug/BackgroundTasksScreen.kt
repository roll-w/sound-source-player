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

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import tech.rollw.player.service.WorkerDefaults
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme

/**
 * @author RollW
 */
@Composable
fun BackgroundTasksScreen(
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    val workInfos by workManager.getWorkInfosFlow(
        WorkQuery.fromStates(
            WorkInfo.State.RUNNING,
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED,
        )
    ).collectAsState(initial = emptyList())

    val taskInfos = workInfos.mapNotNull {
        fromWorkInfo(it, context)
    }
    LazyColumn {
        items(taskInfos,
            { task -> task.id }
        ) { task ->
            TaskInfoView(task)
        }
    }
}

@Composable
private fun TaskInfoView(
    task: TaskInfo,
    typography: ContentTypography = PlayerTheme.typography.contentNormal
) {

    Row {
        AnimatedVisibility(task.running) {
            CircularProgressIndicator(
                modifier = Modifier.padding(10.dp)
            )
        }
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = task.name,
                style = typography.title
            )
            Text(
                text = task.message ?: "",
                style = typography.body
            )
        }
    }

}

private data class TaskInfo(
    val id: String,
    val tags: Set<String>,
    val state: WorkInfo.State,
    val progress: Int,
    val name: String,
    val message: String?
) {
    val running: Boolean
        get() = state == WorkInfo.State.RUNNING
}

private fun fromWorkInfo(info: WorkInfo, context: Context): TaskInfo? {
    val workerSpec = WorkerDefaults.getWorkerSpec(info.tags) ?: return null
    val progress = if (info.state == WorkInfo.State.RUNNING) {
        info.progress.getInt(WorkerDefaults.KEY_PROGRESS, 0)
    } else {
        100
    }
    return TaskInfo(
        info.id.toString(),
        info.tags,
        info.state,
        progress,
        workerSpec.getName(context),
        tryGetMessage(info)
    )
}

private fun tryGetMessage(info: WorkInfo): String? {
    if (info.state == WorkInfo.State.RUNNING ||
        info.state == WorkInfo.State.ENQUEUED
    ) {
        return info.progress.getString(WorkerDefaults.KEY_MESSAGE)
    }

    return info.outputData.getString(WorkerDefaults.KEY_MESSAGE)
}