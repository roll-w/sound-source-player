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
import tech.rollw.player.data.database.PlayerDatabase
import tech.rollw.player.data.database.dao.PlayerDao
import tech.rollw.player.getApplicationService

/**
 * @author RollW
 */
abstract class PlayerRepository<T>(
    protected val context: Context
) {
    protected val database: PlayerDatabase = context.getApplicationService(PlayerDatabase::class.java) {
        PlayerDatabase.getDatabase(context)
    }

    protected open val dao: PlayerDao<T> by lazy {
        getDao(database)
    }

    protected abstract fun getDao(database: PlayerDatabase): PlayerDao<T>

    fun insert(entity: T) = dao.insert(entity)

    fun insert(entities : Collection<T>) = dao.insert(entities)

    fun delete(entity: T) = dao.delete(entity)

    fun delete(entities: Collection<T>) = dao.delete(entities)

    fun update(entity: T) = dao.update(entity)

    fun update(entities: Collection<T>) = dao.update(entities)

    fun get(): List<T> = dao.get()

    fun getFlow(): Flow<List<T>> = dao.getFlow()

    fun insertOrUpdate(entity: T) = dao.insertOrUpdate(entity)

    fun insertOrUpdate(entities: Collection<T>) = dao.insertOrUpdate(entities)
}