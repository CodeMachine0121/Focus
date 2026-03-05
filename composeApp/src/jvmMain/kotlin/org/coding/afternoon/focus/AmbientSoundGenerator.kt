package org.coding.afternoon.focus

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates and streams looping ambient PCM audio for a given [AmbientSoundType].
 *
 * ## Thread Model
 * [start] launches a single daemon thread named "ambient-sound". That thread owns the
 * [SourceDataLine] for its entire lifetime and writes audio in fixed-size buffer chunks
 * ([BUFFER_SAMPLES] samples each). [stop] sets a volatile flag that the thread checks at
 * the top of each loop iteration, so playback stops within one buffer cycle (~50 ms).
 *
 * ## Volume Control
 * [volume] is a `@Volatile` Float in [0.0, 1.0]. The synthesis loop reads it fresh each
 * buffer iteration, so volume changes take effect within the next buffer write without
 * restarting the thread or the audio line.
 *
 * ## Synthesis Algorithms
 * - **White Noise** — each sample is `Random.nextFloat()` scaled to `[-1, 1]`.
 * - **Brown Noise** — a running accumulator integrates white noise, kept bounded via a
 *   soft-clamp so the signal does not drift to silence or clip.
 * - **Rain** — white noise multiplied by a slow amplitude envelope that oscillates using a
 *   low-frequency sine to simulate irregular rainfall intensity.
 * - **Binaural Focus** — two sine waves at BASE_FREQ Hz and (BASE_FREQ + BEAT_FREQ) Hz are
 *   summed and halved. Their phase difference walks at BEAT_FREQ (40 Hz), producing an
 *   audible beating effect in the mono mix.
 *
 * ## Error Handling
 * All exceptions in the audio thread are caught and suppressed. The thread exits cleanly,
 * leaving [onError] set so the ViewModel can surface an error state to the UI.
 */
