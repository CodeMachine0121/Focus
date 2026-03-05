# Product Spec: Multi-Timer Workspace

## Sprint 10 | Feature: Workspace Tab

---

## Overview

The Multi-Timer Workspace introduces a new "Workspace" tab to the Focus app. It provides up to 6 independent countdown timers displayed in a 2-column grid. Each timer is fully self-contained with its own label, countdown duration, run state, and completion notification. The feature is session-only and does not interact with the existing Pomodoro `FocusTimerViewModel`.

---

## Data Model

### `WorkspaceTimerState` (enum)

| Value       | Description                                                  |
|-------------|--------------------------------------------------------------|
| `Idle`      | Timer created but not yet started, or after reset            |
| `Running`   | Countdown is actively decrementing                           |
| `Paused`    | Countdown suspended; can be resumed                          |
| `Completed` | Countdown reached zero; completion beep and badge shown      |

### `WorkspaceTimer` (data class)

```kotlin
data class WorkspaceTimer(
    val id: String,                          // UUID, assigned at creation
    val label: String,                       // User-supplied label (1-60 chars)
    val totalSeconds: Int,                   // Original duration in seconds (> 0)
    val remainingSeconds: Int,               // Seconds left on clock
    val state: WorkspaceTimerState           // Current lifecycle state
)
```

`WorkspaceTimer` is **immutable**. All state changes produce a new copy via `copy()`.

---

## Gherkin Scenarios

### Feature: Add Timer

```gherkin
Feature: Add a new workspace timer

  Scenario: User opens workspace with no timers
    Given the user opens the "Workspace" tab
    Then the timer grid is empty
    And the "Add Timer" button is visible

  Scenario: User successfully adds a timer
    Given the user is on the Workspace tab
    When the user clicks "Add Timer"
    Then the AddTimerDialog opens
    When the user enters label "Build" and duration "45" minutes
    And clicks "Add"
    Then a new timer card appears in the grid with label "Build" and time "45:00"
    And the timer state is "Idle"

  Scenario: User adds a timer with the minimum valid duration
    Given the AddTimerDialog is open
    When the user enters label "Quick" and duration "1" minutes
    And clicks "Add"
    Then a timer card appears with time "01:00"

  Scenario: User attempts to add a timer with zero duration
    Given the AddTimerDialog is open
    When the user enters duration "0" and clicks "Add"
    Then the "Add" button remains disabled
    And no timer is added

  Scenario: User cancels the AddTimerDialog
    Given the AddTimerDialog is open
    When the user clicks "Cancel"
    Then the dialog closes
    And no timer is added
```

### Feature: Maximum 6 Timers Limit

```gherkin
Feature: Enforce maximum timer count

  Scenario: Add Timer button is disabled at limit
    Given the workspace already contains 6 timers
    Then the "Add Timer" button is disabled or not visible

  Scenario: Sixth timer can still be added
    Given the workspace contains 5 timers
    When the user adds a sixth timer
    Then the grid shows 6 timer cards
    And the "Add Timer" button becomes disabled
```

### Feature: Start Timer

```gherkin
Feature: Start an individual workspace timer

  Scenario: User starts an idle timer
    Given a timer card with label "Review" is in Idle state showing "25:00"
    When the user clicks "Start" on that card
    Then the timer transitions to Running state
    And the countdown decrements every second
    And the progress arc starts moving

  Scenario: Running one timer does not affect other timers
    Given timer A is Idle showing "20:00"
    And timer B is Idle showing "10:00"
    When the user clicks "Start" on timer A
    Then timer A is Running
    And timer B remains Idle showing "10:00"
```

### Feature: Pause Timer

```gherkin
Feature: Pause an individual workspace timer

  Scenario: User pauses a running timer
    Given timer "Review" is Running with "24:35" remaining
    When the user clicks "Pause" on that card
    Then the timer transitions to Paused state
    And the countdown stops decrementing
    And "24:35" is preserved

  Scenario: Pausing one timer does not affect others
    Given timer A is Running
    And timer B is Running
    When the user pauses timer A
    Then timer A is Paused
    And timer B continues Running
```

