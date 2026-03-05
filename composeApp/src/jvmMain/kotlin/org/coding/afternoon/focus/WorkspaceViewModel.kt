package org.coding.afternoon.focus

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.PI
import kotlin.math.sin

/**
 * ViewModel that manages a collection of up to [MAX_TIMERS] independent workspace timers.
 *
 * State is held in [timers], a [SnapshotStateList] that Compose observes directly.
 * Each timer owns exactly one coroutine [Job] stored in [timerJobs]; when a timer
 * is paused or removed the job is cancelled immediately to prevent leaks.
 *
 * All state mutations use the immutable-copy pattern: the existing [WorkspaceTimer] entry
 * is replaced via `timers[index] = timers[index].copy(...)`, which triggers Compose
 * recomposition only for the affected card.
 */
class WorkspaceViewModel : ViewModel() {

    companion object {
        const val MAX_TIMERS = 6
        private const val SAMPLE_RATE = 44100f
        private const val BITS = 16
        private const val CHANNELS = 1
        private const val SIGNED = true
        private const val BIG_ENDIAN = false
    }

    /** Observable list of workspace timers. Compose reads this directly. */
    val timers = mutableStateListOf<WorkspaceTimer>()

    /**
     * Maps timer id -> active countdown coroutine Job.
     * Entries are added on start, cancelled and removed on pause/reset/remove/completion.
     */
    private val timerJobs = mutableMapOf<String, Job>()

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Adds a new [WorkspaceTimer] with the given [label] and [minutes] duration.
     * Silently no-ops if the workspace is already at [MAX_TIMERS].
     *
     * If [label] is blank, a default label "Timer N" is generated using the
     * current timer count.
     */
    fun addTimer(label: String, minutes: Int) {
        if (timers.size >= MAX_TIMERS) return
        if (minutes <= 0) return

        val resolvedLabel = label.trim().take(60).ifBlank { "Timer ${timers.size + 1}" }
        val totalSeconds = minutes * 60
        val timer = WorkspaceTimer(
            id = UUID.randomUUID().toString(),
            label = resolvedLabel,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            state = WorkspaceTimerState.Idle
        )
        timers.add(timer)
    }

    /**
     * Removes the timer with [id] from the workspace.
     * Cancels the associated coroutine job if the timer is currently running.
     * No-ops if no such timer exists.
     */
    fun removeTimer(id: String) {
        cancelJob(id)
        val index = timers.indexOfFirst { it.id == id }
        if (index != -1) timers.removeAt(index)
    }

    /**
     * Starts or resumes the timer identified by [id].
     * - Idle → Running: starts a fresh countdown from [WorkspaceTimer.remainingSeconds].
     * - Paused → Running: resumes from the preserved [WorkspaceTimer.remainingSeconds].
     * - Running or Completed: no-op.
     */
    fun startTimer(id: String) {
        val index = timers.indexOfFirst { it.id == id }
        if (index == -1) return
        val timer = timers[index]
        if (timer.state == WorkspaceTimerState.Running || timer.state == WorkspaceTimerState.Completed) return

        timers[index] = timer.copy(state = WorkspaceTimerState.Running)
        launchCountdown(id)
    }

    /**
     * Pauses a running timer identified by [id].
     * Cancels the countdown job; remaining seconds are preserved.
     * No-ops if the timer is not Running.
     */
    fun pauseTimer(id: String) {
        val index = timers.indexOfFirst { it.id == id }
        if (index == -1) return
        val timer = timers[index]
        if (timer.state != WorkspaceTimerState.Running) return

        cancelJob(id)
        timers[index] = timer.copy(state = WorkspaceTimerState.Paused)
    }

    /**
     * Resets the timer identified by [id] back to Idle, restoring full duration.
     * Cancels any active countdown job.
     * No-ops if no such timer exists or the timer is already Idle.
     */
    fun resetTimer(id: String) {
        val index = timers.indexOfFirst { it.id == id }
        if (index == -1) return
        val timer = timers[index]
        if (timer.state == WorkspaceTimerState.Idle) return

        cancelJob(id)
        timers[index] = timer.copy(
            remainingSeconds = timer.totalSeconds,
            state = WorkspaceTimerState.Idle
        )
    }

