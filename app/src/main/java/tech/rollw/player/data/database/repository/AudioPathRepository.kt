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

package tech.rollw.player.data.database.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import tech.rollw.player.audio.AudioPath
import tech.rollw.player.data.database.PlayerDatabase
import tech.rollw.player.data.database.dao.AudioPathDao
import tech.rollw.support.io.PathType

/**
 * @author RollW
 */
class AudioPathRepository(
    context: Context
) : PlayerRepository<AudioPath>(context) {

    override fun getDao(database: PlayerDatabase): AudioPathDao {
        return database.getAudioPathDao()
    }

    fun getByPath(path: String, type: PathType) = (dao as AudioPathDao).getByPath(path, type)

    fun getById(id: Long): Flow<List<AudioPath>> = (dao as AudioPathDao).getById(id)

}