# Sprint 6 Designer Document: Daily Focus Goal Planner

## Overview

This document defines the UI/UX design for the Goals tab introduced in Sprint 6. The Goals tab is the third primary navigation destination alongside Timer and History. Its purpose is to provide a calm, structured planning surface — not a project management tool. The design prioritizes clarity and minimal friction over feature richness.

The app window is fixed at 390 × 780 px (non-resizable). All layout decisions are made within this constraint.

---

## Screen Layout: Goals Tab

The screen is divided into three vertical zones from top to bottom:

1. **Header Zone** — title text + daily progress bar (fixed, not scrollable)
2. **Content Zone** — LazyColumn of GoalCards (scrollable)
3. **FAB** — floating action button anchored to bottom-right (overlays content zone)

The header zone occupies approximately 100dp of vertical space. The remaining space is given to the scrollable goal list. The FAB floats at 16dp from the bottom and right edges using a `Scaffold` with `floatingActionButton`.

---

## Component Breakdown

### 1. GoalCard

A `Card` (Material3) representing a single DailyGoal.

**Layout (Row, verticalAlignment = CenterVertically):**
- Left: Vertical Column with:
  - Title text (16sp, `FontWeight.Medium`)
    - If completed: `TextDecoration.LineThrough`, color = `onSurfaceVariant`
    - If incomplete: no decoration, color = `onSurface`
  - Subtitle text (12sp): "X pomodoro(s)", color = `onSurfaceVariant`
- Right: Row of two `IconButton`s:
  - Complete button: `Icons.Default.CheckCircle` (filled, primary color) if complete; `Icons.Default.RadioButtonUnchecked` (outline, onSurfaceVariant) if incomplete
  - Delete button: `Icons.Default.Delete`, tint = `error` color

**Card styling:**
- Incomplete: `CardDefaults.cardColors()` default (surface)
- Completed: `CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)`
- Elevation: 1dp
- Corner radius: default (12dp via Material3)
- Padding inside card: `horizontal = 16.dp, vertical = 12.dp`
- External margin between cards: `8.dp` via `Arrangement.spacedBy(8.dp)` in LazyColumn

**Accessibility:** Each IconButton has a `contentDescription` ("Mark complete" / "Goal completed" / "Delete goal").

---

### 2. AddGoalDialog

An `AlertDialog` (Material3) shown when the user taps the FAB.

**Title:** "New Goal"

**Content:** `Column` with:
1. `OutlinedTextField` — label: "Goal title", single line, `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)`, max 60 characters enforced via `onValueChange` trim. Below the field, a character counter "X / 60" is shown in 11sp muted text.
2. Vertical spacer (12dp)
3. Row with label text "Estimated Pomodoros" and a pair of small `IconButton`s (`-` and `+`) flanking a bold number display. Value range: 1-10. The `-` button is disabled when value is 1; `+` button disabled when value is 10.

**Buttons:**
- `dismissButton`: "Cancel" — `TextButton`, always enabled, dismisses dialog without saving
- `confirmButton`: "Add Goal" — `Button`, enabled only when title.trim().isNotBlank()

**Dialog width:** fills the dialog container, which Material3 constrains to ~280dp on desktop.

---

### 3. DailyProgressBar

Displayed at the top of the Goals tab, below the screen title.

**Layout (Column, fillMaxWidth):**
1. Row: Label "Today's Goals" (`FontWeight.SemiBold`, 18sp) on left; fraction text "X / Y" (`FontWeight.Bold`, 16sp, primary color) on right
2. Vertical spacer 8dp
3. `LinearProgressIndicator(progress = completed.toFloat() / total.coerceAtLeast(1).toFloat())`, `modifier = Modifier.fillMaxWidth()`, `trackColor = MaterialTheme.colorScheme.surfaceVariant`

When total is 0, the progress indicator shows 0% fill (empty track). When all goals are complete, it shows 100% fill.

---

### 4. EmptyState

Shown in the content zone when the goal list is empty, centered both vertically and horizontally within the available space.

**Layout (Box, fillMaxSize, contentAlignment = Center):**
- `Column(horizontalAlignment = CenterHorizontally)`:
  - Icon: `Icons.Default.FormatListBulleted`, size = 48.dp, tint = `onSurfaceVariant` at 60% alpha
  - Spacer 12dp
  - Text: "No goals yet." (`FontWeight.Medium`, 16sp, `onSurfaceVariant`)
  - Text: "Tap + to plan your day." (14sp, `onSurfaceVariant`)

---

### 5. Goal Limit Banner

When 5 goals are present, a `Text` label appears below the progress bar section:
- Text: "Goal limit reached (5/5)" (12sp, `onSurfaceVariant`, italic)
- The FAB remains visible but is rendered with `alpha = 0.4f` and `enabled = false` to clearly communicate it is inactive without disappearing.

---

## Interaction Flows

### Add Goal Flow

