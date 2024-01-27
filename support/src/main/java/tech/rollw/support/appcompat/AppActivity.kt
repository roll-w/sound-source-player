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

package tech.rollw.support.appcompat

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity

/**
 * @author RollW
 */
abstract class AppActivity : AppCompatActivity() {
    private val localeDelegate: LocaleDelegate = LocaleDelegate()

    override fun onResume() {
        super.onResume()
        if (localeDelegate.isLocaleChanged) {
            recreate()
        }
    }

    private val activityResultLauncherMap: MutableMap<Class<out ActivityResultContract<*, *>?>,
            ActivityResultContractWrapper<*, *>> = hashMapOf()

    protected fun <I, O> registerActivityLauncher(contract: ActivityResultContract<I, O>) {
        val wrapper = ActivityResultContractWrapper(contract)
        activityResultLauncherMap[contract::class.java] =
            wrapper
    }

    fun <I, O> registerActivityResultCallback(
        clazz: Class<out ActivityResultContract<I, O>?>,
        callback: ActivityResultCallback<O>?,
        unregisterAfterCallback: Boolean = true
    ): ActivityResultLauncher<I> {
        val wrapper = activityResultLauncherMap[clazz] as ActivityResultContractWrapper<I, O>?
            ?: throw IllegalStateException("ActivityResultLauncher have not been registered.")
        wrapper.callbacks.registerOnActivityResultCallback(callback, unregisterAfterCallback)
        return wrapper.launcher
    }

    fun <I, O> unregisterActivityResultCallback(
        clazz: Class<out ActivityResultContract<I, O>?>,
        callback: ActivityResultCallback<O>?
    ) {
        val wrapper = activityResultLauncherMap[clazz] as ActivityResultContractWrapper<I, O>?
            ?: throw IllegalStateException("ActivityResultContract have not been registered.")
        wrapper.callbacks.unregisterOnActivityResultCallback(callback)
    }

    fun <I, O> getActivityResultLauncher(clazz: Class<out ActivityResultContract<I, O>?>): ActivityResultLauncher<I> {
        val wrapper = activityResultLauncherMap[clazz] as ActivityResultContractWrapper<I, O>?
            ?: throw IllegalStateException("ActivityResultContract have not been registered.")
        return wrapper.launcher
    }

    private class ActivityResultCallbacks<O> : ActivityResultCallback<O> {
        private val callbacks: MutableMap<ActivityResultCallback<O>, Boolean> = HashMap()

        fun registerOnActivityResultCallback(callback: ActivityResultCallback<O>?, unregisterAfterCallback: Boolean) {
            if (callback == null) {
                return
            }
            callbacks[callback] = unregisterAfterCallback
        }

        fun unregisterOnActivityResultCallback(callback: ActivityResultCallback<O>?) {
            if (callback == null) {
                return
            }
            callbacks.remove(callback)
        }

        override fun onActivityResult(result: O) {
            val it = callbacks.keys.iterator()
            while (it.hasNext()) {
                val callback = it.next()
                callback.onActivityResult(result)
                if (callbacks[callback] == true) {
                    it.remove()
                }
            }
        }
    }

    private inner class ActivityResultContractWrapper<I, O>(
        contract: ActivityResultContract<I, O>
    ) {
        val callbacks: ActivityResultCallbacks<O> = ActivityResultCallbacks()
        val launcher: ActivityResultLauncher<I> =
            this@AppActivity.registerForActivityResult(contract, callbacks)
    }
}