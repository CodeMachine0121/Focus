package org.coding.afternoon.focus

/**
 * Represents the lifecycle state of a single workspace timer.
 */
enum class WorkspaceTimerState {
    /** Timer has been created (or reset) but not yet started. */
    Idle,

    /** Countdown is actively decrementing. */
    Running,

    /** Countdown has been suspended; can be resumed. */
    Paused,

    /** Countdown reached zero; a completion beep has fired. */
    Completed
}

/**
 * Immutable snapshot of a single workspace timer's state.
 *
 * All mutations are expressed as [copy] calls in [WorkspaceViewModel],
 * which replaces the entry in the SnapshotStateList, triggering Compose recomposition.
 *
 * @param id             Unique identifier (UUID string) assigned at creation time.
 * @param label          User-supplied display label (1–60 chars; defaults to "Timer N").
 * @param totalSeconds   The original countdown duration in seconds (always > 0).
 * @param remainingSeconds Seconds remaining on the clock (0 ≤ remainingSeconds ≤ totalSeconds).
 * @param state          Current lifecycle state of this timer.
 */
data class WorkspaceTimer(
    val id: String,
    val label: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val state: WorkspaceTimerState
) {
    /** Fraction of time elapsed, in [0f, 1f]. Used for the progress arc sweep angle. */
    val progress: Float
        get() = if (totalSeconds == 0) 1f else remainingSeconds.toFloat() / totalSeconds.toFloat()
}
