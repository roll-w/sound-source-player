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

package tech.rollw.player.ui.player.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioPath
import tech.rollw.player.data.database.repository.AudioPathRepository
import tech.rollw.player.data.database.repository.AudioRepository
import tech.rollw.player.getApplicationService

/**
 * @author RollW
 */
class AudioListViewModel(
    private val audioRepository: AudioRepository,
    private val audioPathRepository: AudioPathRepository
) : ViewModel() {

    val audios: Flow<List<Audio>> = audioRepository.getFlow()
        .distinctUntilChanged()

    val audioPaths: Flow<List<AudioPath>> = audioPathRepository.getFlow().distinctUntilChanged()

    fun getAudioPath(audio: Audio): Flow<List<AudioPath>> =
        audio.id?.let {
            audioPathRepository.getById(it)
        } ?: throw IllegalArgumentException("Audio id is null.")

    companion object {
        val FACTORY:  ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    as Context
                val audioRepository = context.getApplicationService(AudioRepository::class.java) {
                    AudioRepository(context)
                }
                val audioPathRepository = context.getApplicationService(AudioPathRepository::class.java) {
                    AudioPathRepository(context)
                }
                AudioListViewModel(audioRepository, audioPathRepository)
            }
        }
    }

}