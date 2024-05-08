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

package tech.rollw.player.ui.setting.screen

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import tech.rollw.player.BuildConfig
import tech.rollw.player.R
import tech.rollw.player.ui.AboutActivity
import tech.rollw.player.ui.setting.BasePreferenceFragment
import tech.rollw.player.ui.setting.SettingActivity

class SettingMenuFragment : BasePreferenceFragment() {
    override fun getTitle() = getString(R.string.setting)

    override fun onCreatePreferencesView(savedInstanceState: Bundle?, rootKey: String?) {
        val preferences = setupPreferences()
        val navController = findNavController()
        preferences.ui.setOnPreferenceClickListener {
            navController.navigate(SettingActivity.ROUTE_UI)
            true
        }

        preferences.mediaStore.setOnPreferenceClickListener {
            true
        }

        preferences.storage.setOnPreferenceClickListener {
            navController.navigate(SettingActivity.ROUTE_STORAGE)
            true
        }

        preferences.debug.setOnPreferenceClickListener {
            navController.navigate(SettingActivity.ROUTE_DEBUG)
            true
        }

        preferences.about.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            true
        }
    }

    private fun setupPreferences(): Preferences {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val commonCategory = PreferenceCategory(context).apply {
            title = getString(R.string.setting)
            isIconSpaceReserved = false
        }

        val otherCategory = PreferenceCategory(context).apply {
            title = "Other"
            isIconSpaceReserved = false
        }

        val uiPreference = Preference(context).apply {
            title = getString(R.string.setting_ui)
            summary = "Provides UI customization capabilities."
            isIconSpaceReserved = false
        }

        val storagePreference = Preference(context).apply {
            title = getString(R.string.setting_storage)
            summary = "Provides storage management capabilities."
            isIconSpaceReserved = false
        }

        val mediaStorePreference = Preference(context).apply {
            title = getString(R.string.media_store)
            summary = getString(R.string.media_store)
            isIconSpaceReserved = false
        }

        val debugPreference = Preference(context).apply {
            title = getString(R.string.setting_debug)
            summary = "Debug"
            isIconSpaceReserved = false
            isSelectable = true
        }

        val aboutPreference = Preference(context).apply {
            title = getString(R.string.about)
            summary = BuildConfig.VERSION_NAME + " Type: "+ BuildConfig.BUILD_TYPE
            setIcon(R.drawable.ic_icon_colored)
        }

        screen.apply {
            addPreference(commonCategory)
            addPreference(otherCategory)
            commonCategory.apply {
                addPreference(uiPreference)
                addPreference(mediaStorePreference)
                addPreference(storagePreference)
                addPreference(debugPreference)
            }
            otherCategory.apply {
                addPreference(aboutPreference)
            }
        }

        preferenceScreen = screen
        return Preferences(
            ui = uiPreference,
            storage = storagePreference,
            mediaStore = mediaStorePreference,
            debug = debugPreference,
            about = aboutPreference
        )
    }

    private data class Preferences(
        val ui: Preference,
        val storage: Preference,
        val mediaStore: Preference,
        val debug: Preference,
        val about: Preference
    )

    companion object {
    }
}