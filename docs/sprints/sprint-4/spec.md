# Sprint 4 Specification — Session Label (Focus Intent)

## Feature Title
**Session Label**: Allow users to name their focus session before starting the timer.

## Background

```gherkin
Background:
  Given the Focus Timer app is open
  And the timer is in the Idle state
  And the timer is set to any valid duration
```

---

## Scenarios

### Scenario 1: User enters a label before starting

```gherkin
Scenario: Label is displayed during an active session
  Given the timer is Idle
  When the user types "Write chapter 3" into the "What are you focusing on?" field
  And the user clicks Start
  Then the timer transitions to Running state
  And the text "Write chapter 3" is displayed below the countdown ring
  And the label input field is disabled (not editable)
```

### Scenario 2: Completion dialog includes the session label

```gherkin
Scenario: Completion dialog shows the session label
  Given the timer is Running with the label "Code review"
  When the countdown reaches zero
  Then the completion dialog appears
  And the dialog body contains "You completed: Code review"
  And the dialog title reads "Time's up!"
```

### Scenario 3: Completion dialog falls back when no label is provided

```gherkin
Scenario: Completion dialog shows generic message when label is empty
  Given the timer is Running with no label entered
  When the countdown reaches zero
  Then the completion dialog appears
  And the dialog body contains "Your focus session is complete."
```

### Scenario 4: Label is cleared on reset

```gherkin
Scenario: Resetting the timer clears the label
  Given the user has entered the label "Deep work block"
  And the timer is Running
  When the user clicks Reset
  Then the timer returns to Idle state
  And the label input field is empty
  And the label input field is enabled and editable
```

### Scenario 5: Label is cleared on dismissing the completion dialog

```gherkin
Scenario: Dismissing the completion dialog clears the label
  Given the timer has just completed with label "Emails"
  And the completion dialog is shown
  When the user clicks OK (or dismisses the dialog)
  Then the timer returns to Idle state
  And the label input field is empty
```
