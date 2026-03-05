# Feature Specification: Completion Chime Notification

## Feature Title

Audio Chime on Focus Session Completion

## Background

```gherkin
Background:
  Given the Focus timer app is running on a JVM desktop
  And the system has an available audio output device
  And the user has set a timer duration (e.g., 25 minutes)
  And the timer is currently in the Running state
```

## Scenarios

### Scenario 1: Chime plays when timer reaches zero

```gherkin
Scenario: Audible chime fires at session completion
  Given the timer is Running with 1 second remaining
  When 1 second elapses and remainingSeconds reaches 0
  Then the timer state transitions to Completed
  And a three-note ascending chime is played via the system audio output
  And the completion AlertDialog is shown to the user
```

### Scenario 2: Chime does not block the UI thread

```gherkin
Scenario: Audio playback runs off the main thread
  Given the timer has just completed
  When the chime begins playing
  Then the Compose UI remains responsive
  And the completion dialog renders without delay
  And the chime plays on a background (non-main) thread
```

### Scenario 3: App does not crash when audio is unavailable

```gherkin
Scenario: Graceful degradation with no audio device
  Given the system has no available audio output device
  When the timer countdown reaches zero
  Then the timer state transitions to Completed
  And no exception propagates to the UI or crashes the app
  And the completion AlertDialog is still shown normally
```

### Scenario 4: Chime fires only once per completed session

```gherkin
Scenario: Chime does not repeat across multiple sessions
  Given a session has completed and the chime played once
  When the user dismisses the dialog and starts a new session
  And that new session also completes
  Then the chime plays exactly once for the second session
  And no residual audio from the first session overlaps or replays
```

### Scenario 5: Chime is synthesized — no external audio files required

```gherkin
Scenario: Programmatic audio generation with no bundled assets
  Given the app is running from a clean installation directory
  When a focus session completes
  Then the chime plays successfully
  And no .wav, .mp3, or other audio asset files are required on disk
```