class AmbientSoundGenerator(
    private val soundType: AmbientSoundType,
    /** Called on the audio thread if the device is unavailable or an error occurs. */
    val onError: () -> Unit = {},
) {

    companion object {
        private const val SAMPLE_RATE = 44100f
        private const val BITS = 16
        private const val CHANNELS = 1
        private const val SIGNED = true
        private const val BIG_ENDIAN = false

        /** Number of PCM samples per write buffer (~46 ms at 44100 Hz). */
        private const val BUFFER_SAMPLES = 2048

        /** Base frequency for Binaural Focus left-channel sine wave (Hz). */
        private const val BINAURAL_BASE_FREQ = 200.0

        /** Beat offset frequency for Binaural Focus (Hz). */
        private const val BINAURAL_BEAT_FREQ = 40.0

        /** Rain envelope oscillation frequency (Hz) — how fast intensity pulses. */
        private const val RAIN_ENVELOPE_FREQ = 1.8

        /** Brown noise soft-clamp threshold — keeps accumulator from drifting. */
        private const val BROWN_CLAMP = 16.0
    }

    /** Current output volume, range [0.0, 1.0]. May be updated at any time from any thread. */
    @Volatile
    var volume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    @Volatile
    private var running = false

    private var thread: Thread? = null

    /** Start continuous playback on a new background thread. No-op if already running. */
    fun start() {
        if (running) return
        running = true
        val t = Thread(::audioLoop, "ambient-sound")
        t.isDaemon = true
        thread = t
        t.start()
    }

    /**
     * Signal the audio thread to stop and release the audio device.
     * Returns immediately; the thread exits within one buffer cycle (~50 ms).
     */
    fun stop() {
        running = false
    }

    // -----------------------------------------------------------------------------------------
    // Audio loop
    // -----------------------------------------------------------------------------------------

    private fun audioLoop() {
        var line: SourceDataLine? = null
        try {
            val format = AudioFormat(SAMPLE_RATE, BITS, CHANNELS, SIGNED, BIG_ENDIAN)
            line = AudioSystem.getSourceDataLine(format)
            line.open(format, BUFFER_SAMPLES * 2 * 2) // 2 bytes/sample, 2 buffer-sizes ahead
            line.start()

            // Mutable generator state (lives on the audio thread only)
            var brownAccum = 0.0          // integrator for Brown noise
            var sampleClock = 0L          // global sample counter for phase-coherent sine waves

            val bufferBytes = ByteArray(BUFFER_SAMPLES * 2) // 16-bit PCM = 2 bytes/sample

            while (running) {
                val vol = volume.toDouble()

                for (i in 0 until BUFFER_SAMPLES) {
                    val sample = when (soundType) {
                        AmbientSoundType.WHITE_NOISE -> generateWhiteNoise(vol)
                        AmbientSoundType.BROWN_NOISE -> {
                            val s = generateBrownNoise(vol, brownAccum)
                            brownAccum = s.second
                            s.first
                        }
                        AmbientSoundType.RAIN -> generateRain(vol, sampleClock + i)
                        AmbientSoundType.BINAURAL_FOCUS -> generateBinaural(vol, sampleClock + i)
                    }

                    val pcm = (sample * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                    bufferBytes[i * 2]     = (pcm.toInt() and 0xFF).toByte()
                    bufferBytes[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
                }

                sampleClock += BUFFER_SAMPLES

                line.write(bufferBytes, 0, bufferBytes.size)
            }

            line.drain()
        } catch (_: Exception) {
            onError()
        } finally {
            try {
                line?.stop()
                line?.close()
            } catch (_: Exception) {
                // Suppress close errors
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Per-type sample generators
    //
    // Each function returns a normalised sample in [-1.0, 1.0] (before volume).
    // -----------------------------------------------------------------------------------------

    /** Flat-spectrum white noise: uniform random samples in [-1, 1]. */
    private fun generateWhiteNoise(vol: Double): Double {
        val raw = (Random.nextFloat() * 2f - 1f).toDouble()
        return raw * vol
    }

    /**
     * Brown noise by integrating white noise.
     * Returns Pair(sample, updatedAccumulator).
     * The accumulator is soft-clamped so it cannot drift more than [BROWN_CLAMP] units,
     * which keeps the normalised output within ±1.0 when divided by BROWN_CLAMP.
     */
    private fun generateBrownNoise(vol: Double, accum: Double): Pair<Double, Double> {
        val white = (Random.nextFloat() * 2f - 1f).toDouble()
        val newAccum = (accum + white).coerceIn(-BROWN_CLAMP, BROWN_CLAMP)
        val normalised = newAccum / BROWN_CLAMP
        return Pair(normalised * vol, newAccum)
    }

    /**
     * Rain simulation: white noise modulated by a slow sinusoidal envelope.
     * The envelope oscillates at [RAIN_ENVELOPE_FREQ] Hz, giving bursts of intensity
     * that loosely mimic rainfall variation. A secondary fast noise component adds texture.
     */
    private fun generateRain(vol: Double, sampleIndex: Long): Double {
        val t = sampleIndex.toDouble() / SAMPLE_RATE
        // Primary slow envelope: 0.25..1.0 range (never fully silent)
        val envelope = 0.625 + 0.375 * sin(2.0 * PI * RAIN_ENVELOPE_FREQ * t)
        val noise = (Random.nextFloat() * 2f - 1f).toDouble()
        return noise * envelope * vol
    }

    /**
     * Binaural Focus: two sine waves at [BINAURAL_BASE_FREQ] and
     * [BINAURAL_BASE_FREQ] + [BINAURAL_BEAT_FREQ]. Their sum creates a 40 Hz
     * amplitude modulation (beating) in the mono output.
     */
    private fun generateBinaural(vol: Double, sampleIndex: Long): Double {
        val t = sampleIndex.toDouble() / SAMPLE_RATE
        val wave1 = sin(2.0 * PI * BINAURAL_BASE_FREQ * t)
        val wave2 = sin(2.0 * PI * (BINAURAL_BASE_FREQ + BINAURAL_BEAT_FREQ) * t)
        val mixed = (wave1 + wave2) * 0.5 // average to stay within [-1, 1]
        return mixed * vol
    }
}
