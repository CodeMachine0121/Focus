# Sprint 6 QA Report: Daily Focus Goal Planner

## Feature Summary

Sprint 6 adds a "Goals" tab to the Focus desktop app. Users can plan up to 5 focus goals per calendar day, each with a title and estimated Pomodoro count. Goals can be marked complete or deleted. A progress bar shows daily completion status. Goals are persisted via Java Preferences and are day-scoped (stale goals from prior days are discarded on load).

Implementation spans four new files:

| File | Role |
|---|---|
| `DailyGoal.kt` | Data model + JSON serialization |
| `GoalRepository.kt` | Persistence + SnapshotStateList |
| `GoalViewModel.kt` | Business logic + derived state |
| `GoalScreen.kt` | Compose UI |

No existing files are modified.

---

## Test Scenarios: Manual Code Trace

### Scenario 1: Empty state on first launch

**Spec**: When no goals exist today, show empty state message and a "0 / 0" progress bar.

**Trace**:
- `GoalRepository` construction calls `loadTodayGoals()`. `prefs.get(KEY, "")` returns `""`. `loadTodayGoals()` returns `emptyList()`. `goals` SnapshotStateList is empty.
- `GoalViewModel.totalCount` is `derivedStateOf { goals.size }` → 0.
- `GoalViewModel.completedCount` → 0.
- `GoalViewModel.progress` → `if (total == 0) 0f` → 0f.
- In `GoalScreen`, `goals.isEmpty()` is true → `GoalEmptyState` renders.
- `DailyProgressSection` shows "0 / 0" and `LinearProgressIndicator(progress = { 0f })`.
- FAB is present, `atLimit` is false → FAB is enabled (primaryContainer color).

**Result**: PASS

---

### Scenario 2: Add a new goal

**Spec**: Tapping FAB opens AddGoalDialog. Confirm disabled until non-blank title entered. On confirm, goal appears in list, progress bar updates.

**Trace**:
- `showAddDialog` is false initially; FAB `onClick` sets `showAddDialog = true` (when `!viewModel.atLimit`).
- `AddGoalDialog` renders with `title = ""` and `pomodoros = 1`.
- `isConfirmEnabled = title.trim().isNotBlank()` → `"".trim().isNotBlank()` → false. Confirm `Button(enabled = false)`.
- User types "Write sprint 6 spec" → `title` becomes non-blank → `isConfirmEnabled` → true.
- User taps "Add Goal" → `onConfirm("Write sprint 6 spec", 1)` called.
- `viewModel.addGoal("Write sprint 6 spec", 1)` → `repository.addGoal(...)`.
- `repository.addGoal`: `goals.size` is 0 < 5, title trims to "Write sprint 6 spec" (non-blank), UUID generated, `DailyGoal` created, added to `goals` SnapshotStateList, `persist()` called.
- `showAddDialog = false` → dialog closes.
- `goals` SnapshotStateList mutation triggers recomposition → `GoalCard` renders.
- `totalCount` recomputes to 1, `completedCount` to 0, progress bar shows "0 / 1".

**Result**: PASS

---

### Scenario 3: Goal title validation

**Spec**: Confirm disabled when title is blank/whitespace. Title capped at 60 characters.

**Trace**:
- `isConfirmEnabled = title.trim().isNotBlank()`. Title containing only spaces: `"   ".trim().isNotBlank()` → false. Correct.
- `onValueChange = { if (it.length <= 60) title = it }` — input longer than 60 characters is rejected by not updating state. The check uses `it.length <= 60`, meaning exactly 60 characters is accepted, 61 is not. Correct.

**Edge case**: The 60-character cap in `onValueChange` is on the raw input length. `GoalRepository.addGoal` also applies `.take(60)` as a safety net. The UI cap and repository cap are consistent.

**Result**: PASS

---

### Scenario 4: Enforce maximum of 5 goals

**Spec**: FAB disabled at limit. Re-enables after deletion.

**Trace**:
- `atLimit = derivedStateOf { goals.size >= GoalRepository.MAX_GOALS }` where `MAX_GOALS = 5`.
- With 5 goals: `atLimit` is true.
- In `GoalScreen`, FAB `onClick`: `if (!viewModel.atLimit) showAddDialog = true`. With `atLimit = true`, dialog never opens. Correct.
- FAB renders with `containerColor = MaterialTheme.colorScheme.surfaceVariant` (muted), communicating disabled state.
- Limit banner `Text("Goal limit reached (5/5)")` is shown.
- After `deleteGoal(id)`, `goals.size` drops to 4, `atLimit` recomputes to false, FAB re-enables, banner hides.

