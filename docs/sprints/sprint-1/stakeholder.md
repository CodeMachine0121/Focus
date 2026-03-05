# Stakeholder Requirements: Audio & Sensory Feedback

## User Problem

When a focus session ends, the user may not be looking at the screen — they are heads-down working. The app currently only brings the window to the foreground, which is silent and easily missed if the user is in another room or wearing headphones while listening to music at low volume.

## Proposed Feature

Play a short audible notification sound when the focus timer reaches zero. The sound should be a synthesized multi-tone chime (three ascending notes) generated programmatically using `javax.sound.sampled` — no external audio files required. The chime plays once automatically at session completion and does not repeat.

Additionally, the UI should visually pulse the completion dialog ring color briefly to reinforce the audio cue for users in noisy environments.

## Acceptance Criteria

- When the countdown reaches 0:00, a three-note ascending chime plays automatically via the system audio output.
- The chime is generated programmatically (sine-wave tones via `javax.sound.sampled`) so no bundled audio asset files are needed.
- The chime plays on a background thread and does not block the UI or the Compose main thread.
- If the system audio is unavailable (e.g., no audio device), the app does not crash — the error is caught and silently ignored.
- The chime does not replay if the user dismisses the dialog and starts a new session; it only fires once per completed countdown.

## Out of Scope

- User-configurable volume or tone selection (future sprint).
- Looping or repeating alarms until the user dismisses.
- Notification sounds during the session (e.g., halfway chime).
- System tray / OS notification integration.
- Visual pulse animation on the completion dialog (deferred — audio is the primary deliverable this sprint).
