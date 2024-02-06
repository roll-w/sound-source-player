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

package tech.rollw.player.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceFragmentCompat
import tech.rollw.player.ui.OnCreateViewHost
import tech.rollw.support.appcompat.AppActivity

/**
 * @author RollW
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat(), OnCreateViewHost {

    private val settingViewModel by activityViewModels<SettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.preferenceDataStore =
            context?.preferenceDataStore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        onCreateViewListeners.forEach {
            it.onCreateView(view, this)
        }
        return view
    }

    protected abstract fun getTitle(): String

    protected open val preferenceResId: Int = 0

    override fun onStart() {
        super.onStart()
        settingViewModel.title.value = getTitle()
    }

    final override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        if (preferenceResId != 0) {
            setPreferencesFromResource(preferenceResId, rootKey)
        }
        onCreatePreferencesView(savedInstanceState, rootKey)
    }

    open fun onCreatePreferencesView(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
    }

    fun requireAppActivity(): AppActivity {
        return super.requireActivity() as AppActivity
    }

    fun requireSettingActivity(): SettingActivity {
        return requireAppActivity() as SettingActivity
    }

    private val onCreateViewListeners = mutableSetOf<OnCreateViewHost.OnCreateViewListener>()

    override fun addOnCreateViewListener(listener: OnCreateViewHost.OnCreateViewListener) {
        onCreateViewListeners.add(listener)
    }

    override fun removeOnCreateViewListener(listener: OnCreateViewHost.OnCreateViewListener) {
        onCreateViewListeners.remove(listener)
    }
}