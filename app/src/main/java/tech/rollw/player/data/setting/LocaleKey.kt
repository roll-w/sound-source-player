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

import java.util.Locale

/**
 * @author RollW
 */
@JvmInline
value class LocaleKey private constructor(val code: String) {
    override fun toString(): String {
        return code
    }

    fun asLocale(): Locale {
        return when (this) {
            System -> Locale.getDefault()
            English -> Locale.ENGLISH
            Chinese -> Locale.CHINESE
            Japanese -> Locale.JAPANESE
            Korean -> Locale.KOREAN
            French -> Locale.FRENCH
            German -> Locale.GERMAN
            Italian -> Locale.ITALIAN
            Spanish -> Locale.forLanguageTag("es")
            Portuguese -> Locale.forLanguageTag("pt")
            Russian -> Locale.forLanguageTag("ru")
            Arabic -> Locale.forLanguageTag("ar")
            else -> Locale.getDefault()
        }
    }

    companion object {
        val System = LocaleKey("system")
        val English = LocaleKey("en")
        val Chinese = LocaleKey("zh")
        val Japanese = LocaleKey("ja")
        val Korean = LocaleKey("ko")
        val French = LocaleKey("fr")
        val German = LocaleKey("de")
        val Italian = LocaleKey("it")
        val Spanish = LocaleKey("es")
        val Portuguese = LocaleKey("pt")
        val Russian = LocaleKey("ru")
        val Arabic = LocaleKey("ar")

        fun fromLocale(locale: String): LocaleKey {
            return when (locale) {
                "en" -> English
                "zh" -> Chinese
                "ja" -> Japanese
                "ko" -> Korean
                "fr" -> French
                "de" -> German
                "it" -> Italian
                "es" -> Spanish
                "pt" -> Portuguese
                "ru" -> Russian
                "ar" -> Arabic
                else -> System
            }
        }
    }
}