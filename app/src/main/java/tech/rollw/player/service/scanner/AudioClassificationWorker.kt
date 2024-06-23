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

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.list.PlaylistItem
import tech.rollw.player.audio.list.PlaylistType
import tech.rollw.player.data.database.repository.AudioPathRepository
import tech.rollw.player.data.database.repository.AudioRepository
import tech.rollw.player.data.database.repository.PlaylistItemRepository
import tech.rollw.player.data.database.repository.PlaylistRepository
import tech.rollw.player.service.WorkerDefaults
import tech.rollw.player.ui.applicationService
import tech.rollw.support.analytics.Analytics
import tech.rollw.support.analytics.AnalyticsEvent
import tech.rollw.support.io.ContentPath

/**
 * @author RollW
 */
class AudioClassificationWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val analytics by context.applicationService<Analytics>()

    private val audioRepository by context.applicationService<AudioRepository>()
    private val audioPathRepository by context.applicationService<AudioPathRepository>()
    private val playlistRepository by context.applicationService<PlaylistRepository>()
    private val playlistItemRepository by context.applicationService<PlaylistItemRepository>()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                executeWork()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to classify audios", e)
                Result.failure(
                    Data.Builder()
                        .putString("error", e.message)
                        .build()
                )
            }
        }
    }

    private suspend fun executeWork(): Result {
        val startTime = System.currentTimeMillis()
        val audios = audioRepository.get()
        val playlistItems = playlistItemRepository.get()

        val queryTime = System.currentTimeMillis()

        coroutineScope {
            val saveArtistsJob = async { saveArtists(audios, playlistItems) }
            val saveAlbumsJob = async { saveAlbums(audios, playlistItems) }
            val saveAlbumArtistsJob = async { saveAlbumArtists(audios, playlistItems) }

            saveArtistsJob.start()
            saveAlbumsJob.start()
            saveAlbumArtistsJob.start()

            saveArtistsJob.await()
            saveAlbumsJob.await()
            saveAlbumArtistsJob.await()
        }

        val endTime = System.currentTimeMillis()

        analytics.logEvent(
            AnalyticsEvent(
                type = "audio_classify",
                extras = listOf(
                    AnalyticsEvent.Param("audios", audios.size.toString()),
                    AnalyticsEvent.Param("exist_playlist_items", playlistItems.size.toString()),
                    AnalyticsEvent.Param("query_time", (queryTime - startTime).toString()),
                    AnalyticsEvent.Param("save_time", (endTime - queryTime).toString()),
                    AnalyticsEvent.Param("total_time", (endTime - startTime).toString()),
                )
            )
        )

        return Result.success()
    }

    private val emptyList = listOf("")

    private suspend fun saveAlbums(
        audios: Collection<Audio>,
        playlistItems: Collection<PlaylistItem>
    ) {
        val audiosByAlbum = audios.groupBy { it.album ?: "" }
        val albums = audiosByAlbum.keys

        saveAndReducePlaylists(albums, playlistItems, audios, PlaylistType.ALBUM) {
            if (it.album == null) emptyList
            else listOf(it.album)
        }
    }

    private suspend fun saveArtists(
        audios: Collection<Audio>,
        playlistItems: Collection<PlaylistItem>
    ) {
        val audiosByArtist = audios.groupByMany {
            if (it.artist == null) emptyList
            else it.artist.split("/")
        }
        val artists = audiosByArtist.keys

        saveAndReducePlaylists(artists, playlistItems, audios, PlaylistType.ARTIST) {
            if (it.artist == null) emptyList
            else it.artist.split("/")
        }
    }

    private suspend fun saveAlbumArtists(
        audios: Collection<Audio>,
        playlistItems: Collection<PlaylistItem>
    ) {
        val audiosByArtist = audios.groupByMany {
            if (it.albumArtist == null) emptyList
            else it.albumArtist.split("/")
        }
        val albumArtists = audiosByArtist.keys

        saveAndReducePlaylists(albumArtists, playlistItems, audios, PlaylistType.ALBUM_ARTIST) {
            if (it.albumArtist == null) emptyList
            else it.albumArtist.split("/")
        }
    }

    /**
     * Save and reduce the playlists.
     *
     * @param playlistNames the names of the playlists in the given [type]
     * @param playlistItems existing playlist items
     * @param audios all audios
     * @param type the type of the playlist
     * @param keyExtractor the key extractor to get the keys from the audio
     */
    private suspend fun saveAndReducePlaylists(
        playlistNames: Collection<String>,
        playlistItems: Collection<PlaylistItem>,
        audios: Collection<Audio>,
        type: PlaylistType,
        keyExtractor: (Audio) -> Iterable<String>
    ): Int {
        val existPlaylists = findPlaylists(playlistNames, type)

        // find the playlists & playlistItems that does not exist in the given names
        // to delete them
        val playlistsToDelete = existPlaylists.filter { it.key !in playlistNames }
        val playlistToDeleteIds = playlistsToDelete.values.map { it.id!! }
        val playlistItemsToDelete = playlistItems.filter { playlistItem ->
            playlistItem.playlistId in playlistToDeleteIds
        }.toSet()

        // find the distinct playlist names that need to be created
        val distinct = playlistNames - existPlaylists.keys - playlistsToDelete.keys
        val newPlaylists = createPlaylists(distinct, type)

        playlistItemRepository.delete(playlistItemsToDelete)
        playlistRepository.deleteByIds(playlistToDeleteIds)

        // Playlist.name -> Playlist
        val allPlaylists = existPlaylists - playlistsToDelete.keys + newPlaylists
        val playlistIds = allPlaylists.values.mapTo(hashSetOf()) { it.id }

        // Audio.id -> List<PlaylistItem>

        // Eventually, all existed playlistItems were split into two parts:
        // 1. existedPlaylistItems: the playlistItems that belong to the playlists
        //    that still exist
        // 2. playlistItemsToDelete: the playlistItems that belong to the playlists
        //    that need to be deleted
        val existedPlaylistItems = playlistItems
            .filter { it.playlistId in playlistIds }
            .groupBy { it.audioId }

        val newPlaylistItems = audios.mapNotNull { audio ->
            keyExtractor(audio).map { it ->
                val playlistId = allPlaylists[it]?.id ?: return@mapNotNull null
                val audioPlaylistItems = existedPlaylistItems[audio.id]

                if (audioPlaylistItems != null &&
                    audioPlaylistItems.any {
                        it.playlistId == playlistId
                    }
                ) {
                    // skip if the playlist item already exists
                    return@mapNotNull null
                }

                PlaylistItem(
                    id = null,
                    playlistId = playlistId,
                    audioId = audio.id!!,
                    isTop = false,
                    next = PlaylistItem.NO_ITEM
                )
            }
        }.flatten()

        playlistItemRepository.insertOrUpdate(newPlaylistItems)

        val allPlaylistItems = existedPlaylistItems.values.flatten() + newPlaylistItems

        val updatedPlaylists = collectPlaylistData(
            allPlaylists.values,
            allPlaylistItems,
            audios
        )

        playlistRepository.update(updatedPlaylists)
        return newPlaylistItems.size
    }

    private fun createPlaylists(
        names: List<String>,
        type: PlaylistType
    ): Map<String, Playlist> {
        val time = System.currentTimeMillis()
        val newPlaylists = names.map {
            Playlist(
                id = null,
                name = it,
                type = type,
                createTime = time,
                updateTime = time,
                coverPath = null,
                count = 0,
                duration = 0
            )
        }
        val ids = playlistRepository.insertAndReturn(newPlaylists)
        val newPlaylistsMap = newPlaylists.zip(ids).associate {
            it.first.name to it.first.copy(id = it.second)
        }
        return newPlaylistsMap
    }

    private fun findPlaylists(
        names: Collection<String>,
        type: PlaylistType
    ): Map<String, Playlist> {
        return playlistRepository.getByNames(names, type)
            .associateBy { it.name }
    }

    private fun collectPlaylistData(
        playlists: Collection<Playlist>,
        playlistItems: Collection<PlaylistItem>,
        audios: Collection<Audio>
    ): List<Playlist> {
        val playlistDatas = playlists.associate { it.id!! to PlaylistData() }

        playlistItems.forEach { item ->
            val data = playlistDatas[item.playlistId] ?: return@forEach
            val audio = audios.firstOrNull { it.id == item.audioId } ?: return@forEach

            data.count++
            data.duration += audio.duration

            if (data.coverPath == null) {
                data.coverPath = audioPathRepository
                    .getByAudioId(audio.id!!)
                    .firstOrNull()?.path
            }
        }

        return playlists.mapNotNull { playlist ->
            val data = playlistDatas[playlist.id!!] ?: return@mapNotNull null

            if (data.count == playlist.count &&
                data.duration == playlist.duration &&
                playlist.coverPath != null
            ) {
                return@mapNotNull null
            }

            playlist.copy(
                count = data.count,
                duration = data.duration,
                coverPath = data.coverPath
            )
        }
    }

    data class PlaylistData(
        var count: Int = 0,
        var duration: Long = 0,
        var coverPath: ContentPath? = null
    )

    companion object {
        private const val TAG = WorkerDefaults.TAG_AUDIO_CLASSIFICATION_WORKER

        val WORKER_SPEC = WorkerDefaults.AudioClassificationWorkerSpec

        @JvmStatic
        fun submitWork(
            context: Context
        ): Operation {
            val workRequest = OneTimeWorkRequestBuilder<AudioClassificationWorker>()
                .addTag(TAG)
                .build()
            return WorkManager.getInstance(context)
                .beginUniqueWork(TAG, ExistingWorkPolicy.KEEP, workRequest)
                .enqueue()
        }

        fun <T, K> Iterable<T>.groupByMany(
            keyExtractor: (T) -> Iterable<K>
        ): Map<K, List<T>> = mutableMapOf<K, MutableList<T>>()
            .also { grouping ->
                forEach { item ->
                    keyExtractor(item).forEach { key ->
                        grouping.computeIfAbsent(key) { mutableListOf() }.add(item)
                    }
                }
            }
    }
}