1. User taps FAB.
2. `AddGoalDialog` opens. Title field is empty, Pomodoros stepper shows 1. Confirm button is disabled.
3. User types a goal title. As soon as a non-blank character exists, Confirm button enables.
4. (Optional) User adjusts Pomodoro count using `+` / `-` buttons.
5. User taps "Add Goal".
6. Dialog closes. New `GoalCard` appears at the bottom of the list with an animation (default Compose item placement animation).
7. Progress bar total increments by 1.
8. If this was the 5th goal, FAB becomes disabled.

### Cancel Add Flow

1. User taps FAB → dialog opens.
2. User taps "Cancel" or presses Escape.
3. Dialog closes. No goal is added.

### Mark Complete Flow

1. User taps the circle/check `IconButton` on an incomplete `GoalCard`.
2. The goal's card immediately re-renders with strikethrough title and surface-variant background.
3. The `CheckCircle` icon replaces the empty circle icon.
4. The progress bar's completed count increments by 1.
5. The `LinearProgressIndicator` animates to the new fill value.

### Delete Goal Flow

1. User taps the delete `IconButton` on any `GoalCard`.
2. The card is immediately removed from the list.
3. The progress bar updates (both total and, if it was completed, the completed count).
4. If the list becomes empty, the `EmptyState` component renders.
5. If fewer than 5 goals now exist, the FAB re-enables.

---

## Design Notes

### Colors (Material3 tokens, no custom colors)

| Usage | Token |
|---|---|
| Progress bar fill | `MaterialTheme.colorScheme.primary` |
| Progress bar track | `MaterialTheme.colorScheme.surfaceVariant` |
| Completed card background | `MaterialTheme.colorScheme.surfaceVariant` |
| Completed title text | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Delete icon tint | `MaterialTheme.colorScheme.error` |
| Complete icon (done state) | `MaterialTheme.colorScheme.primary` |
| Complete icon (undone state) | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Empty state icon/text | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Goal count fraction | `MaterialTheme.colorScheme.primary` |

No hardcoded hex values. All colors use MaterialTheme tokens to automatically respect light/dark theme.

### Spacing

- Screen padding: `24.dp` (matches HistoryScreen convention)
- Between progress block and list: `16.dp`
- Between cards: `8.dp` via `Arrangement.spacedBy`
- Inside card content: `horizontal = 16.dp, vertical = 12.dp`
- FAB offset from edges: `16.dp` (default Scaffold FAB placement)

### Material3 Components Used

- `Scaffold` (for FAB placement)
- `FloatingActionButton` with `Icons.Default.Add`
- `AlertDialog`
- `OutlinedTextField`
- `LinearProgressIndicator`
- `Card` + `CardDefaults`
- `IconButton` + `Icons.Default.*`
- `LazyColumn`
- `Text`, `Row`, `Column`, `Box`, `Spacer`

### Animation

Default Compose `animateItemPlacement()` is applied on LazyColumn items for smooth add/remove transitions. The `LinearProgressIndicator` built-in animation handles the fill change.

---

## ASCII Wireframe: Goals Tab (390 × 780 px)

```
+------------------------------------------+
|  Timer  |  History  |  Goals             |  <- PrimaryTabRow (tabs)
+------------------------------------------+
|                                          |
|  Today's Goals              2 / 3        |  <- DailyProgressBar header row
|  [=============================----]     |  <- LinearProgressIndicator
|                                          |
| +--------------------------------------+ |
| | Write sprint 6 spec          [o] [x] | |  <- GoalCard (incomplete)
| | 3 pomodoros                          | |
| +--------------------------------------+ |
|                                          |
| +--------------------------------------+ |
| | ~~Review PR #42~~            [v] [x] | |  <- GoalCard (completed, strikethrough)
| | 1 pomodoro                           | |  <- surface-variant background
| +--------------------------------------+ |
|                                          |
| +--------------------------------------+ |
| | Read chapter 5               [o] [x] | |  <- GoalCard (incomplete)
| | 2 pomodoros                          | |
| +--------------------------------------+ |
|                                          |
|                                          |
|                                          |
|                                          |
|                                   [+ ]   |  <- FAB (bottom-right)
+------------------------------------------+

Legend:
  [o]  = unchecked circle icon (mark complete)
  [v]  = filled check icon (completed)
  [x]  = delete icon
  [+ ] = FloatingActionButton
  ~~text~~ = strikethrough (completed goal)
  [===----] = LinearProgressIndicator (2/3 filled)
```

### Empty State Wireframe

```
+------------------------------------------+
|  Timer  |  History  |  Goals             |
+------------------------------------------+
|                                          |
|  Today's Goals              0 / 0        |
|  [------------------------------------]  |
|                                          |
|                                          |
|                                          |
|                   [=]                    |  <- FormatListBulleted icon (muted)
|              No goals yet.               |
|           Tap + to plan your day.        |
|                                          |
|                                          |
|                                          |
|                                   [+ ]   |
+------------------------------------------+
```
