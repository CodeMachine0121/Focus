package org.coding.afternoon.focus

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.PI
import kotlin.math.sin

/**
 * Plays a three-note ascending chime synthesized via javax.sound.sampled.
 * No external audio files are required.
 * Playback runs on a dedicated daemon thread so the UI is never blocked.
 */
object CompletionSound {

    private const val SAMPLE_RATE = 44100f
    private const val BITS = 16
    private const val CHANNELS = 1
    private const val SIGNED = true
    private const val BIG_ENDIAN = false

    /** Frequencies (Hz) for the three ascending notes of the chime. */
    private val NOTE_FREQUENCIES = listOf(523.25, 659.26, 783.99) // C5, E5, G5

    /** Duration of each note in milliseconds. */
    private const val NOTE_DURATION_MS = 220

    /** Silence gap between notes in milliseconds. */
    private const val GAP_MS = 60

    fun play() {
        val thread = Thread(::playBlocking, "focus-chime")
        thread.isDaemon = true
        thread.start()
    }

    private fun playBlocking() {
        try {
            val format = AudioFormat(SAMPLE_RATE, BITS, CHANNELS, SIGNED, BIG_ENDIAN)
            val dataLine = AudioSystem.getSourceDataLine(format)
            dataLine.open(format)
            dataLine.start()

            for (freq in NOTE_FREQUENCIES) {
                val noteData = synthesizeTone(freq, NOTE_DURATION_MS)
                dataLine.write(noteData, 0, noteData.size)
                val gapData = synthesizeTone(0.0, GAP_MS)
                dataLine.write(gapData, 0, gapData.size)
            }

            dataLine.drain()
            dataLine.close()
        } catch (_: Exception) {
            // Audio device unavailable or unsupported — degrade silently.
        }
    }

    /**
     * Synthesizes a sine-wave tone at [frequencyHz] for [durationMs] milliseconds.
     * Uses a simple raised-cosine (Hann) envelope to eliminate clicks at note edges.
     * Returns raw PCM bytes (16-bit little-endian signed).
     */
    private fun synthesizeTone(frequencyHz: Double, durationMs: Int): ByteArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val buffer = ByteArray(numSamples * 2)
        val amplitude = 0.6 // 0..1, headroom below full scale

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Hann envelope: sin^2(pi * i / N) fades in and out smoothly
            val envelope = sin(PI * i / numSamples).let { it * it }
            val sample = if (frequencyHz == 0.0) 0.0
                         else amplitude * envelope * sin(2.0 * PI * frequencyHz * t)
            val pcm = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            // Little-endian: low byte first
            buffer[i * 2]     = (pcm.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }
}