**Note**: The FAB is not `enabled = false` — it still receives click events but the `onClick` guard `if (!viewModel.atLimit)` prevents dialog from opening. The visual muting (surfaceVariant container) communicates disabled state to users. This is functionally correct but the FAB is technically still interactive. A minor UX inconsistency, not a bug.

**Result**: PASS (functional); UX note flagged below.

---

### Scenario 5: Mark a goal as complete

**Spec**: Tapping complete button updates goal to completed, shows strikethrough, updates progress bar.

**Trace**:
- `GoalCard` incomplete: `Icons.Default.RadioButtonUnchecked`, no strikethrough.
- `IconButton onClick = { if (!goal.completed) onComplete() }` → `viewModel.markComplete(goal.id)`.
- `repository.markComplete(id)`: finds index, replaces with `copy(completed = true)`, calls `persist()`.
- `goals[index] = ...` mutates SnapshotStateList → recomposition.
- `GoalCard` now renders with `goal.completed = true`: `TextDecoration.LineThrough`, `onSurfaceVariant` color, `surfaceVariant` card background, `CheckCircle` icon.
- `completedCount` increments, `progress` increases, `LinearProgressIndicator` updates.

**Edge case**: Tapping complete on an already-completed goal: `if (!goal.completed) onComplete()` guard prevents double-calling `markComplete`. Correct.

**Result**: PASS

---

### Scenario 6: Delete a goal

**Spec**: Delete button removes goal from list and Preferences. Empty state reappears when list is empty.

**Trace**:
- `IconButton onClick = onDelete` → `viewModel.deleteGoal(goal.id)`.
- `repository.deleteGoal(id)`: `goals.removeAll { it.id == id }`. Returns true if removed, calls `persist()`.
- SnapshotStateList mutation triggers recomposition.
- If `goals.isEmpty()` after removal → `GoalEmptyState` renders. Correct.
- `totalCount` and `completedCount` recompute. If deleted goal was completed, both decrease.

**Edge case**: Deleting a goal with an ID not in the list: `removeAll` returns false, `persist()` is not called. No-op. Correct.

**Result**: PASS

---

### Scenario 7: Persistence across restarts (same day)

**Spec**: Goals survive an app restart on the same calendar date.

**Trace**:
- After each mutation, `repository.persist()` calls `saveAll(goals.toList())` which calls `prefs.put(KEY, serialized)` and `prefs.flush()`. Data is written to Java Preferences backing store.
- On next launch, `GoalRepository` construction calls `loadTodayGoals()`.
- `fromJson()` reconstructs each `DailyGoal`. `isToday()` checks `createdEpochMillis` against `LocalDate.now()`.
- Same calendar day: all goals pass the filter. Completed goals retain `completed = true`. Progress bar restores correctly.

**Result**: PASS

---

### Scenario 8: Stale goals from previous day are discarded

**Spec**: Goals created on a prior day are not shown.

**Trace**:
- `isToday()` extension: `Instant.ofEpochMilli(createdEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()`.
- A goal created yesterday will have a `createdEpochMillis` mapping to yesterday's `LocalDate`. This check returns false.
- `loadTodayGoals()`: `todayGoals = all.filter { it.isToday() }`. Stale goals filtered.
- `if (todayGoals.size != all.size) saveAll(todayGoals)` — stale entries are removed from Preferences immediately.
- SnapshotStateList is seeded with only today's goals.

**Result**: PASS

---

## Code Review Findings

### Correctness

1. **UUID generation**: `UUID.randomUUID().toString()` — correct. UUIDs are collision-safe for the application's scale.

2. **JSON escape correctness**: `DailyGoal.escape()` replaces `\\` before `"` to avoid double-escaping. Order is correct: `replace("\\", "\\\\")` first, then `replace("\"", "\\\"")`. The `extractString` regex `((?:[^"\\]|\\.)*)` handles escaped characters in the value. This is correct for the expected input domain (goal titles without embedded newlines).

3. **Newline delimiter safety**: Goal titles could theoretically contain `\n` characters if copy-pasted. The `OutlinedTextField` with `singleLine = true` prevents newline insertion via the UI. The repository's `raw.lines()` split would corrupt a goal if a title somehow contained a literal newline. Risk is low (UI prevents it) but worth noting.

4. **`coerceIn(1, 10)` in `addGoal`**: Pomodoro count is clamped server-side in the repository even though the UI stepper enforces the range. Defensive and correct.

