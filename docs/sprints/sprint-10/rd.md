# RD Implementation Notes: Multi-Timer Workspace

## Sprint 10 | RD Role

---

## Files Created

All new files are in `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/`.

| File                   | Purpose                                               |
|------------------------|-------------------------------------------------------|
| `WorkspaceTimer.kt`    | Immutable data model + lifecycle state enum           |
| `WorkspaceViewModel.kt`| Business logic, per-timer coroutine management, beep  |
| `WorkspaceScreen.kt`   | Compose UI: grid, card, dialog, arc                   |

**No existing files were modified.** `App.kt`, `FocusTimerViewModel.kt`, `FocusScreen.kt`, `CompletionSound.kt`, and all other pre-existing files are untouched.

---

## Integration Instructions

To expose the Workspace tab in the running app, make the following minimal change to `App.kt`:

```kotlin
// 1. Add WorkspaceViewModel import (same package, no import needed)

// 2. Instantiate the ViewModel alongside FocusTimerViewModel:
//    val workspaceViewModel = remember { WorkspaceViewModel() }
//    (or inject via a factory in main.kt)

// 3. Add "Workspace" to the tabs list:
val tabs = listOf("Timer", "History", "Workspace")

// 4. Add the new case to the when block:
2 -> WorkspaceScreen(workspaceViewModel)
```

The feature is fully functional in isolation; the `WorkspaceViewModel` requires no parameters and manages its own lifecycle via `viewModelScope`.

---

## Architecture Decisions

### Independent Coroutine Per Timer

Each timer owns exactly one `Job` stored in `WorkspaceViewModel.timerJobs: Map<String, Job>`.

```
timerJobs = { "uuid-1": Job(running), "uuid-3": Job(paused/cancelled), ... }
```

- **Start**: cancels any stale job for that id, then launches a new `viewModelScope.launch(Dispatchers.Main)` coroutine.
- **Pause**: cancels the job. `remainingSeconds` is preserved in the SnapshotStateList entry.
- **Resume**: re-launches a new job; it reads `remainingSeconds` from the current timer snapshot to resume from the correct point.
- **Remove**: cancels the job and removes the entry from both `timerJobs` and `timers`.

This design means pausing timer A and starting timer B are completely orthogonal — they touch different keys in `timerJobs` and different indices in `timers`.

### Immutable Copy Pattern

`WorkspaceTimer` is a Kotlin `data class` with `val` fields. Every state mutation in `WorkspaceViewModel` replaces the list entry:

```kotlin
timers[index] = timers[index].copy(state = WorkspaceTimerState.Running)
```

Compose's `SnapshotStateList` detects the structural change and triggers recomposition only for the card backed by that entry. There are no `MutableState` fields inside `WorkspaceTimer` itself, so the data model remains pure and serializable.

### SnapshotStateList for Reactivity

`timers` is declared as:

```kotlin
val timers = mutableStateListOf<WorkspaceTimer>()
```

`mutableStateListOf` (from `androidx.compose.runtime`) creates a `SnapshotStateList` that integrates with Compose's snapshot system. Reads inside `@Composable` functions are automatically tracked; writes (add, removeAt, element replacement) invalidate exactly the relevant composition scopes.

The `LazyVerticalGrid` uses `items(timers, key = { it.id })` so Compose can correctly animate additions, removals, and in-place updates without re-creating every card.

### Completion Beep Implementation

The workspace beep (`WorkspaceViewModel.playCompletionBeep`) is deliberately distinct from `CompletionSound`:

| Property          | Pomodoro chime (`CompletionSound`)    | Workspace beep (`WorkspaceViewModel`)   |
|-------------------|---------------------------------------|-----------------------------------------|
| Notes             | C5 → E5 → G5 (ascending, 3 notes)    | A4 → E4 (descending, 2 notes)          |
| Note duration     | 220 ms each                           | 150 ms each                             |
| Gap between notes | 60 ms                                 | 40 ms                                   |
| Amplitude         | 0.6                                   | 0.5                                     |
| Envelope          | Hann window (sin²)                    | Hann window (sin²)                      |
| Thread            | Daemon thread "focus-chime"           | Daemon thread "workspace-beep"          |

The beep is shorter, lower-pitched, and quieter so it is distinguishable from a Pomodoro completion even if both fire close in time.

### Max Timer Enforcement

`addTimer` checks `timers.size >= MAX_TIMERS` before inserting. The UI disables the "Add Timer" `Button` when `timers.size >= MAX_TIMERS`. Both layers enforce the limit independently.

---

## Memory Leak Prevention

1. **Coroutine scope**: All jobs are launched with `viewModelScope`. When `WorkspaceViewModel.onCleared()` is called (e.g., when the composable host is destroyed), `viewModelScope` is automatically cancelled, which cancels all child jobs.

2. **Explicit cleanup in `onCleared()`**: As a belt-and-suspenders measure, `onCleared()` iterates `timerJobs.values` and calls `cancel()` on each, then clears the map.

3. **Remove cleans up immediately**: `removeTimer(id)` cancels the job before removing the list entry so there is zero window during which a coroutine can decrement a timer that no longer exists in the list.

4. **Index-safety**: The countdown coroutine re-looks up `timers.indexOfFirst { it.id == id }` on every tick. If it returns `-1` (timer was removed between ticks), the coroutine `break`s without accessing any state, preventing index-out-of-bounds and zombie ticks.

---

## Edge Cases Handled

| Scenario                                      | Handling                                                              |
|-----------------------------------------------|-----------------------------------------------------------------------|
| `startTimer` called on a Running timer        | Early return — no duplicate job                                       |
| `startTimer` called on a Completed timer      | Early return — prevents restarting a finished timer                   |
| `resetTimer` called on an Idle timer          | Early return — no unnecessary copy                                    |
| `addTimer` with minutes <= 0                  | Silently returns; UI also disables "Add" when duration is 0           |
| `addTimer` when at MAX_TIMERS                 | Silently returns; UI disables the button                              |
| Audio device unavailable                      | `try/catch` around entire `playCompletionBeep` body; swallowed silently|
| App closed while timers running               | `viewModelScope` cancellation + `onCleared()` cancel all jobs         |
| Blank label submitted                         | Defaults to "Timer N" where N is current list size + 1                |

---

## Dependencies

No new dependencies are required. The implementation uses:

- `androidx.compose.runtime.mutableStateListOf` — already on the classpath via Compose for Desktop.
- `androidx.lifecycle.ViewModel`, `viewModelScope` — already used by `FocusTimerViewModel`.
- `javax.sound.sampled` — JVM standard library, already used by `CompletionSound`.
- `java.util.UUID` — JVM standard library.
- Standard Compose Material3 components (`Card`, `Button`, `AlertDialog`, `LazyVerticalGrid`, `Canvas`).
