package org.coding.afternoon.focus

import java.util.prefs.Preferences

/**
 * Stores and retrieves [ScheduledSession]s using Java [Preferences].
 *
 * Sessions are persisted as a newline-delimited list of pipe-separated strings under
 * a single preferences key, following the same convention used by [SessionRepository]
 * and [GoalRepository].
 *
 * Serialisation format per line:
 *   `"id|label|hour|minute|duration|recurringType|isActive"`
 */
class ScheduleRepository {

    private val prefs: Preferences =
        Preferences.userRoot().node("org/coding/afternoon/focus/schedules")

    private val KEY = "sessions"

    fun save(session: ScheduledSession) {
        val current = loadAll().toMutableList()
        current.add(session)
        persist(current)
    }

    fun loadAll(): List<ScheduledSession> {
        val raw = prefs.get(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { deserialise(it) }
    }

    fun delete(id: String) {
        val updated = loadAll().filter { it.id != id }
        persist(updated)
    }

    fun update(session: ScheduledSession) {
        val updated = loadAll().map { if (it.id == session.id) session else it }
        persist(updated)
    }

    // ---- persistence helpers ----

    private fun persist(sessions: List<ScheduledSession>) {
        prefs.put(KEY, sessions.joinToString("\n") { serialise(it) })
        prefs.flush()
    }

    private fun serialise(s: ScheduledSession): String =
        "${s.id}|${s.label.replace("|", "")}|${s.scheduledHour}|${s.scheduledMinute}" +
        "|${s.durationMinutes}|${s.recurringType.name}|${s.isActive}"

    private fun deserialise(line: String): ScheduledSession? = runCatching {
        val parts = line.split("|")
        if (parts.size < 7) return null
        ScheduledSession(
            id = parts[0],
            label = parts[1],
            scheduledHour = parts[2].toInt(),
            scheduledMinute = parts[3].toInt(),
            durationMinutes = parts[4].toInt(),
            recurringType = RecurringType.valueOf(parts[5]),
            isActive = parts[6].toBoolean()
        )
    }.getOrNull()
}
