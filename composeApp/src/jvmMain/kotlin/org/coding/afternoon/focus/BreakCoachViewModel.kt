package org.coding.afternoon.focus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel for the Break Coach feature.
 *
 * Exposes the filtered list of [BreakActivity] items, today's completion set,
 * the active category filter, and a shuffled-pick index so the UI can highlight
 * a randomly selected card.
 *
 * @param focusTimerViewModel Optional reference to the shared [FocusTimerViewModel].
 *   Used for READ-ONLY observation of [FocusTimerViewModel.currentPhase] to surface
 *   a break-phase snackbar suggestion. This ViewModel never calls start(), pause(),
 *   reset(), or dismiss() on the timer.
 * @param repository Repository responsible for persisting today's completed activity IDs.
 */
class BreakCoachViewModel(
    private val focusTimerViewModel: FocusTimerViewModel? = null,
    private val repository: BreakCoachRepository = BreakCoachRepository(),
) : ViewModel() {

    // ── Category filter ───────────────────────────────────────────────────────

    /** Currently selected category filter. Null means "All". */
    var selectedCategory: BreakCategory? by mutableStateOf(null)
        private set

    // ── Activity list ─────────────────────────────────────────────────────────

    /** Full activity library — never changes after init. */
    val allActivities: List<BreakActivity> = BreakActivityLibrary.allActivities

    /** Activities currently visible given [selectedCategory]. */
    val filteredActivities: List<BreakActivity>
        get() = selectedCategory
            ?.let { cat -> allActivities.filter { it.category == cat } }
            ?: allActivities

    // ── Completion state ──────────────────────────────────────────────────────

    /**
     * IDs of activities completed today. Backed by a [SnapshotStateList] so that
     * Compose recomposes activity cards when the set changes.
     */
    val todayCompletedIds: SnapshotStateList<String> = mutableStateListOf<String>().also { list ->
        list.addAll(repository.getTodayCompletedIds())
    }

    val completedCount: Int get() = todayCompletedIds.size
    val totalCount: Int get() = allActivities.size

    // ── Shuffle ───────────────────────────────────────────────────────────────

    /**
     * Index into [filteredActivities] of the card that should be highlighted after
     * a shuffle action. -1 means no card is currently highlighted.
     */
    var shuffleHighlightIndex: Int by mutableStateOf(-1)
        private set

    // ── Auto-cycle break phase notification ──────────────────────────────────

    /**
     * When Auto-Cycle transitions to Break phase the UI should show a snackbar
     * with this activity suggestion. The UI is responsible for observing this
     * field and clearing it after the snackbar is shown.
     */
    var pendingBreakSuggestion: BreakActivity? by mutableStateOf(null)
        private set

    /**
     * Tracks the last observed phase so we only trigger on a Focus→Break transition.
     */
    private var lastObservedPhase: TimerPhase? = null

    // ── Initialization ────────────────────────────────────────────────────────

    init {
        repository.clearOldDays()
        startPhaseObservation()
    }

    // ── Public actions ────────────────────────────────────────────────────────

    /** Select a category chip. Pass null to select "All". */
    fun selectCategory(category: BreakCategory?) {
        selectedCategory = category
        shuffleHighlightIndex = -1
    }

    /** Mark an activity as done for today. Idempotent. */
    fun markDone(activityId: String) {
        if (!todayCompletedIds.contains(activityId)) {
            repository.markDone(activityId)
            todayCompletedIds.add(activityId)
        }
    }

    /**
     * Choose a random activity from the currently visible list, preferring uncompleted ones.
     * Updates [shuffleHighlightIndex] so the UI can scroll to and highlight the card.
     */
    fun shuffle() {
        val visible = filteredActivities
        if (visible.isEmpty()) return

        val uncompleted = visible.filterNot { todayCompletedIds.contains(it.id) }
        val target = if (uncompleted.isNotEmpty()) uncompleted.random() else visible.random()
        shuffleHighlightIndex = visible.indexOf(target)
    }

    /** Called by the UI after it has displayed [pendingBreakSuggestion] as a snackbar. */
    fun clearBreakSuggestion() {
        pendingBreakSuggestion = null
    }

    // ── Phase observation (read-only) ─────────────────────────────────────────

    /**
     * Polls [FocusTimerViewModel.currentPhase] and emits a [pendingBreakSuggestion]
     * whenever a Focus→Break transition is detected.
     *
     * This uses a coroutine loop with a short polling interval as a lightweight
     * alternative to exposing a Flow from FocusTimerViewModel (which would require
     * modifying that existing file).
     */
    private fun startPhaseObservation() {
        val timerVm = focusTimerViewModel ?: return
        viewModelScope.launch {
            while (true) {
                val phase = timerVm.currentPhase
                if (lastObservedPhase == TimerPhase.Focus && phase == TimerPhase.Break) {
                    if (pendingBreakSuggestion == null) {
                        pendingBreakSuggestion = pickBreakSuggestion()
                    }
                }
                lastObservedPhase = phase
                kotlinx.coroutines.delay(500L)
            }
        }
    }

    private fun pickBreakSuggestion(): BreakActivity {
        val uncompleted = allActivities.filterNot { todayCompletedIds.contains(it.id) }
        return if (uncompleted.isNotEmpty()) uncompleted.random() else allActivities.random()
    }
}
