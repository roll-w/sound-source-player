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

package tech.rollw.player.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import tech.rollw.player.BuildConfig
import tech.rollw.player.util.Logger

/**
 * @author RollW
 */
class CrashHandler(
    private val context: Context,
    private val logger: Logger
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        val contextInfo = collectContextInfo()

        logger.error(
            "CrashHandler",
            "\n=== Crash ===\n" +
                    "Thread: ${t.name}\n" +
                    contextInfo +
                    "\n=== Stack Trace ===\n",
            e
        )
    }

    private fun collectContextInfo(): ContextInfo {
        val activeActivities = mutableListOf<String>()
        val activityManager = context.getSystemService<ActivityManager>()

        activityManager?.appTasks?.forEach { task ->
            task.taskInfo.topActivity?.let { activity ->
                activeActivities += activity.className
            }
        }

        return ContextInfo(
            activeActivities = activeActivities,
            appVersion = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE,
            appBuildType = BuildConfig.BUILD_TYPE,
            appAbi = BuildConfig.ABI,
            osVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            product = Build.PRODUCT,
            device = Build.DEVICE,
            tags = Build.TAGS,
            cpuAbis = Build.SUPPORTED_ABIS.toList()
        )
    }

    private data class ContextInfo(
        val activeActivities: List<String>,
        val appVersion: String,
        val appVersionCode: Int,
        val appBuildType: String,
        val appAbi: String,
        val osVersion: String,
        val sdkVersion: Int,
        val manufacturer: String,
        val model: String,
        val product: String,
        val device: String,
        val tags: String,
        val cpuAbis: List<String>
    ) {
        override fun toString(): String {
            return """
                === Info ===
                App Version        : $appVersion ($appVersionCode) ${appAbi}-${appBuildType}
                Android OS Version : $osVersion (SDK $sdkVersion)
                Manufacturer       : $manufacturer
                Model              : $model
                Product            : $product
                Device             : $device
                Tags               : $tags
                CPU ABIs           : $cpuAbis
                === Activities ===
                - ${activeActivities.joinToString("\n- ")}
            """.trimIndent()
        }
    }

    companion object {
        @JvmStatic
        fun install(context: Context, logger: Logger) {
            Thread.setDefaultUncaughtExceptionHandler(
                CrashHandler(context, logger)
            )
        }
    }
}