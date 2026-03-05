package org.coding.afternoon.focus

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Goals tab.
 *
 * Holds no timer state; delegates all persistence to [GoalRepository]. The goal list
 * is exposed directly from the repository's [SnapshotStateList] so the Compose UI
 * recomposes automatically on any change without additional LiveData wiring.
 *
 * Derived state ([completedCount], [totalCount], [progress], [atLimit]) is computed
 * lazily via [derivedStateOf] — recomposition only triggers when the underlying
 * snapshot list changes.
 */
class GoalViewModel(private val repository: GoalRepository) : ViewModel() {

    /** The live list of today's goals. Backed by a SnapshotStateList. */
    val goals get() = repository.goals

    /** Number of goals marked complete today. */
    val completedCount: Int by derivedStateOf {
        repository.goals.count { it.completed }
    }

    /** Total number of goals for today. */
    val totalCount: Int by derivedStateOf {
        repository.goals.size
    }

    /**
     * Progress fraction in [0f, 1f] for the daily progress bar.
     * Returns 0f when no goals exist.
     */
    val progress: Float by derivedStateOf {
        val total = repository.goals.size
        if (total == 0) 0f else repository.goals.count { it.completed }.toFloat() / total.toFloat()
    }

    /** True when the maximum number of goals has been reached. */
    val atLimit: Boolean by derivedStateOf {
        repository.goals.size >= GoalRepository.MAX_GOALS
    }

    // ---- Actions ----

    /**
     * Adds a new goal. Delegates to [GoalRepository.addGoal].
     *
     * @param title              The goal description (will be trimmed, max 60 chars).
     * @param estimatedPomodoros Estimated Pomodoros count (clamped to 1-10).
     */
    fun addGoal(title: String, estimatedPomodoros: Int) {
        repository.addGoal(title, estimatedPomodoros)
    }

    /**
     * Marks the goal with [id] as completed.
     */
    fun markComplete(id: String) {
        repository.markComplete(id)
    }

    /**
     * Deletes the goal with [id].
     */
    fun deleteGoal(id: String) {
        repository.deleteGoal(id)
    }
}
