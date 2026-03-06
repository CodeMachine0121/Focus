package org.coding.afternoon.focus

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.prefs.Preferences

/**
 * Stores and retrieves completed [SessionRecord]s using Java [Preferences].
 *
 * Records are persisted as a newline-delimited list of JSON objects under a single
 * preferences key. The in-memory [sessions] list is a Compose [SnapshotStateList] so
 * the UI recomposes automatically when records are added.
 */
class SessionRepository {
    private val prefs: Preferences =
        Preferences.userRoot().node("org/coding/afternoon/focus/sessions")

    private val KEY = "records"

    /** Most-recent-first ordered snapshot list; drives Compose UI recomposition. */
    val sessions: SnapshotStateList<SessionRecord> = mutableStateListOf<SessionRecord>().also { list ->
        list.addAll(loadFromPrefs())
    }

    /** Record a new session and persist it immediately. */
    fun record(durationMinutes: Int, label: String = "") {
        if (durationMinutes <= 0) return
        val entry = SessionRecord.now(durationMinutes, label)
        sessions.add(0, entry) // prepend so most-recent is first
        saveToPrefs()
    }

    /** Total minutes focused today across all recorded sessions. */
    fun todayTotalMinutes(): Int {
        val today = SessionRecord.today()
        return sessions.filter { it.date == today }.sumOf { it.durationMinutes }
    }

    // ---- persistence helpers ----

    private fun loadFromPrefs(): List<SessionRecord> {
        val raw = prefs.get(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { SessionRecord.fromJson(it) }
            // already stored most-recent-first, but sort defensively
            .sortedByDescending { "${it.date}T${it.startTime}" }
    }

    private fun saveToPrefs() {
        val serialized = sessions.joinToString("\n") { SessionRecord.toJson(it) }
        prefs.put(KEY, serialized)
        prefs.flush()
    }
}
