# Sprint 2 Specification

## Feature: Session History Log

Track completed focus sessions locally and display a history view so users can monitor their productivity habits over time.

---

## Background

```gherkin
Background:
  Given the Focus Timer desktop app is running
  And the user is on the main timer screen
  And local session storage is available via Java Preferences
```

---

## Scenarios

### Scenario 1: Completed session is recorded and persisted

```gherkin
Scenario: A completed focus session is saved to history
  Given the timer is set to 25 minutes
  And the user starts the timer
  When the countdown reaches zero
  And the completion dialog appears
  And the user dismisses the dialog by clicking "OK"
  Then a session record is saved containing:
    | field     | value                              |
    | date      | today's date (yyyy-MM-dd)          |
    | startTime | the time the session completed     |
    | duration  | 25 (minutes)                       |
  And the record persists after the app is restarted
```

### Scenario 2: History tab shows list of past sessions

```gherkin
Scenario: User views their session history
  Given at least two focus sessions have been completed and recorded
  When the user navigates to the "History" tab
  Then a scrollable list of past sessions is shown
  And each entry displays the session date, time, and duration in minutes
  And the entries are ordered with the most recent session first
  And a "Today" summary at the top shows the total focused minutes for today
```

### Scenario 3: Empty state is shown when no sessions exist

```gherkin
Scenario: No sessions recorded yet
  Given the user has never completed a focus session
  When the user navigates to the "History" tab
  Then the message "No sessions recorded yet." is displayed
  And no session list items are shown
```

### Scenario 4: Today summary accumulates across multiple sessions

```gherkin
Scenario: Today's total updates after each completed session
  Given the user has already completed one 25-minute session today
  And the history tab shows "Today: 25 min"
  When the user completes another 15-minute session
  And navigates to the "History" tab
  Then the "Today" summary shows "Today: 40 min"
  And the new session appears at the top of the list
```

### Scenario 5: Sessions from previous days are visible but excluded from today total

```gherkin
Scenario: Historical sessions from prior days appear but do not affect today total
  Given a session was recorded yesterday with duration 25 minutes
  And no sessions have been completed today
  When the user opens the "History" tab
  Then the yesterday's session appears in the list with its date
  And the "Today" summary shows "Today: 0 min"
```

---

## Implementation Notes

- Use `java.util.prefs.Preferences` for persistence, storing sessions as a JSON-formatted string under a single preferences key.
- The History tab is implemented as a second tab in a `TabRow` within `App.kt`, keeping the timer UI on Tab 0 and history on Tab 1.
- No external libraries are to be added; use Kotlin's standard library for JSON serialization (manual string building or `kotlinx.serialization` if already on classpath).
- The `SessionRepository` class handles read/write of session records, decoupled from the UI.
- When the timer completes and the user dismisses the dialog, `FocusTimerViewModel` calls a callback that the repository uses to record the session.
