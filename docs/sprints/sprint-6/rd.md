# Sprint 6 RD Document: Daily Focus Goal Planner

## Summary

Sprint 6 implements a new "Goals" tab backed by four new Kotlin files. No existing files are modified. The feature is ready to integrate with a single edit to `App.kt` (documented below).

---

## Architecture Decisions

### 1. No New Dependencies

The implementation reuses only libraries already declared in the project:
- `androidx.lifecycle.ViewModel` — for `GoalViewModel`
- `androidx.compose.runtime` — for `SnapshotStateList`, `derivedStateOf`, `mutableStateOf`
- `java.util.prefs.Preferences` — for persistence (same as `SessionRepository`)
- `java.util.UUID` — for goal ID generation (part of standard JVM)
- Material Icons Extended — already a transitive dependency of `compose-material-icons-extended`

### 2. Repository Owns the SnapshotStateList

`GoalRepository.goals` is a `SnapshotStateList<DailyGoal>`. This pattern is copied directly from `SessionRepository.sessions`. The UI observes the list directly — no `StateFlow`, no `LiveData`, no manual `collectAsState()` required. Compose recomposes the relevant composables automatically whenever the list is mutated.

### 3. derivedStateOf in the ViewModel

`completedCount`, `totalCount`, `progress`, and `atLimit` in `GoalViewModel` are computed via `derivedStateOf { ... }`. This ensures that these aggregates are only recomputed when the underlying `goals` SnapshotStateList actually changes, not on every recomposition frame.

### 4. Day Scoping in the Repository

Day filtering is applied at load time in `GoalRepository.loadTodayGoals()`. Goals whose `createdEpochMillis` maps to a prior calendar date (system default timezone) are silently dropped and the cleaned list is re-persisted. This means stale data from yesterday is cleaned up the first time the app opens after midnight — no background job, no scheduled task.

### 5. Minimal JSON Serialization

The same hand-rolled regex-based JSON encoding used by `SessionRecord` is replicated in `DailyGoal.Companion`. This keeps the serialization approach consistent across the codebase and avoids introducing a JSON library dependency. The `escape()` helper ensures titles containing `"` or `\` are safely encoded.

### 6. GoalViewModel Does Not Extend ViewModel Lifecycle

`GoalViewModel` extends `androidx.lifecycle.ViewModel` for consistency with `FocusTimerViewModel`, but it does not use `viewModelScope` (no coroutine work is needed — all operations are synchronous Preferences writes). The ViewModel is instantiated with `remember { GoalViewModel(GoalRepository()) }` in the composable, matching how the rest of the project manages ViewModel lifetime.

---

## File Structure

```
composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/
├── DailyGoal.kt          NEW — data class + JSON serialization helpers
├── GoalRepository.kt     NEW — Preferences persistence + SnapshotStateList
├── GoalViewModel.kt      NEW — business logic, derived state
└── GoalScreen.kt         NEW — Compose UI (GoalScreen, GoalCard, AddGoalDialog, EmptyState)
```

All four files are new. No existing file is touched.

---

## File Descriptions

### DailyGoal.kt

Data class with five fields: `id (String)`, `title (String)`, `estimatedPomodoros (Int)`, `completed (Boolean)`, `createdEpochMillis (Long)`. The companion object contains:
- `DailyGoal.isToday()` extension function for day-scoping checks
- `toJson(goal)` and `fromJson(json)` for Preferences serialization
- Private `escape()`, `extractString()`, `extractInt()`, `extractLong()`, `extractBoolean()` helpers

### GoalRepository.kt

- Preferences node: `org/coding/afternoon/focus/goals`, key `"daily_goals"`
- Public `goals: SnapshotStateList<DailyGoal>` — initialized from today's persisted goals on construction
- `loadTodayGoals()` — filters and optionally re-persists (stale cleanup)
- `addGoal(title, estimatedPomodoros): DailyGoal?` — enforces max 5, trims title, generates UUID, returns null if limit hit
- `markComplete(id)` — replaces the goal at its index via `copy(completed = true)`
- `deleteGoal(id)` — removes from list, persists

### GoalViewModel.kt

Thin ViewModel wrapping `GoalRepository`. Exposes:
- `goals` — direct reference to repository's SnapshotStateList
- `completedCount`, `totalCount`, `progress`, `atLimit` — `derivedStateOf` computed properties
- `addGoal()`, `markComplete()`, `deleteGoal()` — delegate to repository

### GoalScreen.kt

Compose screen using `Scaffold` with a `FloatingActionButton`. Internal composables:
- `DailyProgressSection` — header row with title + fraction text + `LinearProgressIndicator`
- `GoalCard` — `Card` with title (strikethrough when complete), pomodoro count, complete `IconButton`, delete `IconButton`
- `GoalEmptyState` — centered column with muted icon + text
- `AddGoalDialog` — `AlertDialog` with `OutlinedTextField`, character counter, pomodoro stepper (`−` / `+` `IconButton`s), Cancel / Add Goal buttons

---

## Integration Instructions

**To activate the Goals tab, make the following changes to `App.kt`:**

```kotlin
// 1. Add GoalRepository and GoalViewModel instantiation inside App():
val goalRepository = remember { GoalRepository() }
val goalViewModel = remember { GoalViewModel(goalRepository) }

// 2. Add "Goals" to the tabs list:
val tabs = listOf("Timer", "History", "Goals")

// 3. Add the Goals case to the when block:
2 -> GoalScreen(goalViewModel)
```

These are the only changes required. The new files compile independently — the integration edit is intentionally left out of scope to avoid merge conflicts with parallel teams.

---

## Trade-offs and Notes

### What was kept simple

- **No goal editing**: After creation, a goal's title and pomodoro count cannot be changed. Editing would require a separate dialog and was descoped per the stakeholder document.
- **No undo on delete**: Deletion is immediate and permanent. An undo snackbar would be a good follow-up.
- **No link to timer sessions**: Goals and Pomodoro sessions are independent. A future sprint could add a "which goal are you working on?" prompt when starting the timer.

### Known limitations

- `derivedStateOf` properties in `GoalViewModel` are read using `by` delegation. This is valid in a `ViewModel` class when the delegated `State<T>` object is from Compose runtime, but callers in composables must ensure they read these properties within a Compose scope for proper recomposition tracking. Since `GoalScreen` reads them inside `@Composable` functions, this is correct.
- The Pomodoro stepper in `AddGoalDialog` uses plain `Text` buttons (`−` / `+`) rather than `FilledIconButton` to avoid potential icon naming inconsistencies across Material Icons versions.
- Goals persist across the same calendar day in the system's default timezone. If a user works past midnight, goals created before midnight will be classified as "yesterday's" by the next morning. This is the intended behavior.

### Potential future improvements

- Swipe-to-delete gesture on `GoalCard`
- Animate item removal with `animateItemPlacement()`
- Show a congratulatory message when all goals are completed
- Allow marking a completed goal as incomplete (toggle behavior)
- Export daily goal completion to the History screen
