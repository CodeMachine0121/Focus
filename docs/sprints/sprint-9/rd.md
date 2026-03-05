# Sprint 9 — RD (Engineering) Document
## Feature: Mindful Break Activity Coach

**Sprint:** 9
**Author:** RD / Engineering
**Date:** 2026-03-05

---

## 1. Summary

Five new Kotlin source files implement the Mindful Break Activity Coach feature. Zero existing files were modified. The feature is wired into the app via two manual steps documented in Section 7 (Integration Instructions).

---

## 2. File Inventory

| File | Location | Role |
|------|----------|------|
| `BreakActivity.kt` | `…/focus/` | Data model: `BreakCategory` enum, `BreakActivity` data class |
| `BreakActivityLibrary.kt` | `…/focus/` | Singleton object holding 24 hardcoded activities |
| `BreakCoachRepository.kt` | `…/focus/` | Java Preferences persistence for today's completions |
| `BreakCoachViewModel.kt` | `…/focus/` | ViewModel: filter state, shuffle, markDone, phase observation |
| `BreakCoachScreen.kt` | `…/focus/` | Compose UI: progress header, filter chips, activity cards, FAB |

---

## 3. Data Model Design

### `BreakCategory` (enum)

Five values with a `displayName` property used directly by the UI filter chips:

```
Movement, EyeRest, Mindfulness, Hydration, Creative
```

### `BreakActivity` (data class)

```kotlin
data class BreakActivity(
    val id: String,           // stable, snake_case unique key
    val title: String,        // short human-readable name
    val description: String,  // 1-2 sentence instruction
    val category: BreakCategory,
    val durationSeconds: Int, // estimated completion time
    val emoji: String,        // single emoji for the card icon
)
```

IDs are stable strings (e.g., `"neck_roll"`, `"box_breathing"`) rather than integers so that persisted preference data remains valid even if the library order changes in a future sprint.

---

## 4. Activity Library Design (`BreakActivityLibrary.kt`)

24 activities are distributed across the five categories:

| Category | Count | Example Activities |
|----------|-------|--------------------|
| Movement | 6 | Neck Roll, Shoulder Shrug, Desk Push-Up, Wrist Stretch, Calf Raise, Seated Spine Twist |
| EyeRest | 5 | 20-20-20 Rule, Eye Palming, Blink Reset, Figure-Eight Eyes, Near-Far Focus Shift |
| Mindfulness | 5 | Box Breathing, 1-Minute Body Scan, Gratitude Pause, Mindful Breath Count, 5-4-3-2-1 Grounding |
| Hydration | 4 | Drink Water, Healthy Snack, Tea Break, Refill Your Bottle |
| Creative | 4 | Journal Prompt, Quick Sketch, Window Gaze, Desk Tidy |

Activities are hardcoded in `BreakActivityLibrary.allActivities` — no network calls, no file I/O. This keeps the library deterministic and offline-safe.

---

## 5. Persistence Approach (`BreakCoachRepository.kt`)

**Storage:** `java.util.prefs.Preferences` (same mechanism used by `SessionRepository`).

**Preferences node:** `org/coding/afternoon/focus/break_coach`

**Key format:** `completed_<YYYY-MM-DD>` (e.g., `completed_2026-03-05`)

**Value format:** Comma-separated activity IDs (e.g., `"neck_roll,box_breathing,drink_water"`)

**Day rollover:** `clearOldDays()` is called in `BreakCoachViewModel.init`. It iterates all preference keys in the node and removes any `completed_*` key that does not match today's date. This keeps the preferences store lean and ensures completed IDs from previous days do not carry over.

**`markDone(activityId)` implementation:**
1. Read current set from preferences.
2. If ID not present, add it and write back (comma-joined).
3. `prefs.flush()` is called to force write to the OS backing store.
4. The call is idempotent — adding a duplicate ID has no effect.

---

## 6. ViewModel Design (`BreakCoachViewModel.kt`)

Extends `androidx.lifecycle.ViewModel`. Key state:

| Property | Type | Description |
|----------|------|-------------|
| `selectedCategory` | `BreakCategory?` | Active filter; null = "All". |
| `filteredActivities` | `List<BreakActivity>` | Derived from `allActivities` + `selectedCategory`. |
| `todayCompletedIds` | `SnapshotStateList<String>` | Live list that drives card completed-state recomposition. |
| `completedCount` | `Int` | `todayCompletedIds.size`. |
| `totalCount` | `Int` | `allActivities.size` (always 24). |
| `shuffleHighlightIndex` | `Int` | Index in `filteredActivities` to highlight; -1 = none. |
| `pendingBreakSuggestion` | `BreakActivity?` | Non-null when the UI should show a break-phase snackbar. |

