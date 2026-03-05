# Product Specification: Ambient Sound Engine

## Sprint
Sprint 7

## Feature Title
Ambient Sound Engine — Programmatic Looping Background Audio

## Description

A self-contained background audio subsystem that synthesizes and streams looping ambient sounds using `javax.sound.sampled`. Exposes a new **Sounds** tab in the app with controls for sound type selection, volume, and playback toggle. The system operates on a dedicated background thread and is fully decoupled from all existing features (timer, history, tray). No audio files are bundled; all PCM samples are generated at runtime.

---

## Data Model

### SoundType Enum

```
SoundType {
    WHITE_NOISE    — flat-spectrum random PCM samples
    BROWN_NOISE    — integrated (low-pass filtered) white noise
    RAIN           — amplitude-modulated noise bursts
    BINAURAL_FOCUS — two sine waves offset by 40 Hz (beating effect)
}
```

### AmbientSoundViewModel State

| Field          | Type       | Default       | Description                              |
|----------------|------------|---------------|------------------------------------------|
| selectedSound  | SoundType  | WHITE_NOISE   | Currently selected sound type            |
| volume         | Float      | 0.5f          | Output amplitude scale, range [0.0, 1.0] |
| isPlaying      | Boolean    | false         | Whether the audio engine is active       |

---

## Gherkin Scenarios

### Background

```gherkin
Background:
  Given the Focus app is running on a JVM desktop
  And the system has a functional audio output device
  And the user has navigated to the "Sounds" tab
```

---

### Scenario 1: Select sound type before playback

```gherkin
Scenario: User selects a sound type while stopped
  Given playback is currently stopped (isPlaying = false)
  When the user selects "Brown Noise" from the sound type selector
  Then selectedSound is updated to BROWN_NOISE
  And the UI shows "Brown Noise" as the active selection
  And no audio playback begins (isPlaying remains false)
```

---

### Scenario 2: Start playback

```gherkin
Scenario: User starts ambient sound playback
  Given selectedSound is WHITE_NOISE
  And volume is 0.6
  And isPlaying is false
  When the user presses the "Play" button
  Then isPlaying transitions to true
  And the button label changes to "Stop"
  And a pulsing visual indicator becomes visible
  And continuous white noise audio begins playing through the system audio output
  And the audio runs on a background (non-main) thread
```

---

### Scenario 3: Stop playback

```gherkin
Scenario: User stops ambient sound playback
  Given isPlaying is true
  And white noise audio is playing continuously
  When the user presses the "Stop" button
  Then isPlaying transitions to false
  And audio output stops within one buffer cycle
  And the SourceDataLine is closed and the audio device is released
  And the button label changes to "Play"
  And the pulsing visual indicator disappears
```

---

### Scenario 4: Adjust volume while playing

```gherkin
Scenario: User adjusts volume slider during playback
  Given isPlaying is true
  And volume is currently 0.5
  When the user moves the volume slider to 0.8
  Then volume is updated to 0.8
  And the audio output amplitude increases accordingly within the next buffer write
  And playback does not stop or restart
  And no audible click or discontinuity occurs (within one buffer transition)
```

---

### Scenario 5: Switch sound type while playing

```gherkin
Scenario: User switches sound type during active playback
  Given isPlaying is true
  And selectedSound is WHITE_NOISE
  When the user selects "Rain" from the sound type selector
  Then the current playback thread is signalled to stop
  And selectedSound is updated to RAIN
  And a new playback thread starts for RAIN sound
  And rain-pattern audio is audible within approximately one buffer cycle
  And isPlaying remains true throughout the transition
```

---

### Scenario 6: Playback independence from timer

```gherkin
Scenario: Timer state changes do not affect ambient sound
  Given isPlaying is true with Brown Noise playing
  When the user starts the Pomodoro timer
  And the timer runs and completes
  And the completion chime fires (CompletionSound.play())
  Then ambient Brown Noise continues playing uninterrupted
  And isPlaying remains true
  And the completion chime plays on top of (not instead of) the ambient sound
```

```gherkin
Scenario: Ambient sound does not interfere with timer
  Given a Pomodoro timer is Running
  When the user navigates to the Sounds tab and starts Rain playback
  Then the timer countdown continues unaffected
  And remainingSeconds decrements normally
  And timer state remains Running
```

---

### Scenario 7: Tab switching does not stop playback

```gherkin
Scenario: Navigating away from Sounds tab preserves playback
  Given isPlaying is true and White Noise is audible
  When the user clicks the "Timer" tab
  Then the Sounds tab composable leaves composition
  But ambient audio continues playing
  And isPlaying remains true in AmbientSoundViewModel
  When the user clicks the "Sounds" tab again
  Then the UI shows the correct playing state (isPlaying = true)
  And the pulsing indicator is visible
```

---

### Scenario 8: No audio files required

```gherkin
Scenario: All audio is synthesized at runtime
  Given the app is installed in a directory containing no audio asset files
  When the user selects any sound type and presses Play
  Then audio plays successfully
  And no FileNotFoundException or resource loading error occurs
  And no .wav, .mp3, .ogg, or other audio files are read from disk
```

---

### Scenario 9: Graceful degradation — no audio device

```gherkin
Scenario: App handles missing audio output device gracefully
  Given the system has no available audio output device
  When the user presses the "Play" button
  Then the app does not crash or throw an unhandled exception
  And isPlaying transitions to false (or stays false)
  And an error message or status label is shown in the Sounds tab UI
  And the Timer and History tabs remain fully functional
```

---

### Scenario 10: Volume at zero produces silence

```gherkin
Scenario: Volume slider at minimum produces silent output
  Given isPlaying is true
  When the user moves the volume slider to 0.0
  Then audio samples written to the SourceDataLine are all zero (silent PCM)
  And isPlaying remains true (the thread is still running)
  And the SourceDataLine is not closed
```

---

## Acceptance Criteria Checklist

- [ ] New `AmbientSoundType` sealed class or enum with entries: WhiteNoise, BrownNoise, Rain, BinauralFocus
- [ ] New `AmbientSoundGenerator` class that synthesizes PCM for each type on a background thread
- [ ] `AmbientSoundGenerator` supports real-time volume changes without restarting the audio thread
- [ ] `AmbientSoundGenerator` loops continuously until `stop()` is called
- [ ] `AmbientSoundGenerator` closes the `SourceDataLine` on stop and handles all exceptions silently
- [ ] New `AmbientSoundViewModel` with `selectedSound`, `volume`, `isPlaying` Compose state
- [ ] `AmbientSoundViewModel.togglePlayback()` starts/stops the generator correctly
- [ ] `AmbientSoundViewModel.selectSound(type)` restarts audio if currently playing
- [ ] `AmbientSoundViewModel.setVolume(v)` passes through to the running generator
- [ ] New `AmbientSoundScreen` composable rendering sound selector, volume slider, play/stop button, status indicator
- [ ] `AmbientSoundScreen` uses Material3 components consistent with the existing app style
- [ ] Integration note documented in `rd.md`; `App.kt` is NOT modified by RD
- [ ] No existing file is modified (App.kt, FocusTimerViewModel.kt, FocusScreen.kt, CompletionSound.kt, HistoryScreen.kt)
- [ ] All audio generated via `javax.sound.sampled` with no bundled asset files
- [ ] Build does not require new Gradle dependencies
