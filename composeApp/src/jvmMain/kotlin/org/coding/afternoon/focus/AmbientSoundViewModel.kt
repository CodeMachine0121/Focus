package org.coding.afternoon.focus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Ambient Sound Engine.
 *
 * Owns the lifecycle of [AmbientSoundGenerator] and exposes Compose-observable state for
 * the UI layer ([AmbientSoundScreen]).
 *
 * ## State
 * - [selectedSound] — the currently chosen [AmbientSoundType]; defaults to WHITE_NOISE.
 * - [volume] — output amplitude in [0.0, 1.0]; defaults to 0.5.
 * - [isPlaying] — whether continuous playback is active.
 * - [errorMessage] — non-null when the audio device is unavailable; null otherwise.
 *
 * ## Thread Safety
 * All public methods are designed to be called from the Compose main thread.
 * [AmbientSoundGenerator] runs on a dedicated daemon thread and reads [volume] via a
 * `@Volatile` field. The generator signals errors back via [AmbientSoundGenerator.onError],
 * which posts a state update back to the main thread via [androidx.compose.runtime].
 *
 * ## Independence from FocusTimerViewModel
 * This ViewModel has no reference to [FocusTimerViewModel] and does not observe timer state.
 * The ambient engine runs entirely independently from the Pomodoro timer.
 */
class AmbientSoundViewModel : ViewModel() {

    var selectedSound: AmbientSoundType by mutableStateOf(AmbientSoundType.WHITE_NOISE)
        private set

    var volume: Float by mutableStateOf(0.5f)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    private var generator: AmbientSoundGenerator? = null

    /**
     * Select a new sound type.
     *
     * If playback is currently active, the running generator is stopped and a new one is
     * started immediately with the new sound type. [isPlaying] remains true.
     */
    fun selectSound(type: AmbientSoundType) {
        if (selectedSound == type) return
        selectedSound = type
        if (isPlaying) {
            stopGenerator()
            startGenerator()
        }
    }

    /**
     * Update the output volume.
     *
     * The value is passed directly to the running generator's `@Volatile volume` field,
     * taking effect within the next buffer write cycle (~50 ms) without restarting playback.
     *
     * @param value Amplitude scale in [0.0, 1.0]; values outside this range are clamped.
     */
    @JvmName("updateVolume")
    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
        generator?.volume = volume
    }

    /**
     * Toggle playback on or off.
     *
     * - If stopped, creates a new [AmbientSoundGenerator] and starts it.
     * - If playing, signals the generator to stop and releases the audio device.
     */
    fun togglePlayback() {
        if (isPlaying) {
            stopGenerator()
            isPlaying = false
            errorMessage = null
        } else {
            errorMessage = null
            startGenerator()
        }
    }

    // -----------------------------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------------------------

    private fun startGenerator() {
        val gen = AmbientSoundGenerator(
            soundType = selectedSound,
            onError = {
                // Called from the audio background thread; update Compose state.
                // mutableStateOf writes are thread-safe for snapshot reads, but to be
                // correct we post the minimal state change here.
                isPlaying = false
                errorMessage = "Audio device unavailable. Check system audio settings."
            },
        )
        gen.volume = volume
        generator = gen
        gen.start()
        isPlaying = true
    }

    private fun stopGenerator() {
        generator?.stop()
        generator = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGenerator()
        isPlaying = false
    }
}
