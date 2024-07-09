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

import android.content.Intent
import android.os.Bundle
import tech.rollw.support.appcompat.AppActivity

/**
 * The entrance of `android:manageSpaceActivity` in AndroidManifest.xml.
 * Will go to [SettingActivity] and carry the [SettingActivity.EXTRA_ROUTE] argument.
 *
 * @author RollW
 */
class ManageSpaceActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar(colorBackground = 0)
        startActivity(buildIntent())
        finish()
    }

    private fun buildIntent(): Intent {
        val intent = Intent(this, SettingActivity::class.java).apply {
            putExtra(SettingActivity.EXTRA_ROUTE, SettingNavigations.ROUTE_STORAGE)
        }
        return intent
    }
}