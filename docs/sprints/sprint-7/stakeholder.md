# Stakeholder Requirements: Ambient Sound Engine

## Sprint
Sprint 7

## Story Points
8 (Fibonacci)

## User Problem

Knowledge workers using the Focus app rely on external tools — browser tabs playing YouTube lo-fi streams, third-party white noise apps, or Spotify playlists — to generate background audio that supports sustained concentration. This workflow is fragmented: switching between applications breaks focus intent, streaming services require an internet connection, and separate audio apps conflict with the app's own completion chime.

Users need a single, self-contained environment where they can run the Pomodoro timer AND play background ambient sound without leaving the app or depending on external services. The absence of a built-in ambient sound system is a meaningful friction point that reduces the app's ability to serve as a complete focus environment.

## Proposed Feature

A standalone Ambient Sound Engine that generates and plays looping background audio programmatically during focus sessions. The engine produces four scientifically motivated sound types using `javax.sound.sampled` PCM synthesis — no audio files, no network access, no external dependencies required.

Available sound types:
- **White Noise** — flat-spectrum random noise; masks sudden environmental distractions evenly across all frequencies.
- **Brown Noise** — low-frequency biased noise (integrated white noise); warmer, deeper rumble preferred by many concentration-focused users.
- **Rain** — shaped noise bursts with amplitude modulation simulating rainfall; emotionally calming, associated with focus and reading environments.
- **Binaural Focus** — two sine waves delivered at a 40 Hz beat frequency offset; intended to create an auditory beating effect associated with gamma-range brainwave entrainment (presented as experimental / for interest).

The user can select a sound type, adjust output volume via a continuous slider, and toggle playback on/off independently. Playback operates completely independently from the Pomodoro timer — the user can run ambient sound without starting the timer, or run the timer without ambient sound.

A dedicated **Sounds** tab in the app provides all controls. This is a completely new tab and new feature; it does not modify any existing code.

## Acceptance Criteria

1. The app includes a new "Sounds" tab accessible from the main tab bar.
2. The Sounds tab presents four selectable sound types: White Noise, Brown Noise, Rain, Binaural Focus.
3. A volume slider (0% to 100%) controls output amplitude in real time while playing.
4. A Play/Stop button toggles continuous ambient sound playback on and off.
5. The visual state of the Play/Stop button and a status indicator clearly reflect whether audio is currently playing.
6. Playback continues uninterrupted when the user switches to another tab (Timer or History).
7. Playback is completely independent from the timer — starting, pausing, or completing a focus session has no effect on ambient sound.
8. All audio is generated programmatically using `javax.sound.sampled`; no audio asset files are bundled or downloaded.
9. Switching sound type while playing stops the current sound and starts the new one immediately.
10. Adjusting volume while playing takes effect within one audio buffer cycle (no audible restart).
11. If no audio output device is available, the app does not crash; a graceful error state is shown in the UI.
12. Stopping playback releases the audio device (SourceDataLine closed) so other applications can use it.
13. No new dependency on the existing `CompletionSound.kt` or `FocusTimerViewModel.kt`; the ambient engine is entirely self-contained.

## Out of Scope

- Saving user's sound preference across app restarts (future sprint).
- Per-ear binaural routing via stereo channel panning (binaural effect is approximated via amplitude beating).
- Equalizer or frequency-shaping controls.
- Recording or exporting generated audio.
- Timer-triggered ambient sound automation (e.g., auto-start sound when timer starts).
