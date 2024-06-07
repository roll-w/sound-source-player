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
import tech.rollw.player.R
import tech.rollw.player.service.scanner.AudioScanWorker

/**
 * @author RollW
 */
object WorkerDefaults {
    /**
     * The key for the progress of the worker.
     *
     * The value is an integer.
     */
    const val KEY_PROGRESS = "progress"

    const val KEY_MESSAGE = "message"

    const val KEY_NAME = "name"

    const val TAG_AUDIO_SCAN_WORKER = "AudioScanWorker"

    val AudioScanWorkerSpec: WorkerSpec = ResourceWorkerSpec(
        TAG_AUDIO_SCAN_WORKER,
        AudioScanWorker::class.java.name,
        R.string.task_audio_scan_title
    )

    fun getWorkerSpec(tags: Set<String>): WorkerSpec? = when {
        AudioScanWorkerSpec.isAnyOf(tags) -> AudioScanWorkerSpec
        else -> null
    }
}

private data class ResourceWorkerSpec(
    override val tag: String,
    override val clazzName: String,
    val nameId: Int
) : WorkerSpec {
    override fun getName(context: Context): String =
        context.getString(nameId)
}

private data class StringWorkerSpec(
    override val tag: String,
    override val clazzName: String,
    val name: String
) : WorkerSpec {
    override fun getName(context: Context): String = name
}