5. **`persist()` called after `markComplete` and `deleteGoal`**: Both mutations call `persist()` only when a change was made (delete checks the return value of `removeAll`; markComplete always finds the goal since it's called via `GoalCard` which holds a live goal reference). Correct.

6. **`derivedStateOf` in ViewModel**: The `by` delegation on `completedCount`, `totalCount`, `progress`, and `atLimit` creates `State<T>` objects. These are read in composable lambdas — the Compose snapshot system correctly registers reads and triggers recomposition. Correct.

7. **`items(key = { it.id })`**: Using stable keys in `LazyColumn` ensures correct item identity during adds/removes. If keys were not stable, Compose might reuse the wrong item slot. Correct.

### Edge Cases and Potential Issues

1. **FAB not formally disabled**: The FAB does not use `enabled = false`; instead, it silently no-ops in `onClick`. The visual muting (surfaceVariant color) communicates non-interactivity but is not enforced at the accessibility/semantics level. Screen readers may not announce the FAB as disabled. Recommended follow-up: set `enabled = !viewModel.atLimit` on the FAB.

2. **Pomodoro stepper in dialog uses `Text` not `Icon`**: The `−` and `+` characters are Unicode minus/plus, which renders correctly on most systems. Not a bug, but using `Icons.Default.Remove` / `Icons.Default.Add` would be more consistent with Material Design.

3. **`GoalViewModel` instantiation pattern**: The `GoalViewModel` is intended to be created with `remember { GoalViewModel(GoalRepository()) }` in the composable. It is not wired to `viewModelStore` lifecycle. This means the ViewModel is recreated on recomposition of the parent `App` composable if `App` is recreated (e.g., window recreation). In practice, Compose for Desktop apps rarely recreate the root composition, so this is acceptable. The pattern is consistent with how `GoalViewModel` is used in `HistoryScreen` (repository created via `remember`).

4. **No `@Stable` annotation on `GoalViewModel`**: Compose compiler cannot prove `GoalViewModel` is stable, which could lead to unnecessary recompositions when it is passed as a parameter. Adding `@Stable` would be a minor optimization.

5. **`Boolean.toBooleanStrictOrNull()`**: Available since Kotlin 1.5. The project uses Kotlin 1.9+, so this is safe.

6. **Concurrent access to Preferences**: `prefs.flush()` is synchronous. Since all repository operations happen on the Compose main thread, there is no concurrent write risk. Correct.

### Build Readiness

| Check | Status |
|---|---|
| All imports resolvable from existing project dependencies | PASS |
| No new `build.gradle` changes required | PASS |
| `DailyGoal.kt` compiles independently (no circular deps) | PASS |
| `GoalRepository.kt` imports only `DailyGoal` and stdlib | PASS |
| `GoalViewModel.kt` imports only `GoalRepository` and `androidx.lifecycle` | PASS |
| `GoalScreen.kt` imports only `GoalViewModel`, `GoalRepository`, `DailyGoal` | PASS |
| Material Icons used (`Add`, `CheckCircle`, `Delete`, `RadioButtonUnchecked`, `FormatListBulleted`) are in `material-icons-extended` | PASS |
| `LinearProgressIndicator(progress = { ... })` uses the lambda form (Material3 1.2+ API) consistent with project | PASS |
| `Scaffold` inner padding correctly applied via `padding(innerPadding)` | PASS |

---

## Pass/Fail Verdict Per Scenario

| # | Scenario | Verdict |
|---|---|---|
| 1 | Empty state on first launch | PASS |
| 2 | Add a new goal | PASS |
| 3 | Title validation (blank, 60-char cap) | PASS |
| 4 | Maximum 5 goals enforced | PASS (UX note: FAB not formally disabled) |
| 5 | Mark goal as complete | PASS |
| 6 | Delete a goal | PASS |
| 7 | Goals persist across same-day restart | PASS |
| 8 | Stale goals from prior day discarded | PASS |

---

## Overall Sprint Verdict: PASS

All 8 Gherkin scenarios are correctly implemented and verified by code trace. The implementation introduces no new dependencies, does not modify any existing file, and follows the established patterns of the codebase (SnapshotStateList, hand-rolled JSON, Preferences persistence, ViewModel-per-screen).

**Recommended follow-up items (not blocking):**

1. Set `enabled = !viewModel.atLimit` on the FAB for correct accessibility semantics.
2. Add `animateItemPlacement()` to `GoalCard` items for smooth list animations.
3. Investigate adding `@Stable` to `GoalViewModel` for Compose compiler optimization.
4. Consider a toggle (mark incomplete) to allow undoing an accidental complete action.
5. Guard against newline characters in goal titles at the repository level as a belt-and-suspenders safety measure.