### Phase Observation

`FocusTimerViewModel` is passed as an optional constructor parameter. The ViewModel reads `currentPhase` in a polling loop (`delay(500ms)`) inside a `viewModelScope` coroutine. When a `Focus → Break` transition is detected, `pendingBreakSuggestion` is set to a randomly chosen uncompleted activity. The UI observes this with `LaunchedEffect` and shows the snackbar.

**Why polling instead of a Flow?** Modifying `FocusTimerViewModel` to expose a Flow is out of scope (the constraint forbids editing existing files). Polling at 500ms is imperceptible to the user and imposes negligible CPU cost.

### Shuffle Algorithm

1. Filter `filteredActivities` to items whose `id` is not in `todayCompletedIds`.
2. If uncompleted items exist, pick one at random (`kotlin.random.Random`).
3. If all are completed, fall back to picking from the full `filteredActivities` list.
4. Set `shuffleHighlightIndex` to the chosen item's index in `filteredActivities`.
5. The UI's `LaunchedEffect(shuffleHighlightIndex)` calls `listState.animateScrollToItem(index)`.

---

## 7. Integration Instructions

**These two steps must be performed on `App.kt` when this feature is integrated into the main app build.**

### Step 1 — Add Break Coach tab to `App.kt`

```kotlin
// In App.kt, change:
val tabs = listOf("Timer", "History")

// To:
val tabs = listOf("Timer", "History", "Break Coach")

// And add to the when(selectedTab) block:
2 -> BreakCoachScreen(breakCoachViewModel)
```

### Step 2 — Instantiate BreakCoachViewModel in `App.kt`

```kotlin
// Add inside App() composable, alongside the existing SessionRepository:
val breakCoachViewModel = remember {
    BreakCoachViewModel(focusTimerViewModel = viewModel)
}
```

The `viewModel` parameter here is the `FocusTimerViewModel` already passed into `App`. The `BreakCoachViewModel` uses it read-only for phase observation.

---

## 8. Compose UI Architecture (`BreakCoachScreen.kt`)

The screen uses a `Scaffold` with:
- `snackbarHost` for the break-phase suggestion snackbar.
- `floatingActionButton` for the Shuffle FAB (`Icons.Default.Shuffle`).

Inside the scaffold content:
1. **`DailyProgressHeader`** — `Surface` with `LinearProgressIndicator` and completion text. Pinned above the scroll area.
2. **`CategoryFilterRow`** — `LazyRow` of Material3 `FilterChip` components.
3. **`LazyColumn`** — activity cards indexed by position for efficient scroll-to targeting.

Each `ActivityCard` uses:
- `ElevatedCard` with conditional `containerColor` for the completed state.
- `animateColorAsState` for the shuffle highlight border (600ms tween, fades from `primary` to `Transparent`).
- `FilledTonalButton("Done")` when uncompleted; `Icon(CheckCircle) + "Completed" text` when done.

---

## 9. Dependencies

No new dependencies are required. All APIs used are already present in the project:

- `androidx.lifecycle:lifecycle-viewmodel-*` — already used by `FocusTimerViewModel`.
- `androidx.compose.material3:material3` — already used throughout.
- `androidx.compose.material:material-icons-extended` — provides `Icons.Default.Shuffle`, `Icons.Default.CheckCircle`. Verify this artifact is already declared in `build.gradle.kts`; if not, add it.
- `java.util.prefs.Preferences` — JDK standard library, no declaration needed.
- `java.time.LocalDate` — JDK standard library (Java 8+), available on the JVM target.

---

## 10. Constraints Checklist

| Constraint | Status |
|------------|--------|
| No existing files modified | PASS — zero edits to App.kt, FocusTimerViewModel.kt, FocusScreen.kt, or any other existing file |
| Break Coach does not call timer methods | PASS — no calls to start(), pause(), reset(), dismiss() anywhere in new files |
| Timer phase observation is read-only | PASS — only reads `currentPhase`, no writes to FocusTimerViewModel state |
| 20+ activities in library | PASS — 24 activities implemented |
| Persistence uses Java Preferences | PASS — BreakCoachRepository uses `java.util.prefs.Preferences` |
| ViewModel extends androidx ViewModel | PASS — `class BreakCoachViewModel : ViewModel()` |
| No `./gradlew` executed | PASS |
