package org.coding.afternoon.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Dashboard screen.
 *
 * Loads session history from [SessionRepository] on a background thread, computes
 * [DashboardStats] via [StreakCalculator], and exposes the result as observable
 * Compose state. Re-computes whenever [refresh] is called (e.g. when the tab is
 * selected or a new session is recorded).
 */
class DashboardViewModel(
    private val repository: SessionRepository,
) : ViewModel() {

    /** Null until first load completes; used to distinguish loading from empty. */
    var stats: DashboardStats? by mutableStateOf(null)
        private set

    /** True while a computation is in progress. */
    var isLoading: Boolean by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    /**
     * Triggers a re-computation of [DashboardStats] from the current repository state.
     * Safe to call from the main thread; computation is dispatched to [Dispatchers.Default].
     */
    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            val records = withContext(Dispatchers.Default) {
                // Take a snapshot of the SnapshotStateList to avoid holding a
                // reference on the background thread beyond this call.
                repository.sessions.toList()
            }
            val computed = withContext(Dispatchers.Default) {
                StreakCalculator.compute(records)
            }
            stats = computed
            isLoading = false
        }
    }
}