    // -------------------------------------------------------------------------
    // Internal countdown management
    // -------------------------------------------------------------------------

    /**
     * Launches an independent coroutine on [Dispatchers.Main] that decrements
     * [WorkspaceTimer.remainingSeconds] once per second.
     *
     * On reaching zero it calls [handleCompletion].  The job is stored in
     * [timerJobs] keyed by [id]; any previously-stored job for that id is
     * cancelled first to guard against double-starts.
     */
    private fun launchCountdown(id: String) {
        cancelJob(id)
        val job = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1_000)
                val index = timers.indexOfFirst { it.id == id }
                if (index == -1) break                         // timer was removed
                val timer = timers[index]
                if (timer.state != WorkspaceTimerState.Running) break  // paused externally

                val newRemaining = timer.remainingSeconds - 1
                if (newRemaining <= 0) {
                    timers[index] = timer.copy(
                        remainingSeconds = 0,
                        state = WorkspaceTimerState.Completed
                    )
                    timerJobs.remove(id)
                    handleCompletion()
                    break
                } else {
                    timers[index] = timer.copy(remainingSeconds = newRemaining)
                }
            }
        }
        timerJobs[id] = job
    }

    /**
     * Cancels and removes the countdown job for [id] if one exists.
     */
    private fun cancelJob(id: String) {
        timerJobs.remove(id)?.cancel()
    }

    /**
     * Called when any timer's countdown reaches zero.
     * Plays a short 2-note beep on a daemon thread, distinct from the
     * 3-note [CompletionSound] used by the Pomodoro timer.
     */
    private fun handleCompletion() {
        val thread = Thread(::playCompletionBeep, "workspace-beep")
        thread.isDaemon = true
        thread.start()
    }

    /**
     * Synthesizes and plays a 2-note descending beep using [javax.sound.sampled].
     * Notes: A4 (440 Hz) followed by E4 (329.63 Hz), each 150 ms, with a 40 ms gap.
     * Lower pitch and shorter duration than the Pomodoro chime to distinguish them.
     * Audio failures are silently swallowed.
     */
    private fun playCompletionBeep() {
        try {
            val format = AudioFormat(SAMPLE_RATE, BITS, CHANNELS, SIGNED, BIG_ENDIAN)
            val line = AudioSystem.getSourceDataLine(format)
            line.open(format)
            line.start()

            val notes = listOf(440.0, 329.63)   // A4, E4 — descending two-note beep
            for (freq in notes) {
                val tone = synthesizeTone(freq, durationMs = 150)
                line.write(tone, 0, tone.size)
                val gap = synthesizeTone(0.0, durationMs = 40)
                line.write(gap, 0, gap.size)
            }

            line.drain()
            line.close()
        } catch (_: Exception) {
            // Audio device unavailable — degrade silently.
        }
    }

    /**
     * Produces raw 16-bit little-endian PCM bytes for a sine-wave tone at
     * [frequencyHz] lasting [durationMs] milliseconds.
     * A Hann (raised-cosine) envelope is applied to eliminate clicks at note edges.
     */
    private fun synthesizeTone(frequencyHz: Double, durationMs: Int): ByteArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val buffer = ByteArray(numSamples * 2)
        val amplitude = 0.5

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = sin(PI * i / numSamples).let { it * it }   // Hann window
            val sample = if (frequencyHz == 0.0) 0.0
                         else amplitude * envelope * sin(2.0 * PI * frequencyHz * t)
            val pcm = (sample * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
            buffer[i * 2]     = (pcm.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Cancels ALL active countdown jobs when the ViewModel is cleared.
     * This ensures no coroutines outlive the workspace screen.
     */
    override fun onCleared() {
        super.onCleared()
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
    }
}
