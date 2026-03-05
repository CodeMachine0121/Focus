package org.coding.afternoon.focus

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Represents a single user-defined focus goal for a calendar day.
 *
 * @param id                 UUID-style unique identifier, immutable after creation.
 * @param title              Short description of the goal (1–60 characters).
 * @param estimatedPomodoros How many Pomodoro sessions the user estimates this goal requires (1–10).
 * @param completed          Whether the user has marked this goal as done.
 * @param createdEpochMillis Epoch milliseconds at creation time; used to determine which calendar
 *                           day this goal belongs to.
 */
data class DailyGoal(
    val id: String,
    val title: String,
    val estimatedPomodoros: Int,
    val completed: Boolean,
    val createdEpochMillis: Long,
) {
    companion object {

        /**
         * Returns true when this goal was created on today's calendar date (system default zone).
         */
        fun DailyGoal.isToday(): Boolean {
            val goalDate = Instant.ofEpochMilli(createdEpochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            return goalDate == LocalDate.now()
        }

        // ---- Minimal JSON serialization (no external library, mirrors SessionRecord pattern) ----

        fun toJson(goal: DailyGoal): String =
            """{"id":"${escape(goal.id)}","title":"${escape(goal.title)}","estimatedPomodoros":${goal.estimatedPomodoros},"completed":${goal.completed},"createdEpochMillis":${goal.createdEpochMillis}}"""

        fun fromJson(json: String): DailyGoal? {
            return try {
                val id = extractString(json, "id") ?: return null
                val title = extractString(json, "title") ?: return null
                val estimatedPomodoros = extractInt(json, "estimatedPomodoros") ?: return null
                val completed = extractBoolean(json, "completed") ?: return null
                val createdEpochMillis = extractLong(json, "createdEpochMillis") ?: return null
                DailyGoal(
                    id = id,
                    title = title,
                    estimatedPomodoros = estimatedPomodoros,
                    completed = completed,
                    createdEpochMillis = createdEpochMillis,
                )
            } catch (_: Exception) {
                null
            }
        }

        /** Escapes backslashes and double-quotes so they are safe inside a JSON string literal. */
        private fun escape(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"")

        private fun extractString(json: String, key: String): String? {
            val pattern = """"$key"\s*:\s*"((?:[^"\\]|\\.)*)"""".toRegex()
            return pattern.find(json)?.groupValues?.get(1)
                ?.replace("\\\"", "\"")
                ?.replace("\\\\", "\\")
        }

        private fun extractInt(json: String, key: String): Int? {
            val pattern = """"$key"\s*:\s*(\d+)""".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
        }

        private fun extractLong(json: String, key: String): Long? {
            val pattern = """"$key"\s*:\s*(\d+)""".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toLongOrNull()
        }

        private fun extractBoolean(json: String, key: String): Boolean? {
            val pattern = """"$key"\s*:\s*(true|false)""".toRegex()
            return pattern.find(json)?.groupValues?.get(1)?.toBooleanStrictOrNull()
        }
    }
}
