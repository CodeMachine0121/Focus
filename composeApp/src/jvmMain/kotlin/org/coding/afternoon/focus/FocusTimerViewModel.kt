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

class FocusTimerViewModel : ViewModel() {
    var totalSeconds by mutableStateOf(25 * 60)
        private set
    var remainingSeconds by mutableStateOf(25 * 60)
        private set
    var timerState by mutableStateOf(TimerState.Idle)
        private set

    private var countdownJob: Job? = null
    var onComplete: (() -> Unit)? = null

    val progress: Float
        get() = if (totalSeconds == 0) 1f else remainingSeconds.toFloat() / totalSeconds.toFloat()

    fun setDuration(minutes: Int) {
        if (timerState != TimerState.Idle) return
        val seconds = minutes * 60
        totalSeconds = seconds
        remainingSeconds = seconds
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
            timerState = TimerState.Completed
            onComplete?.invoke()
        }
    }

    fun pause() {
        if (timerState != TimerState.Running) return
        countdownJob?.cancel()
        timerState = TimerState.Paused
    }

    fun reset() {
        countdownJob?.cancel()
        remainingSeconds = totalSeconds
        timerState = TimerState.Idle
    }

    fun dismiss() {
        countdownJob?.cancel()
        timerState = TimerState.Idle
    }
}
