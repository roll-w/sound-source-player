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

package tech.rollw.player

import android.app.Application
import android.content.Context

/**
 * @author RollW
 */
class PlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    private val services: MutableMap<Class<*>, Any> = hashMapOf()

    fun <T : Any> getService(clazz: Class<T>, service: () -> T): T {
        return services.getOrPut(clazz) { service.invoke() } as T
    }

    fun destroyService(clazz: Class<*>) {
        services.remove(clazz)
    }
}

fun <T : Any> Context.getApplicationService(clazz: Class<T>, service: () -> T): T =
    (applicationContext as PlayerApplication).getService(clazz, service)

fun Context.destroyApplicationService(clazz: Class<*>) {
    (applicationContext as PlayerApplication).destroyService(clazz)
}
