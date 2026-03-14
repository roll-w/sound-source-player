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
import android.os.Process
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import tech.rollw.player.app.CrashHandler
import tech.rollw.player.audio.player.AudioPlaylistProvider
import tech.rollw.player.audio.player.DefaultAudioPlaylistProvider
import tech.rollw.player.data.database.repository.AudioPathRepository
import tech.rollw.player.data.database.repository.AudioRepository
import tech.rollw.player.data.database.repository.PlaylistItemRepository
import tech.rollw.player.data.database.repository.PlaylistRepository
import tech.rollw.player.data.setting.SettingApplyProvider
import tech.rollw.player.data.setting.SettingApplyProviderDelegate
import tech.rollw.player.data.setting.SettingApplyProviderRegistry
import tech.rollw.player.data.setting.SettingKey
import tech.rollw.player.data.setting.preferenceDataStore
import tech.rollw.player.data.storage.BlurredImageFetcher
import tech.rollw.player.data.storage.CommonResources
import tech.rollw.player.data.storage.ContentPathImageFetcher
import tech.rollw.player.data.storage.LocalImageLoader
import tech.rollw.player.service.PlaylistInitializer
import tech.rollw.player.ui.setting.provider.UISettingApplyProvider
import tech.rollw.player.util.FileLogger
import tech.rollw.player.util.Logger
import tech.rollw.player.util.SettingInitializer
import tech.rollw.player.util.today
import tech.rollw.support.analytics.Analytics
import tech.rollw.support.analytics.AndroidLogAnalytics
import tech.rollw.support.analytics.CombinedAnalytics
import tech.rollw.support.analytics.InMemoryAnalytics
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author RollW
 */
class PlayerApplication : Application(),
    ImageLoaderFactory,
    SettingApplyProviderRegistry {

    init {
        APPLICATION = this
    }

    private val serviceConfigs: MutableMap<Class<*>, () -> Any?> =
        hashMapOf<Class<*>, () -> Any?>().apply {
            put(AudioPlaylistProvider::class.java) { DefaultAudioPlaylistProvider() }
            put(LocalImageLoader::class.java) { LocalImageLoader(this@PlayerApplication) }
            put(CommonResources::class.java) { CommonResources(this@PlayerApplication) }
            put(Analytics::class.java) {
                CombinedAnalytics(AndroidLogAnalytics, InMemoryAnalytics())
            }
            put(Logger::class.java) { logger }
            put(AudioRepository::class.java) { AudioRepository(this@PlayerApplication) }
            put(AudioPathRepository::class.java) { AudioPathRepository(this@PlayerApplication) }
            put(PlaylistRepository::class.java) { PlaylistRepository(this@PlayerApplication) }
            put(PlaylistItemRepository::class.java) { PlaylistItemRepository(this@PlayerApplication) }
        }

    private val settingProviderDelegate by lazy {
        SettingApplyProviderDelegate(this)
    }

    override fun onCreate() {
        initServices()
        super.onCreate()

        initSettings()
    }

    private fun initServices() {
        CrashHandler.install(this, logger)
    }

    private fun initSettings() {
        getSettingInitializer().applyDefaults(this)
        settingProviderDelegate.init(dataStore = preferenceDataStore)
        settingProviderDelegate.register(UISettingApplyProvider)
    }

    private fun getSettingInitializer() =
        SettingInitializer(
            this.preferenceDataStore,
            listOf(
                UISettingApplyProvider,
                PlaylistInitializer
            )
        )

    private val services: MutableMap<Class<*>, Any> = hashMapOf()

    fun <T : Any> getService(clazz: Class<T>, service: () -> T?): T {
        if (services.containsKey(clazz)) {
            return services[clazz] as T
        }
        if (serviceConfigs.containsKey(clazz)) {
            val config = serviceConfigs[clazz]!!
            val instance = config() as T
            services[clazz] = instance
            return instance
        }
        val instance = service() ?: throw IllegalStateException("Service not found: $clazz")
        services[clazz] = instance
        return instance
    }

    fun destroyService(clazz: Class<*>) {
        services.remove(clazz)
    }

    private fun getLogFile(): File {
        val dir = File(filesDir.parentFile, "logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val time = today()
        return File(dir, "player-$time.log")
    }

    private val logger: Logger by lazy {
        // TODO: check date
        FileLogger(getLogFile())
    }

    private val workQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val threadPoolExecutor = ThreadPoolExecutor(
        NUMBER_OF_CORES,
        NUMBER_OF_CORES,
        KEEP_ALIVE_TIME.toLong(),
        KEEP_ALIVE_TIME_UNIT,
        workQueue
    )

    fun getThreadPoolExecutor(): ThreadPoolExecutor {
        return threadPoolExecutor
    }

    fun runInBackground(runnable: Runnable) {
        threadPoolExecutor.execute(runnable)
    }

    fun killSelf() {
        Process.killProcess(Process.myPid())
    }

    fun isBackground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED
    }

    fun isForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    fun getProcessLifecycleOwner(): LifecycleOwner {
        return ProcessLifecycleOwner.get()
    }

    companion object {
        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
        private const val KEEP_ALIVE_TIME = 1
        private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS

        lateinit var APPLICATION: PlayerApplication
            private set
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .diskCache(
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)// = 50MB
                    .build()
            )
            .memoryCache(
                MemoryCache.Builder(this)
                    .maxSizeBytes(50 * 1024 * 1024)// = 50MB
                    .maxSizePercent(0.5)
                    .build()
            )
            .components {
                add(ContentPathImageFetcher.Factory())
                add(BlurredImageFetcher.Factory())
            }
            .crossfade(true)
            .build()
    }

    override fun register(provider: SettingApplyProvider) =
        settingProviderDelegate.register(provider)

    override fun unregister(provider: SettingApplyProvider) =
        settingProviderDelegate.unregister(provider)

    override fun getProviders(settingKey: SettingKey<*, *>): List<SettingApplyProvider> =
        settingProviderDelegate.getProviders(settingKey)
}

private val Context.application: PlayerApplication
    get() = applicationContext as PlayerApplication

fun <T : Any> Context.getApplicationService(clazz: Class<T>, service: () -> T? = { null }): T =
    application.getService(clazz, service)

inline fun <reified T : Any> Context.getApplicationService(): T =
    getApplicationService(T::class.java)


fun Context.destroyApplicationService(clazz: Class<*>) {
    application.destroyService(clazz)
}

fun Context.runInBackground(runnable: Runnable) {
    application.runInBackground(runnable)
}

fun Context.killSelf() {
    application.killSelf()
}

fun Context.isBackground(): Boolean {
    return application.isBackground()
}

fun Context.isForeground(): Boolean {
    return application.isForeground()
}
