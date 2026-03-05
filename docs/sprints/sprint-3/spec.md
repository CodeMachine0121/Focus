# Sprint 3 Specification

## Feature: Auto-Cycle Mode for Pomodoro Timer

Automatically alternate between focus sessions and break sessions when Auto-Cycle is enabled, eliminating manual interaction between Pomodoro intervals.

---

## Background

```gherkin
Background:
  Given the Focus timer app is running
  And the timer is in the Idle state
  And the default focus duration is 25 minutes
  And the default break duration is 5 minutes
```

---

## Scenarios

### Scenario 1: Auto-Cycle toggle is visible and defaults to OFF

```gherkin
Scenario: Auto-Cycle toggle is visible and off by default
  Given the app has just launched
  When the user views the timer screen
  Then an "Auto-Cycle" toggle control is visible
  And the toggle is in the OFF position
  And the timer behaves with manual (existing) completion flow
```

### Scenario 2: Focus session auto-transitions to break when Auto-Cycle is ON

```gherkin
Scenario: Focus session auto-starts break on completion with Auto-Cycle ON
  Given the user has set a focus duration of 25 minutes
  And the user has turned Auto-Cycle ON
  When the user starts the timer
  And the focus countdown reaches zero
  Then no completion dialog is shown
  And the timer immediately starts counting down from 5 minutes (break phase)
  And the phase label displays "Break"
  And the cycle counter displays "Cycle 1"
```

### Scenario 3: Break session auto-transitions to next focus session

```gherkin
Scenario: Break session auto-starts focus on completion with Auto-Cycle ON
  Given the user has completed one focus session with Auto-Cycle ON
  And the break countdown is now running
  When the break countdown reaches zero
  Then the timer immediately starts a new focus session with the original duration
  And the phase label displays "Focus"
  And the cycle counter increments to "Cycle 2"
```

### Scenario 4: Turning off Auto-Cycle mid-session preserves manual behavior

```gherkin
Scenario: Disabling Auto-Cycle mid-session restores manual completion
  Given Auto-Cycle is ON and a focus session is currently running
  When the user turns Auto-Cycle OFF
  And the focus countdown reaches zero
  Then the completion dialog is shown
  And the timer does not auto-start a break session
  And the timer returns to the Idle state after the user dismisses the dialog
```

### Scenario 5: Pausing during Auto-Cycle stops the current countdown

```gherkin
Scenario: User can pause during an auto-cycle break
  Given Auto-Cycle is ON and the break countdown is running
  When the user clicks the Pause button
  Then the break countdown pauses
  And the phase label still displays "Break"
  And the cycle counter does not change
  And clicking Resume continues the break countdown from where it paused
```

---

## Implementation Notes

- The `autoCycleEnabled` flag and `cycleCount` / `currentPhase` state live in `FocusTimerViewModel`.
- `currentPhase` is an enum: `Focus` or `Break`.
- Break duration is hardcoded to 5 minutes (`BREAK_DURATION_MINUTES = 5`).
- The phase label and cycle counter are displayed below the timer ring in `FocusScreen`.
- The Auto-Cycle toggle should be disabled (grayed out but visible) while a Break phase is running, to avoid ambiguous mid-break toggling — toggling is only allowed during Idle or Focus Running.
- When Auto-Cycle is toggled OFF during a running session, the change takes effect at the next completion boundary (the current countdown continues normally).
