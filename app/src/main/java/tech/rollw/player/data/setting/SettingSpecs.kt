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

package tech.rollw.player.data.setting

/**
 * @author RollW
 */
interface SettingSpecs {
    val specs: List<SettingSpec<*, *>>

    operator fun get(key: String): SettingSpec<*, *>? {
        return specs.firstOrNull { it.key.key == key }
    }

    operator fun <T, V> get(key: SettingKey<T, V>): SettingSpec<T, V>? {
        return specs.firstOrNull { it.key == key } as SettingSpec<T, V>?
    }
}

object CommonValues {
    const val AUTO = "auto"
    const val ON = "on"
    const val OFF = "off"
    const val NONE = "none"
}

/**
 * App settings.
 */
object AppSettings : SettingSpecs {
    /**
     * Whether the app has been set up.
     */
    val AppSetup = SettingSpec.boolean(
        "setting:app:setup",
        false
    )

    val GracefulShutdown = SettingSpec.boolean(
        "setting:app:graceful_shutdown",
        true
    )

    val LastPlayedList = SettingSpec(
        SettingKey("setting:app:last_played_list", SettingType.LONG),
        allowAnyValue = true
    )

    val LastPlayedIndex = SettingSpec(
        SettingKey("setting:app:last_played_index", SettingType.INT),
        allowAnyValue = true
    )

    val LastPlayedPosition = SettingSpec(
        SettingKey("setting:app:last_played_position", SettingType.LONG),
        allowAnyValue = true
    )

    override val specs: List<SettingSpec<*, *>>
        get() = listOf(
            AppSetup,
            GracefulShutdown,
            LastPlayedList,
            LastPlayedIndex,
            LastPlayedPosition
        )
}

object UISettings : SettingSpecs {
    val NightMode = SettingSpec(
        SettingKey("setting:ui:night_mode", SettingType.STRING),
        0, CommonValues.AUTO, CommonValues.OFF, CommonValues.ON
    )

    val Language = SettingSpec(
        SettingKey("setting:ui:language", SettingType.STRING),
        0, "system", "en", "zh"
    )

    override val specs: List<SettingSpec<*, *>>
        get() = listOf(
            NightMode,
            Language
        )
}

object MediaStoreSettings : SettingSpecs {
    override val specs: List<SettingSpec<*, *>>
        get() = listOf()
}

object AudioSettings : SettingSpecs {
    override val specs: List<SettingSpec<*, *>>
        get() = listOf()
}

object UserSettings : SettingSpecs {
    val Username = SettingSpec(
        SettingKey("setting:user:username", SettingType.STRING),
        allowAnyValue = true
    )

    override val specs: List<SettingSpec<*, *>>
        get() = listOf(Username)
}

object DebugSettings : SettingSpecs {
    val BackgroundImageEnabled = SettingSpec.boolean(
        "setting:debug:background_image_enabled",
        default = true
    )

    val BackgroundGradientMask = SettingSpec.boolean(
        "setting:debug:background_gradient_mask",
        default = false
    )

    val BackgroundGradientEnabled = SettingSpec.boolean(
        "setting:debug:background_gradient_enabled",
        default = false
    )

    /**
     * Always show [tech.rollw.player.ui.player.SetupActivity] when the app starts.
     */
    val AlwaysShowSetup = SettingSpec.boolean(
        "setting:debug:always_show_setup",
        default = false
    )

    override val specs: List<SettingSpec<*, *>>
        get() = listOf(
            BackgroundImageEnabled,
            BackgroundGradientMask,
            BackgroundGradientEnabled,
            AlwaysShowSetup
        )
}
