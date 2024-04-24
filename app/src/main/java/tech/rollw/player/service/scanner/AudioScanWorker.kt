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

package tech.rollw.player.service.scanner

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.rollw.player.R
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioFormatType
import tech.rollw.player.audio.AudioPath
import tech.rollw.player.audio.tag.NativeLibAudioTag
import tech.rollw.player.audio.toAudio
import tech.rollw.player.audio.toAudioPath
import tech.rollw.player.data.database.repository.AudioPathRepository
import tech.rollw.player.data.database.repository.AudioRepository
import tech.rollw.player.getApplicationService
import tech.rollw.player.service.NotificationChannels
import tech.rollw.player.service.WorkerDefaults
import tech.rollw.player.service.WorkerNotificationProvider
import tech.rollw.player.ui.SplashActivity
import tech.rollw.support.StringUtils.getSuffix
import tech.rollw.support.appcompat.openFileDescriptor
import tech.rollw.support.io.toContentPath
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * @author RollW
 */
class AudioScanWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val channelConfig = NotificationChannels.WorkerChannel
    private val progress = AtomicInteger(0)
    private val totalCounter = AtomicLong(0)

    private val notificationProvider = WorkerNotificationProvider(
        context, channelConfig
    )
    private val audioRepository = context.getApplicationService(
        AudioRepository::class.java
    ) {
        AudioRepository(context)
    }

    private val audioPathRepository = context.getApplicationService(
        AudioPathRepository::class.java
    ) {
        AudioPathRepository(context)
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            NotificationChannels.createChannel(
                context,
                channelConfig
            )

            setScanProgress(1)
            try {
                return@withContext executeWork()
            } finally {
                setScanProgress(100)
            }
        }
    }

    private suspend fun setScanProgress(progress: Int) {
        this.progress.set(progress)
        val foregroundInfo = getForegroundInfo()
        setProgressAsync(
            workDataOf(
                WorkerDefaults.KEY_PROGRESS to progress,
                WorkerDefaults.KEY_MESSAGE to foregroundInfo.notification.extras.getString(
                    NotificationCompat.EXTRA_TEXT
                )
            )
        )

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, foregroundInfo.notification)
        }
    }

    private suspend fun executeWork(): Result {
        val uris = inputData.getStringArray(KEY_URIS)?.map {
            Uri.parse(it)
        } ?: return Result.success()
        val start = System.currentTimeMillis()
        val audioPaths = collectUris(uris)
        setScanProgress(20)
        scanAudioTags(audioPaths) { audio ->
            audio ?: return@scanAudioTags
            // TODO: could show a notification for each audio
        }
        val end = System.currentTimeMillis()
        totalCounter.set(end - start)
        return Result.success(
            workDataOf(
                WorkerDefaults.KEY_MESSAGE to description()
            )
        )
    }

    private fun collectUris(uris: List<Uri>): Map<String, List<Uri>> {
        val audioPaths = mutableMapOf<String, MutableList<Uri>>()

        fun addPathIfAudio(file: DocumentFile) {
            if (file.isDirectory) {
                file.listFiles().forEach {
                    addPathIfAudio(it)
                }
                return
            }
            val identifier = getIdentifier(file.uri)
            val suffix = identifier.getSuffix()
            // check if it's an audio file by its extension
            AudioFormatType.fromExtensionOrNull(suffix) ?: return
            val list = audioPaths.getOrPut(identifier) { mutableListOf() }
            list.add(file.uri)
        }

        for (uri in uris) {
            val file = DocumentFile.fromTreeUri(context, uri) ?: continue
            addPathIfAudio(file)
        }
        return audioPaths
    }

    // TODO: coroutine scan audio tags
    private fun scanAudioTags(
        audioPaths: Map<String, List<Uri>>,
        onScan: (Audio?) -> Unit = {}
    ) {
        audioPaths.forEach { (identifier, uris) ->
            val audioFormatType = AudioFormatType
                .fromExtensionOrNull(identifier.getSuffix())
                ?: return@forEach
            val audio = scanAudioTag(
                uris,
                identifier,
                audioFormatType
            )
            onScan(audio)
        }
    }

    private fun scanAudioTag(
        uris: List<Uri>, identifier: String,
        audioFormatType: AudioFormatType
    ): Audio? {
        if (uris.isEmpty()) {
            return null
        }
        val scanResult = readAudioFile(
            uris,
            identifier,
            audioFormatType
        )
        if (scanResult.audio == null) {
            return null
        }
        return updateAudioByResult(scanResult, identifier)
    }

    private fun updateAudioByResult(
        audioReadResult: AudioReadResult,
        identifier: String
    ): Audio? {
        if (audioReadResult.audio == null ||
            audioReadResult.validUris.isEmpty()
        ) {
            return null
        }
        val audio = audioReadResult.audio
        val paths = audioReadResult.validUris.map {
            val path = it.toContentPath()
            path.toAudioPath(audio.id ?: 0, identifier)
        }
        return when (audioReadResult.policy) {
            POLICY_NONE -> audio

            POLICY_INSERT -> {
                val id = audioRepository.insertAudioWithPath(audio, paths)
                audio.copy(id = id)
            }

            POLICY_UPDATE -> {
                audioPathRepository.insert(paths)
                audioRepository.update(audio)
                audio
            }

            else -> throw IllegalStateException("Invalid audio scan policy: ${audioReadResult.policy}")
        }
    }

    private fun getAudioPathsByIdentifier(identifier: String): List<AudioPath> {
        return audioPathRepository.getByIdentifier(identifier)
    }

    private fun readAudioFile(
        uris: List<Uri>,
        identifier: String,
        audioFormatType: AudioFormatType
    ): AudioReadResult {
        val validUris = collectValidUris(uris)
        val pfd = validUris.firstNotNullOfOrNull {
            tryOpenFileDescriptorOf(it)
        }
        if (pfd == null) {
            Log.w(TAG, "Failed to open file descriptor: $identifier. None of the uris is valid.")
            return AudioReadResult.EMPTY
        }
        val existPaths = getAudioPathsByIdentifier(identifier)
        val existId = existPaths.firstOrNull()?.id

        val existAudio = if (existId != null) {
            audioRepository.getById(existId)
        } else {
            null
        }

        val fd = pfd.detachFd()
        val audioTag = NativeLibAudioTag(
            fd,
            audioFormatType = audioFormatType,
            readonly = true
        )
        val timestamp = System.currentTimeMillis()
        val lastModified = audioTag.getLastModified()

        if (existAudio != null &&
            lastModified == existAudio.lastModified
        ) {
            return AudioReadResult(existAudio, validUris)
        }

        val audio = audioTag.toAudio(existId, timestamp)
        return AudioReadResult(
            audio, validUris,
            policy = if (existAudio != null)
                POLICY_UPDATE
            else POLICY_INSERT
        )
    }

    private fun collectValidUris(
        uris: List<Uri>
    ): List<Uri> {
        return uris.filter {
            val fd = context.contentResolver.openFileDescriptor(it, "r")
            try {
                fd != null
            } catch (e: Exception) {
                false
            } finally {
                fd?.close()
            }
        }
    }

    private data class AudioReadResult(
        val audio: Audio?,
        val validUris: List<Uri>,
        val invalidPaths: List<AudioPath> = emptyList(),
        val policy: Int = POLICY_NONE
    ) {
        companion object {
            val EMPTY = AudioReadResult(null, emptyList())
        }
    }

    private fun tryOpenFileDescriptorOf(uri: Uri): ParcelFileDescriptor? =
        try {
            uri.openFileDescriptor(context, "r")
        } catch (e: Exception) {
            null
        }

    private fun getIdentifier(uri: Uri) =
        Uri.decode(uri.lastPathSegment)

    private val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, SplashActivity::class.java).apply {
            putExtra(SplashActivity.EXTRA_SOURCE, SplashActivity.SOURCE_NOTIFICATION)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun title() = context.getString(R.string.task_audio_scan_title)

    private fun description() = if (progress.get() >= 100) {
        context.getString(
            R.string.task_audio_scan_success,
            (totalCounter.get() / 1000).toString()
        )
    } else {
        context.getString(R.string.task_audio_scan_description)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            notificationProvider.createNotification(
                title(),
                description(),
                progress.get()
            ) {
                setContentIntent(pendingIntent)
                if (progress.get() == 100) {
                   setAutoCancel(true)
                }
            }
        )
    }

    private class Tracer(
        private val start: Long = System.currentTimeMillis()
    ) {
        // TODO: audio worker tracer
        private var end: Long = 0

        fun onFailed(
            uri: Uri,
            e: Exception?
        ) {
        }

        fun end() {
            end = System.currentTimeMillis()
        }
    }


    companion object {
        private const val TAG = WorkerDefaults.TAG_AUDIO_SCAN_WORKER
        private const val NOTIFICATION_ID = 24010

        private const val KEY_FILTER_PATHS = "filter_paths"
        private const val KEY_URIS = "uris"

        /**
         * The key for the audio length threshold.
         *
         * The value is long and represents the audio length in milliseconds.
         */
        private const val KEY_AUDIO_LENGTH_THRESHOLD = "audio_length_threshold"

        @JvmStatic
        fun submitWork(context: Context) {
            val rwUris = mutableListOf<Uri>()
            context.contentResolver.persistedUriPermissions.forEach {
                rwUris.add(it.uri)
            }
            submitWork(context, rwUris)
        }

        @JvmStatic
        fun submitWork(
            context: Context, uris: List<Uri>,
            filterPaths: List<String> = emptyList(),
            audioLengthThreshold: Long = 0
        ) {
            val uriStrings = uris.map { it.toString() }

            val data = workDataOf(
                KEY_URIS to uriStrings.toTypedArray(),
                KEY_FILTER_PATHS to filterPaths.toTypedArray(),
                KEY_AUDIO_LENGTH_THRESHOLD to audioLengthThreshold
            )
            val workRequest = OneTimeWorkRequestBuilder<AudioScanWorker>()
                .addTag(TAG)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context)
                .beginUniqueWork(TAG, ExistingWorkPolicy.KEEP, workRequest)
                .enqueue()
        }

        private const val POLICY_NONE = 0
        private const val POLICY_UPDATE = 1
        private const val POLICY_INSERT = 2

    }
}