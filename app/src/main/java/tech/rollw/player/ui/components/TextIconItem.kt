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

package tech.rollw.player.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author RollW
 */
interface TextIconItem {
    val key: String

    var enabled: Boolean

    fun enabledAsFlow(): Flow<Boolean>

    fun getText(context: Context): String

    fun getIcon(context: Context): ImageVector

    companion object {
        fun of(
            key: String,
            text: String,
            icon: ImageVector,
            enabled: Boolean = true
        ): TextIconItem =
            StringTextIconItem(text, icon, key).also {
                it.enabled = enabled
            }

        fun of(
            key: String,
            text: String,
            @DrawableRes iconRes: Int,
            enabled: Boolean = true
        ): TextIconItem =
            StringResTextIconItem(text, iconRes, key).also {
                it.enabled = enabled
            }

        fun of(
            key: String,
            @StringRes textRes: Int,
            @DrawableRes iconRes: Int,
            enabled: Boolean = true
        ): TextIconItem =
            ResTextIconItem(textRes, iconRes, key).also {
                it.enabled = enabled
            }
    }
}

private data class StringTextIconItem(
    private val text: String,
    private val icon: ImageVector,
    override val key: String,
) : TextIconItem {
    private val enabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override var enabled: Boolean
        get() = enabledFlow.value
        set(value) {
            enabledFlow.value = value
        }

    override fun enabledAsFlow(): Flow<Boolean> {
        return enabledFlow
    }

    override fun getText(context: Context): String = text

    override fun getIcon(context: Context): ImageVector = icon
}

private data class ResTextIconItem(
    private val textRes: Int,
    private val icon: Int,
    override val key: String
) : TextIconItem {
    private val enabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override var enabled: Boolean
        get() = enabledFlow.value
        set(value) {
            enabledFlow.value = value
        }

    override fun enabledAsFlow(): Flow<Boolean> {
        return enabledFlow
    }

    override fun getText(context: Context): String = context.getString(textRes)

    override fun getIcon(context: Context): ImageVector =
        ImageVector.vectorResource(
            context.theme,
            context.resources,
            icon
        )
}

private data class StringResTextIconItem(
    private val text: String,
    private val icon: Int,
    override val key: String
) : TextIconItem {
    private val enabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override var enabled: Boolean
        get() = enabledFlow.value
        set(value) {
            enabledFlow.value = value
        }

    override fun enabledAsFlow(): Flow<Boolean> {
        return enabledFlow
    }

    override fun getText(context: Context): String = text

    override fun getIcon(context: Context): ImageVector =
        ImageVector.vectorResource(
            context.theme,
            context.resources,
            icon
        )
}