### Feature: Resume Timer

```gherkin
Feature: Resume a paused workspace timer

  Scenario: User resumes a paused timer
    Given timer "Review" is Paused with "18:42" remaining
    When the user clicks "Resume" on that card
    Then the timer transitions to Running state
    And the countdown resumes from "18:42"
```

### Feature: Reset Timer

```gherkin
Feature: Reset an individual workspace timer

  Scenario: User resets a running timer
    Given timer "Build" is Running with "12:08" remaining (originally 45:00)
    When the user clicks "Reset" on that card
    Then the timer transitions to Idle state
    And the display resets to "45:00"
    And the progress arc is fully filled

  Scenario: User resets a completed timer
    Given timer "Deploy" is in Completed state showing "00:00"
    When the user clicks "Reset" on that card
    Then the timer returns to Idle state
    And the completion badge is cleared
    And the display shows the original duration
```

### Feature: Multiple Timers Running Simultaneously

```gherkin
Feature: Multiple independent timers running at the same time

  Scenario: Three timers run in parallel
    Given timer A has "05:00" remaining and is Idle
    And timer B has "10:00" remaining and is Idle
    And timer C has "03:00" remaining and is Idle
    When the user starts all three timers
    Then after 60 seconds, timer A shows approximately "04:00"
    And timer B shows approximately "09:00"
    And timer C shows approximately "02:00"
    And each timer decremented independently

  Scenario: Starting a second timer does not restart a running timer
    Given timer A is Running with "08:30" remaining
    When the user starts timer B
    Then timer A still shows approximately "08:30" (minus elapsed seconds)
    And timer B begins its own countdown
```

### Feature: Timer Completion Notification

```gherkin
Feature: Completion notification when a workspace timer reaches zero

  Scenario: Timer plays beep on completion
    Given timer "Deploy" is Running with "00:01" remaining
    When 1 second elapses
    Then the timer transitions to Completed state
    And a short 2-note beep plays (distinct from the Pomodoro chime)
    And the timer card shows a "Done" completion badge
    And the display shows "00:00"

  Scenario: Completion of one timer does not affect other timers
    Given timer A transitions to Completed
    And timer B is Running with "05:30" remaining
    Then timer B continues running normally
    And only timer A shows the completion badge
    And only one beep plays (for timer A)
```

### Feature: Remove Timer

```gherkin
Feature: Remove a workspace timer

  Scenario: User removes an idle timer
    Given a timer "Build" is in Idle state
    When the user clicks "Remove" on that card
    Then the card disappears from the grid
    And the "Add Timer" button is re-enabled if it was disabled

  Scenario: User removes a running timer
    Given timer "Cook" is Running
    When the user clicks "Remove" on that card
    Then the countdown coroutine is cancelled immediately
    And the card is removed from the grid

  Scenario: User removes a completed timer
    Given timer "Deploy" is in Completed state
    When the user clicks "Remove"
    Then the card is removed and no beep plays again
```

### Feature: Session-Only Persistence

```gherkin
Feature: Workspace state is session-only

  Scenario: Workspace resets on app restart
    Given the user has added 3 running timers in the Workspace tab
    When the user closes and reopens the application
    Then the Workspace tab is empty
    And no timers are present

  Scenario: Workspace state does not appear in Session History
    Given the user runs and completes a workspace timer
    When the user navigates to the History tab
    Then no entry from the workspace timer appears in session history
```

---

## Integration Note

To expose this feature, a "Workspace" tab must be added to `App.kt` with `WorkspaceScreen(workspaceViewModel)`. This modification is documented in `rd.md` and is intentionally left to the integration step to avoid conflicts with parallel sprint work.
