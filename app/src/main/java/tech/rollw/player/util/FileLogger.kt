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

package tech.rollw.player.util

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author RollW
 */
class FileLogger(
    val file: File,
    val append: Boolean = true
) : Logger {
    private val printWriter by lazy {
        if (!file.exists()) {
            file.createNewFile()
        }
        if (append) {
            return@lazy PrintWriter(FileOutputStream(file, true), true)
        }
        file.printWriter()
    }

    override fun debug(
        tag: String?, message: String,
        throwable: Throwable?
    ) = printLog(Level.DEBUG, tag, message, throwable)

    override fun error(
        tag: String?, message: String,
        throwable: Throwable?
    ) = printLog(Level.ERROR, tag, message, throwable)

    override fun info(
        tag: String?, message: String,
        throwable: Throwable?
    ) = printLog(Level.INFO, tag, message, throwable)

    override fun warn(
        tag: String?, message: String,
        throwable: Throwable?
    ) = printLog(Level.WARN, tag, message, throwable)

    private fun printLog(
        level: Level,
        tag: String?,
        message: String,
        throwable: Throwable?
    ) {
        val timestamp = System.currentTimeMillis()
        val formatTime = TIME_FORMATTER.format(Date(timestamp))

        printWriter.println(
            "$formatTime ${level.level} --- [${tag ?: "System"}]: $message"
        )
        throwable?.let {
            printWriter.println(
                formatStackTraces(it)
            )
        }
        printWriter.flush()
    }

    private enum class Level(
        val level: String
    ) {
        DEBUG("DEBUG"),
        INFO("INFO "),
        WARN("WARN "),
        ERROR("ERROR")
    }

    companion object {
        private val TIME_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    }
}