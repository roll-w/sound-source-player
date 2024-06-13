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
import tech.rollw.player.data.database.PlayerDatabase
import tech.rollw.player.data.database.dao.AutoPrimaryKeyDao

/**
 * @author RollW
 */
abstract class AutoPrimaryKeyRepository<T>(context: Context) :
    PlayerRepository<T>(context) {

    override val dao: AutoPrimaryKeyDao<T> by lazy { getDao(database) }

    abstract override fun getDao(database: PlayerDatabase): AutoPrimaryKeyDao<T>

    fun insertAndReturn(entity: T) = dao.insertAndReturn(entity)

    fun insertAndReturn(entities: Collection<T>): List<Long> =
        if (entities.isEmpty()) emptyList()
        else dao.insertAndReturn(entities)

    fun deleteById(id: Long) = dao.deleteById(id)

    fun deleteByIds(ids: Collection<Long>) = dao.deleteByIds(ids)

    fun getById(id: Long) = dao.getById(id)

    fun getByIds(ids: Collection<Long>) = dao.getByIds(ids)

    fun getByIdsFlow(ids: Collection<Long>) = dao.getByIdsFlow(ids)
}