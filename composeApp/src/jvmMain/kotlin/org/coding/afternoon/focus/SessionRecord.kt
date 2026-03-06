package org.coding.afternoon.focus

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SessionRecord(
    val date: String,        // yyyy-MM-dd
    val startTime: String,   // HH:mm:ss
    val durationMinutes: Int,
    val label: String = "",  // user-supplied session title
) {
    companion object {
        private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss")

        fun now(durationMinutes: Int, label: String = ""): SessionRecord {
            val dt = LocalDateTime.now()
            return SessionRecord(
                date = dt.format(DATE_FMT),
                startTime = dt.format(TIME_FMT),
                durationMinutes = durationMinutes,
                label = label,
            )
        }

        fun today(): String = LocalDate.now().format(DATE_FMT)

        // Minimal JSON encode: no embedded quotes in values assumed safe for our fields.
        fun toJson(record: SessionRecord): String =
            """{"date":"${record.date}","startTime":"${record.startTime}","durationMinutes":${record.durationMinutes},"label":"${record.label}"}"""

        fun fromJson(json: String): SessionRecord? {
            return try {
                val date = extractString(json, "date") ?: return null
                val startTime = extractString(json, "startTime") ?: return null
                val duration = extractInt(json, "durationMinutes") ?: return null
                val label = extractString(json, "label") ?: ""
                SessionRecord(date, startTime, duration, label)
            } catch (_: Exception) {
                null
            }
        }

        private fun extractString(json: String, key: String): String? {
            val pattern = """"$key"\s*:\s*"([^"]*)"""".toRegex()
            return pattern.find(json)?.groupValues?.get(1)
        }

        private fun extractInt(json: String, key: String): Int? {
            val pattern = """"$key"\s*:\s*(\d+)""".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
        }
    }
}
