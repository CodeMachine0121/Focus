# Sprint 6 Specification: Daily Focus Goal Planner

## Feature Title

Daily Focus Goal Planner — structured per-day goal board with Pomodoro estimates, completion tracking, and persistence.

---

## Background

```gherkin
Background:
  Given the Focus desktop app is running
  And the current date is today's calendar date
  And the "Goals" tab is selected
```

---

## Scenarios

### Scenario 1: Empty state on first launch

```gherkin
Scenario: No goals exist for today
  Given no goals have been created today
  When the user navigates to the Goals tab
  Then an empty state message is displayed: "No goals yet. Tap + to add your first goal for today."
  And the progress bar shows "0 / 0"
  And the FAB (add button) is visible and enabled
```

---

### Scenario 2: Add a new goal

```gherkin
Scenario: User adds a valid goal
  Given the Goals tab is active
  And fewer than 5 goals exist for today
  When the user taps the FAB (+) button
  Then the AddGoalDialog appears with:
    | field               | state    |
    | Title input         | empty    |
    | Estimated Pomodoros | value: 1 |
    | Confirm button      | disabled |

  When the user types "Write sprint 6 spec" in the Title field
  Then the Confirm button becomes enabled

  When the user sets Estimated Pomodoros to 3
  And the user taps Confirm
  Then the dialog closes
  And a new goal card appears with title "Write sprint 6 spec" and "3 pomodoros"
  And the progress bar updates to "0 / 1"
```

---

### Scenario 3: Goal title validation

```gherkin
Scenario: Title must be non-empty to confirm
  Given the AddGoalDialog is open
  When the Title field is blank or contains only whitespace
  Then the Confirm button is disabled

Scenario: Title is trimmed to 60 characters maximum
  Given the AddGoalDialog is open
  When the user types a title longer than 60 characters
  Then the input field accepts at most 60 characters
```

---

### Scenario 4: Enforce maximum of 5 goals

```gherkin
Scenario: FAB is disabled at goal limit
  Given 5 goals already exist for today
  When the user views the Goals tab
  Then the FAB is visible but disabled
  And a label reads "Goal limit reached (5/5)"

Scenario: FAB re-enables after a goal is deleted
  Given 5 goals exist for today
  When the user deletes one goal
  Then the FAB becomes enabled again
  And the label no longer shows "Goal limit reached"
```

---

### Scenario 5: Mark a goal as complete

```gherkin
Scenario: User completes a goal
  Given a goal with title "Review PR #42" exists and is incomplete
  When the user taps the complete (checkmark) button on that goal card
  Then the goal's completed status changes to true
  And the goal card title is rendered with a strikethrough style
  And the card background color shifts to a muted/surface-variant tone
  And the progress bar increments by 1

Scenario: Completing all goals shows full progress
  Given 3 goals exist and 2 are already complete
  When the user marks the remaining goal as complete
  Then the progress bar shows "3 / 3"
  And the progress bar indicator is fully filled
```

---

### Scenario 6: Delete a goal

```gherkin
Scenario: User deletes an incomplete goal
  Given a goal with title "Read documentation" exists and is incomplete
  When the user taps the delete (trash) button on that goal card
  Then the goal card is removed from the list
  And the progress bar total decreases by 1
  And if no goals remain, the empty state is shown again

Scenario: User deletes a completed goal
  Given a goal with title "Send weekly report" is marked complete
  When the user taps the delete button on that goal card
  Then the goal is removed
  And the completed count and total both decrease by 1
```

---

### Scenario 7: Goals persist across app restarts (same day)

```gherkin
Scenario: Goals survive a restart on the same calendar day
  Given 3 goals exist for today (1 completed, 2 incomplete)
  When the user quits the app and relaunches it on the same calendar date
  And the user navigates to the Goals tab
  Then all 3 goals are present with their original titles and Pomodoro estimates
  And the 1 previously completed goal is still shown as completed
  And the progress bar shows "1 / 3"
```

---

### Scenario 8: Stale goals from a previous day are discarded

```gherkin
Scenario: Yesterday's goals are not shown today
  Given goals were created and persisted on a previous calendar date
  When the user launches the app on a new calendar day
  And navigates to the Goals tab
  Then no goals from the previous day are displayed
  And the empty state message is shown
  And the progress bar shows "0 / 0"
```

---

## Data Model

### DailyGoal

| Field                | Type    | Constraints                          | Description                              |
|----------------------|---------|--------------------------------------|------------------------------------------|
| `id`                 | String  | UUID v4, immutable after creation    | Unique identifier for this goal          |
| `title`              | String  | 1-60 characters, trimmed             | The goal description entered by the user |
| `estimatedPomodoros` | Int     | 1-10                                 | How many Pomodoros the user estimates    |
| `completed`          | Boolean | Default: false                       | Whether the goal has been marked done    |
| `createdEpochMillis` | Long    | System.currentTimeMillis() at create | Used to determine which calendar day the goal belongs to |

### Day-scoping rule

A goal "belongs to today" if `LocalDate` derived from `createdEpochMillis` (using the system default time zone) equals `LocalDate.now()`. Goals that fail this check are filtered out and removed from Preferences on the next load.

---

## Persistence Strategy

Goals are stored under a dedicated Java Preferences node:
`org/coding/afternoon/focus/goals`

Storage key: `"daily_goals"`
Format: newline-delimited list of JSON-like strings, one goal per line, using the same minimal hand-rolled JSON pattern established by `SessionRecord`.

---

## Acceptance Criteria Checklist

- [ ] "Goals" tab exists and is navigable from the main tab bar.
- [ ] Progress bar displays `completed / total` at the top of the Goals tab.
- [ ] FAB opens AddGoalDialog.
- [ ] AddGoalDialog Confirm is disabled until a non-blank title is entered.
- [ ] Title is capped at 60 characters.
- [ ] Estimated Pomodoros input accepts integers 1-10.
- [ ] New goal appears immediately in the list after confirmation.
- [ ] Goals list is scrollable (LazyColumn).
- [ ] Maximum 5 goals enforced — FAB disabled and labeled when limit is reached.
- [ ] Tapping the complete button on a goal marks it done and updates progress.
- [ ] Completed goals show strikethrough title and muted styling.
- [ ] Tapping the delete button removes the goal from the list and Preferences.
- [ ] Goals created today are loaded from Preferences on startup.
- [ ] Goals from a prior day are silently discarded on load.
- [ ] Empty state message is shown when no goals exist.
- [ ] No existing files are modified.
- [ ] No new dependencies are introduced.
