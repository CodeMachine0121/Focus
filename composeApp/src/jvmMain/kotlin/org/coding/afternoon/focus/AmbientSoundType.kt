package org.coding.afternoon.focus

/**
 * Enumerates the ambient sound types available in the Ambient Sound Engine.
 *
 * Each type corresponds to a distinct PCM synthesis algorithm in [AmbientSoundGenerator].
 * All synthesis is purely programmatic — no audio asset files are required.
 */
enum class AmbientSoundType(val displayName: String) {
    /** Flat-spectrum random noise; masks broadband environmental distractions. */
    WHITE_NOISE("White Noise"),

    /**
     * Low-frequency biased noise produced by integrating (cumulative-summing) white noise
     * samples. Produces a deep, warm rumble preferred in many concentration environments.
     */
    BROWN_NOISE("Brown Noise"),

    /**
     * Amplitude-modulated shaped noise simulating the irregular pattering of rainfall.
     * Achieved by multiplying white noise bursts against a low-frequency envelope oscillator.
     */
    RAIN("Rain"),

    /**
     * Two sine waves at slightly different frequencies (40 Hz beat offset) that interact to
     * produce an auditory beating effect. Presented as an experimental / curiosity feature.
     * The beating is rendered in the mono mix by summing both waves.
     */
    BINAURAL_FOCUS("Binaural Focus"),
}
