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

package tech.rollw.player.audio.tag

import java.io.File
import java.util.regex.Pattern

/**
 * Parse the lrc string to [Lyric].
 *
 * [LRC Format](https://en.wikipedia.org/wiki/LRC_(file_format))
 *
 * @author RollW
 */
class LyricParser(
    lyric: String
) {
    private val lines = lyric.lines()

    fun parse(): Lyric {
        val parsed = mutableListOf<LyricRow>()
        lines.forEach {
            val row = parseLine(it)
            if (row != null) {
                parsed.add(row)
            }
        }
        val collapsed = collapseRows(parsed)
        return Lyric(listOf(), collapsed)
    }

    private fun collapseRows(rows: MutableList<LyricRow>): List<LyricRow> {
        rows.sort()
        val collapsed = mutableListOf<LyricRow>()
        val timeLyricMap = hashMapOf<Long, LyricRow>()
        rows.forEach {
            val row = timeLyricMap[it.timestamp]
            if (row == null) {
                timeLyricMap[it.timestamp] = it
            } else {
                row += it
            }
        }

        timeLyricMap.forEach { (_, u) ->
            collapsed.add(u)
        }
        return collapsed.sorted()
    }

    private fun parseLine(line: String): LyricRow? {
        if (line.isEmpty()) {
            return null
        }
        if (line.startsWith(TAG_PREFIX) && line.endsWith(TAG_SUFFIX)) {
            // ignore the tag line for now
            // TODO: parse the tag line to get offset
            return null
        }

        val timestampMatcher = TIMESTAMP_PATTEN.matcher(line)
        if (!timestampMatcher.find()) {
            return LyricRow(line, LyricRow.INVALID_TIMESTAMP, line)
        }
        val timestamp = parseTimestamp(
            timestampMatcher.group()
                .removePrefix(TAG_PREFIX)
                .removeSuffix(TAG_SUFFIX)
        )
        if (timestamp < 0) {
            return LyricRow(line, LyricRow.INVALID_TIMESTAMP, line)
        }
        val lyric = line.substring(timestampMatcher.end())
        return LyricRow(line, timestamp, lyric)
    }


    private fun parseIdTag(tagLine: String): Pair<String, String> {


        return Pair("", "")
    }

    private fun parseTimestamp(timestamp: String): Long {
        val times = timestamp
            .replace('.', ':')
            .split(":")
            .toTypedArray()
        if (times.size < 3) {
            return LyricRow.INVALID_TIMESTAMP
        }
        // mm:ss:SS
        return try {
            times[0].toInt() * 60 * 1000L +
                    times[1].toInt() * 1000 +
                    times[2].toInt()
        } catch (e: NumberFormatException) {
            LyricRow.INVALID_TIMESTAMP
        }
    }

    companion object {
        private const val TAG = "LyricParser"

        private const val TAG_PREFIX = "["
        private const val TAG_SUFFIX = "]"

        private const val TIMESTAMP_REGEX = "\\[\\d*?:\\d*\\.\\d*]"
        private val TIMESTAMP_PATTEN: Pattern = Pattern.compile(TIMESTAMP_REGEX)

        fun createFrom(audioTag: AudioTag): LyricParser? {
            val lyric = audioTag.getTagField(AudioTagField.LYRICS) ?: return null
            return LyricParser(lyric)
        }

        fun createFrom(file: File): LyricParser {
            return LyricParser(file.readText())
        }

        fun createFrom(lyric: String): LyricParser {
            return LyricParser(lyric)
        }
    }

}