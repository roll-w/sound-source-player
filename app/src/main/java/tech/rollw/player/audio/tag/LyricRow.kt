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

/**
 * @author RollW
 */
class LyricRow(
    private val raw: String,
    val timestamp: Long = INVALID_TIMESTAMP,
    contents: List<String>
) : Comparable<LyricRow> {
    constructor(raw: String, timestamp: Long, content: String) :
            this(raw, timestamp, listOf(content))

    private val _contents = mutableListOf<String>()

    val contents: List<String>
        get() = _contents

    init {
        _contents.addAll(contents)
    }

    override fun compareTo(other: LyricRow) =
        timestamp.compareTo(other.timestamp)

    operator fun get(index: Int) = contents[index]

    operator fun plusAssign(lrc: LyricRow) {
        if (lrc.timestamp != timestamp) {
            throw IllegalArgumentException("Timestamp not equal.")
        }

        _contents.addAll(lrc.contents)
    }

    operator fun plus(content: String) =
        LyricRow(raw, timestamp, _contents + content)

    operator fun plusAssign(content: String) {
        _contents.add(content)
    }

    operator fun plusAssign(contents: List<String>) {
        _contents.addAll(contents)
    }

    operator fun minusAssign(contents: List<String>) {
        _contents.removeAll(contents)
    }

    operator fun minusAssign(content: String) {
        _contents.remove(content)
    }

    operator fun contains(content: String) = content in _contents

    override fun toString(): String {
        return "LyricRow(raw='$raw', time=$timestamp, contents=$_contents)"
    }

    companion object {
        const val INVALID_TIMESTAMP = -1L

        fun List<LyricRow>.findAt(timestamp: Long): Int {
            var low = 0
            var high = size - 1
            while (low <= high) {
                val mid = (low + high) ushr 1
                val midVal = this[mid].timestamp
                if (midVal < timestamp) {
                    low = mid + 1
                } else if (midVal > timestamp) {
                    high = mid - 1
                } else {
                    return mid
                }
            }
            return high
        }
    }

}
