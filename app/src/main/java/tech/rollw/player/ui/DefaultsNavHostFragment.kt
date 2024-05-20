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

package tech.rollw.player.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import tech.rollw.player.R

/**
 * @author RollW
 */
// https://stackoverflow.com/questions/50482095
class DefaultsNavHostFragment : NavHostFragment() {
    var defaultNavOptions: NavOptions = DefaultNavOptions

    companion object {
        private val DefaultNavOptions = navOptions {
            anim {
                enter = R.anim.slide_left
                exit = R.anim.wait_anim
                popEnter = R.anim.wait_anim
                popExit = R.anim.slide_right
            }
        }

        private val EmptyNavOptions = navOptions {}
    }

    override fun onCreateNavHostController(
        navHostController: NavHostController
    ) {
        super.onCreateNavHostController(navHostController)
        navHostController.navigatorProvider.addNavigator(
            FragmentNavigatorWithDefaultAnimations(
                requireContext(),
                childFragmentManager,
                id
            )
        )
    }

    @Navigator.Name("fragment")
    private inner class FragmentNavigatorWithDefaultAnimations(
        context: Context,
        manager: FragmentManager,
        containerId: Int
    ) : FragmentNavigator(context, manager, containerId) {
        override fun navigate(
            entries: List<NavBackStackEntry>,
            navOptions: NavOptions?,
            navigatorExtras: Navigator.Extras?
        ) {
            val shouldUseTransitionsInstead = navigatorExtras != null
            val replacedOptions = if (shouldUseTransitionsInstead) navOptions
            else navOptions.fillEmptyAnimationsWithDefaults()

            return super.navigate(entries, replacedOptions, navigatorExtras)
        }

        override fun navigate(
            destination: Destination,
            args: Bundle?,
            navOptions: NavOptions?,
            navigatorExtras: Navigator.Extras?
        ): NavDestination? {
            // this will try to fill in empty animations with defaults when no shared element transitions are set
            // https://developer.android.com/guide/navigation/navigation-animate-transitions#shared-element
            val shouldUseTransitionsInstead = navigatorExtras != null

            val replacedOptions = if (shouldUseTransitionsInstead) navOptions
            else navOptions.fillEmptyAnimationsWithDefaults()

            return super.navigate(destination, args, replacedOptions, navigatorExtras)
        }

        private fun NavOptions?.fillEmptyAnimationsWithDefaults(): NavOptions =
            this?.copyNavOptionsWithDefaultAnimations() ?: defaultNavOptions

        private fun NavOptions.copyNavOptionsWithDefaultAnimations(): NavOptions =
            let { originalNavOptions ->
                navOptions {
                    launchSingleTop = originalNavOptions.shouldLaunchSingleTop()
                    originalNavOptions.popUpToRoute?.let {
                        popUpTo(it) {
                            inclusive = originalNavOptions.isPopUpToInclusive()
                        }
                    }
                    anim {
                        enter = if (originalNavOptions.enterAnim ==
                            EmptyNavOptions.enterAnim
                        ) defaultNavOptions.enterAnim else originalNavOptions.enterAnim

                        exit = if (originalNavOptions.exitAnim ==
                            EmptyNavOptions.exitAnim
                        ) defaultNavOptions.exitAnim else originalNavOptions.exitAnim

                        popEnter = if (originalNavOptions.popEnterAnim ==
                            EmptyNavOptions.popEnterAnim
                        ) defaultNavOptions.popEnterAnim
                        else originalNavOptions.popEnterAnim

                        popExit = if (originalNavOptions.popExitAnim ==
                            EmptyNavOptions.popExitAnim
                        ) defaultNavOptions.popExitAnim
                        else originalNavOptions.popExitAnim
                    }
                }
            }
    }
}

