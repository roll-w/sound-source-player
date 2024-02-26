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

import android.os.Bundle
import tech.rollw.support.appcompat.AppActivity

abstract class PlayerAppActivity :
    AppActivity(),
    MediaControllerDelegate.OnMediaControllerStateChangeListener {

    private val mediaControllerDelegate: MediaControllerDelegate by lazy {
        MediaControllerDelegate(this).also {
            it.addOnMediaControllerStateChangeListener(this)
        }
    }

    val mediaController get() = mediaControllerDelegate.mediaController

    @MediaControllerDelegate.State
    val mediaControllerState = mediaControllerDelegate.state

    fun isConnected() = mediaControllerDelegate.isConnected()

    override fun onCreate(savedInstanceState: Bundle?) {
        mediaControllerDelegate.create()

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaControllerDelegate.close()
    }

    override fun onStateChanged(state: Int) {
    }
}
