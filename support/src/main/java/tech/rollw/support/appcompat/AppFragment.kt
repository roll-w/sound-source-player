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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * @author RollW
 */
abstract class AppFragment : Fragment() {
    private var contentView: View? = null

    var cacheView: Boolean = false

    final override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (contentView != null && cacheView) {
            return contentView
        }
        contentView = onCreateFragmentView(
            inflater, container,
            savedInstanceState
        )
        return contentView
    }

    open fun onCreateFragmentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(
            inflater, container,
            savedInstanceState
        )
    }

    override fun getView(): View? {
        return contentView
    }

    @Throws(ClassCastException::class)
    fun requireAppActivity(): AppActivity {
        return super.requireActivity() as AppActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        contentView = null
    }
}