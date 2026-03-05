package org.coding.afternoon.focus

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.coding.afternoon.focus.DailyGoal.Companion.isToday
import org.coding.afternoon.focus.DailyGoal.Companion.toJson
import org.coding.afternoon.focus.DailyGoal.Companion.fromJson
import java.util.prefs.Preferences
import java.util.UUID

/**
 * Persists [DailyGoal]s for the current calendar day using Java [Preferences].
 *
 * Goals are stored as a newline-delimited list of JSON-encoded strings under a single key,
 * following the same pattern used by [SessionRepository]. On every load, goals that do not
 * belong to today's calendar date are silently discarded.
 *
 * The public [goals] property is a Compose [SnapshotStateList], so any UI observing it will
 * recompose automatically when goals are added, removed, or updated.
 */
class GoalRepository {

    private val prefs: Preferences =
        Preferences.userRoot().node("org/coding/afternoon/focus/goals")

    private val KEY = "daily_goals"

    /** Live snapshot list of today's goals; drives Compose UI recomposition. */
    val goals: SnapshotStateList<DailyGoal> = mutableStateListOf<DailyGoal>().also { list ->
        list.addAll(loadTodayGoals())
    }

    // ---- Public API ----

    /**
     * Loads today's goals from Preferences. Goals from prior days are filtered out.
     * This is called once on construction to seed [goals]; callers can also call it
     * directly to force a refresh.
     */
    fun loadTodayGoals(): List<DailyGoal> {
        val raw = prefs.get(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        val all = raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { fromJson(it) }
        val todayGoals = all.filter { it.isToday() }
        // If stale goals were discarded, persist the cleaned list immediately.
        if (todayGoals.size != all.size) {
            saveAll(todayGoals)
        }
        return todayGoals
    }

    /**
     * Adds a new goal with the given [title] and [estimatedPomodoros].
     *
     * The goal is assigned a random UUID, marked incomplete, and timestamped now.
     * Enforces the maximum of 5 goals — silently no-ops if the limit is already reached.
     *
     * @return The created [DailyGoal], or null if the limit was reached.
     */
    fun addGoal(title: String, estimatedPomodoros: Int): DailyGoal? {
        if (goals.size >= MAX_GOALS) return null
        val trimmedTitle = title.trim().take(60)
        if (trimmedTitle.isBlank()) return null
        val goal = DailyGoal(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            estimatedPomodoros = estimatedPomodoros.coerceIn(1, 10),
            completed = false,
            createdEpochMillis = System.currentTimeMillis(),
        )
        goals.add(goal)
        persist()
        return goal
    }

    /**
     * Marks the goal with the given [id] as completed.
     * If no goal with that id exists, this is a no-op.
     */
    fun markComplete(id: String) {
        val index = goals.indexOfFirst { it.id == id }
        if (index < 0) return
        goals[index] = goals[index].copy(completed = true)
        persist()
    }

    /**
     * Removes the goal with the given [id] from the list and Preferences.
     * If no goal with that id exists, this is a no-op.
     */
    fun deleteGoal(id: String) {
        val removed = goals.removeAll { it.id == id }
        if (removed) persist()
    }

    // ---- Persistence helpers ----

    private fun persist() {
        saveAll(goals.toList())
    }

    private fun saveAll(goalList: List<DailyGoal>) {
        val serialized = goalList.joinToString("\n") { toJson(it) }
        prefs.put(KEY, serialized)
        prefs.flush()
    }

    companion object {
        const val MAX_GOALS = 5
    }
}
