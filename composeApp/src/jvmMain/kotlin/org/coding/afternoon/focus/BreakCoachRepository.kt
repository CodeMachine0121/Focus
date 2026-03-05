package org.coding.afternoon.focus

import java.time.LocalDate
import java.util.prefs.Preferences

/**
 * Persists the set of break activity IDs that the user has completed today.
 *
 * Data is stored in [java.util.prefs.Preferences] under a date-keyed node so that
 * completed IDs automatically become invisible on a new calendar day.
 *
 * Key format:  `completed_<YYYY-MM-DD>`  →  comma-separated activity ID string
 *
 * Example stored value:
 *   Node key: "completed_2026-03-05"
 *   Value:    "neck_roll,box_breathing,drink_water"
 */
class BreakCoachRepository {

    private val prefs: Preferences =
        Preferences.userRoot().node("org/coding/afternoon/focus/break_coach")

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Records [activityId] as completed for today.
     * If the ID is already in the set, the call is a no-op.
     */
    fun markDone(activityId: String) {
        val current = getTodayCompletedIds().toMutableSet()
        if (current.add(activityId)) {
            prefs.put(todayKey(), current.joinToString(","))
            prefs.flush()
        }
    }

    /**
     * Returns the set of activity IDs that have been marked done on today's calendar date.
     * Returns an empty set if nothing has been completed yet today.
     */
    fun getTodayCompletedIds(): Set<String> {
        val raw = prefs.get(todayKey(), "") ?: ""
        return if (raw.isBlank()) emptySet()
        else raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    /**
     * Removes preference entries for all dates prior to today.
     * Should be called once at app startup to avoid unbounded growth.
     */
    fun clearOldDays() {
        val today = todayKey()
        try {
            val keys = prefs.keys()
            for (key in keys) {
                if (key.startsWith("completed_") && key != today) {
                    prefs.remove(key)
                }
            }
            prefs.flush()
        } catch (_: Exception) {
            // BackingStoreException — safe to ignore; old data will simply be unused.
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun todayKey(): String = "completed_${LocalDate.now()}"
}
