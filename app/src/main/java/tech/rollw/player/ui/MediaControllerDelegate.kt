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

import android.content.ComponentName
import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.IntDef
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import tech.rollw.player.service.AudioPlayerSessionService
import java.io.Closeable
import java.util.concurrent.Executors

/**
 * A delegate for managing media controller.
 *
 * @author RollW
 */
class MediaControllerDelegate(
    private val context: Context
) : Closeable {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var _mediaController: MediaController? = null

    private var closed = false

    @State
    private var delegateState = STATE_NOT_CREATED

    @State
    val state: Int get() = delegateState

    fun isConnected(): Boolean {
        return delegateState == STATE_CONNECTED
    }

    val mediaController: MediaController
        get() {
            if (_mediaController != null) {
                return _mediaController!!
            }
            if (!this::controllerFuture.isInitialized) {
                throw IllegalStateException("This media connection has not been created.")
            }
            if (closed || controllerFuture.isCancelled) {
                throw IllegalStateException("This media connection has been closed.")
            }
            return try {
                _mediaController = controllerFuture.get()
                _mediaController!!
            } catch (e: Exception) {
                throw IllegalStateException("Failed to get media controller.", e)
            }
        }

    fun create() {
        if (closed) {
            return
        }
        makeStateChanged(STATE_CONNECTING)

        val sessionToken = SessionToken(
            context,
            ComponentName(context, AudioPlayerSessionService::class.java)
        )
        controllerFuture =
            MediaController.Builder(context, sessionToken)
                .buildAsync()
        controllerFuture.addListener({
            _mediaController = controllerFuture.get()
            makeStateChanged(STATE_CONNECTED)
        }, EXECUTOR)
    }

    override fun close() {
        if (closed) {
            return
        }
        try {
            closed = true
            if (controllerFuture.isCancelled) {
                return
            }
            MediaController.releaseFuture(controllerFuture)
        } finally {
            makeStateChanged(STATE_CANCELLED)

            listeners.clear()
        }
    }

    private val listeners = mutableListOf<OnMediaControllerStateChangeListener>()

    fun addOnMediaControllerStateChangeListener(
        listener: OnMediaControllerStateChangeListener
    ) {
        listeners.add(listener)
    }

    fun removeOnMediaControllerStateChangeListener(
        listener: OnMediaControllerStateChangeListener
    ) {
        listeners.remove(listener)
    }

    private fun makeStateChanged(@State state: Int) {
        delegateState = state
        listeners.forEach {
            it.onStateChanged(state)
        }
    }

    interface OnMediaControllerStateChangeListener {
        @AnyThread
        fun onStateChanged(@State state: Int) {}
    }

    @IntDef(
        STATE_NOT_CREATED,
        STATE_CONNECTING,
        STATE_CONNECTED,
        STATE_CANCELLED
    )
    annotation class State

    companion object {
        const val STATE_NOT_CREATED = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
        const val STATE_CANCELLED = 4

        private val EXECUTOR by lazy {
            Executors.newFixedThreadPool(1)
        }
    }
}