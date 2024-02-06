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

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.fragment
import com.google.android.material.color.DynamicColors
import tech.rollw.player.R
import tech.rollw.player.databinding.ActivitySettingBinding
import tech.rollw.player.ui.OnCreateViewHost
import tech.rollw.support.Switch
import tech.rollw.support.appcompat.AppActivity

/**
 * @author RollW
 */
class SettingActivity : AppActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val settingViewModel by viewModels<SettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setupActivityLauncher()

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = supportFragmentManager.findFragmentById(
            R.id.fragment_container
        )?.findNavController() ?: throw IllegalStateException("No NavHostFragment found")
        setupNavGraph(navController)
        setupAppBarLayout(navController)

        setStatusBar(
            colorBackground = Color.TRANSPARENT,
            lightStatusBar = Switch.ON
        )

        binding.toolBar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_close -> {
                        finish()
                        true
                    }

                    else -> false
                }
            }
            setNavigationOnClickListener {
                val status = navController.popBackStack()
                if (!status) {
                    finish()
                }
                // onBackPressedDispatcher.onBackPressed()
            }
        }

        settingViewModel.title.value = getString(R.string.setting)
        settingViewModel.title.observe(this) {
            if (it == null) {
                binding.toolBar.title = getString(R.string.setting)
                return@observe
            }
            binding.toolBar.title = it
        }

        navigateIfExtras(navController)
    }

    private fun navigateIfExtras(navController: NavController) {
        if (intent.hasExtra(EXTRA_ROUTE)) {
            val route = intent.getStringExtra(EXTRA_ROUTE) ?: return
            navController.navigate(route)
        }
    }

    private fun setupActivityLauncher() {
        registerActivityLauncher(ActivityResultContracts.OpenDocumentTree())
        registerActivityLauncher(ActivityResultContracts.CreateDocument("*/*"))
        registerActivityLauncher(ActivityResultContracts.OpenDocument())
    }

    private fun setupNavGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            startDestination = ROUTE_MENU
        ) {
            fragment<SettingMenuFragment>(route = ROUTE_MENU) {
                label = "Menu"
            }
            fragment<DebugFragment>(route = ROUTE_DEBUG) {
                label = "Debug"
            }
            fragment<StorageFragment>(route = ROUTE_STORAGE) {
                label = "Storage"
            }
        }
    }


    private val fragmentCreateViewListener = object : OnCreateViewHost.OnCreateViewListener {
        override fun onCreateView(view: View, host: OnCreateViewHost) {
            if (host !is BasePreferenceFragment) {
                return
            }
            binding.appBarLayout.setLiftOnScrollTargetView(host.listView)
        }
    }

    private fun setupAppBarLayout(navController: NavController) {
        binding.appBarLayout.setLiftable(true)
        val navHostFragment = binding.fragmentContainer
            .getFragment<NavHostFragment>()

        navHostFragment.childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment !is OnCreateViewHost) {
                return@addFragmentOnAttachListener
            }
            fragment.addOnCreateViewListener(fragmentCreateViewListener)
        }

        navController.addOnDestinationChangedListener { _, _, _ ->
            val fragments = navHostFragment.childFragmentManager.fragments
            if (fragments.isEmpty()) {
                return@addOnDestinationChangedListener
            }
            val fragment = fragments[0] as BasePreferenceFragment
            fragment.addOnCreateViewListener(fragmentCreateViewListener)
        }
    }

    companion object {
        private const val TAG = "SettingActivity"

        /**
         * Indicate the route to show.
         */
        const val EXTRA_ROUTE = "route"

        const val ROUTE_MENU = "menu"
        const val ROUTE_DEBUG = "debug"
        const val ROUTE_STORAGE = "storage"
    }
}