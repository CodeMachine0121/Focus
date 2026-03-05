package org.coding.afternoon.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class TimerState { Idle, Running, Paused, Completed }
enum class TimerPhase { Focus, Break }

class FocusTimerViewModel : ViewModel() {
    companion object {
        const val BREAK_DURATION_MINUTES = 5
    }

    var totalSeconds by mutableStateOf(25 * 60)
        private set
    var remainingSeconds by mutableStateOf(25 * 60)
        private set
    var timerState by mutableStateOf(TimerState.Idle)
        private set
    var sessionLabel by mutableStateOf("")
        private set

    // Auto-Cycle state
    var autoCycleEnabled by mutableStateOf(false)
    var currentPhase by mutableStateOf(TimerPhase.Focus)
        private set
    // cycleCount starts at 1 for the first focus session; increments each time a new focus begins
    var cycleCount by mutableStateOf(1)
        private set

    // Remembers the user-chosen focus duration so break→focus cycles restore it
    private var focusTotalSeconds = 25 * 60

    private var countdownJob: Job? = null
    var onComplete: (() -> Unit)? = null
    /** Called with the duration in minutes when the user dismisses a completed session. */
    var onSessionDismissed: ((Int) -> Unit)? = null

    fun setSessionLabel(label: String) {
        if (timerState != TimerState.Idle) return
        sessionLabel = label.take(60)
    }

    val progress: Float
        get() = if (totalSeconds == 0) 1f else remainingSeconds.toFloat() / totalSeconds.toFloat()

    fun setDuration(minutes: Int) {
        if (timerState != TimerState.Idle) return
        if (minutes <= 0) return
        val seconds = minutes * 60
        totalSeconds = seconds
        remainingSeconds = seconds
        focusTotalSeconds = seconds
    }

    fun start() {
        if (timerState == TimerState.Running || timerState == TimerState.Completed) return
        timerState = TimerState.Running
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.Main) {
            while (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds--
            }
            handleCompletion()
        }
    }

    private fun handleCompletion() {
        if (autoCycleEnabled) {
            when (currentPhase) {
                TimerPhase.Focus -> {
                    // Transition to break
                    currentPhase = TimerPhase.Break
                    val breakSeconds = BREAK_DURATION_MINUTES * 60
                    totalSeconds = breakSeconds
                    remainingSeconds = breakSeconds
                    startInternal()
                }
                TimerPhase.Break -> {
                    // Transition to next focus session
                    cycleCount = cycleCount + 1
                    currentPhase = TimerPhase.Focus
                    totalSeconds = focusTotalSeconds
                    remainingSeconds = focusTotalSeconds
                    startInternal()
                }
            }
        } else {
            timerState = TimerState.Completed
            CompletionSound.play()
            onComplete?.invoke()
        }
    }

    private fun startInternal() {
        timerState = TimerState.Running
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.Main) {
            while (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds--
            }
            handleCompletion()
        }
    }

    fun pause() {
        if (timerState != TimerState.Running) return
        countdownJob?.cancel()
        timerState = TimerState.Paused
    }

    fun reset() {
        countdownJob?.cancel()
        remainingSeconds = focusTotalSeconds
        totalSeconds = focusTotalSeconds
        sessionLabel = ""
        timerState = TimerState.Idle
        currentPhase = TimerPhase.Focus
        cycleCount = 1
    }

    fun dismiss() {
        val completedDuration = totalSeconds / 60
        countdownJob?.cancel()
        remainingSeconds = focusTotalSeconds
        totalSeconds = focusTotalSeconds
        sessionLabel = ""
        timerState = TimerState.Idle
        currentPhase = TimerPhase.Focus
        cycleCount = 1
        onSessionDismissed?.invoke(completedDuration)
    }
}
