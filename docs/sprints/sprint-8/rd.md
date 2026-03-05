# Sprint 8 — RD Implementation Notes
## Feature: Productivity Dashboard & Streak Tracker

---

## Files Created

All files live in:
`composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/`

| File | Lines | Role |
|---|---|---|
| `DashboardStats.kt` | ~50 | Data classes only |
| `StreakCalculator.kt` | ~115 | Pure computation logic |
| `DashboardViewModel.kt` | ~55 | Coroutine-dispatched ViewModel |
| `DashboardScreen.kt` | ~310 | Full Compose UI |

No existing files were modified.

---

## Integration Instructions

To wire the Dashboard into the app, make the following changes to `App.kt`:

```kotlin
// 1. Add "Dashboard" to the tab list
val tabs = listOf("Timer", "History", "Dashboard")

// 2. Instantiate the ViewModel alongside repository
val dashboardViewModel = remember { DashboardViewModel(repository) }

// 3. Add the tab case
2 -> DashboardScreen(dashboardViewModel)

// 4. (Optional) Refresh dashboard after a session is dismissed
viewModel.onSessionDismissed = { durationMinutes ->
    repository.record(durationMinutes)
    dashboardViewModel.refresh()   // triggers re-computation
}
```

---

## Architecture Decisions

### 1. Separation of concerns: pure functions vs ViewModel vs Composable

`StreakCalculator` contains zero Compose or Android dependencies — it is a plain Kotlin `object` with pure functions. This makes it trivially unit-testable in a standard JVM test. `DashboardViewModel` is the narrow bridge: it owns the threading concern (background computation) and the reactive state (`mutableStateOf`). `DashboardScreen` is purely presentational — it reads state and renders it.

### 2. Data class design (DashboardStats.kt)

All data classes are immutable. `DashboardStats` is the single value passed from ViewModel to Screen, preventing the Screen from depending directly on `StreakCalculator` or `SessionRepository`. This keeps the UI composable pure and testable with mock data.

### 3. ViewModel threading

`DashboardViewModel.refresh()` uses `viewModelScope.launch` and dispatches the list snapshot + computation to `Dispatchers.Default`. The UI thread only sees the final `DashboardStats` value assigned back on `Main`. The `isLoading` flag prevents the UI from rendering a half-computed state.

The `repository.sessions.toList()` snapshot is taken on `Dispatchers.Default` — this is safe because `SnapshotStateList` is thread-safe for reads; the snapshot captures a consistent moment without locking the UI.

### 4. Streak algorithm

```
currentStreak:
  1. Build a Set<LocalDate> of all days with sessions.
  2. If today is in the set, start counting from today backwards.
     If today is NOT in the set but yesterday IS, start from yesterday
     (the streak is "at risk" for today but not yet broken).
  3. Walk backwards day by day, incrementing the counter until a day
     with no session is found.

longestStreak:
  1. Build a sorted list of unique active days.
  2. Walk the list comparing consecutive days.
     If gap == 1 day: increment current run.
     If gap > 1 day: reset current run to 1.
  3. Track the maximum run seen.
```

Both algorithms are O(n) where n = number of unique session days.

### 5. Weekly bar chart (Canvas)

The chart is drawn entirely with `Compose Canvas` using `drawRoundRect`. The canvas width is `fillMaxWidth`, so bar widths adapt to the window. The relative bar height is computed as `(minutes / maxMinutes) * maxBarHeight`. A minimum height of 2px ensures zero-minute days show a visual base line rather than disappearing.

Value labels are rendered above each bar using `TextMeasurer` + `drawText` (the Compose 1.4+ API). Labels are omitted if they would clip above the canvas top edge.

### 6. 30-day heatmap (Canvas)

The heatmap uses `BoxWithConstraints` to measure the available width as a `Dp` value at composition time, then derives `cellSizeDp` from it. This ensures the grid always fills the card width regardless of window size. The canvas height is derived from `cellSizeDp * rows + gap * (rows - 1)`, keeping the cells square.

Color intensity uses linear alpha stepping from 0.25 to 1.0 over session counts 1–4. Alpha blending over the card's `surfaceContainerLow` background produces a natural green scale visually similar to GitHub contribution graphs.

Today's cell is identified by comparing each `HeatmapCell.date` against `LocalDate.now()` (captured once via `remember` to avoid repeated calls per frame). A `Stroke`-style `drawRoundRect` draws the outline border.

### 7. Empty state

Shown when `stats.totalSessions == 0`. This is checked after the ViewModel has loaded (not during loading), so the empty state is never shown incorrectly while data is being fetched.

### 8. No new dependencies

The implementation uses only APIs already present in the project:
- `androidx.lifecycle.ViewModel` / `viewModelScope`
- Compose for Desktop (Canvas, Material3, Layout)
- Kotlin stdlib + `java.time` (already used in `SessionRecord.kt`)

---

## Known Limitations / Future Work

- `DashboardViewModel` does not observe `SessionRepository.sessions` reactively with a `Flow` or `snapshotFlow`. The caller must call `refresh()` after new sessions are recorded. A future sprint could convert `SessionRepository` to expose a `StateFlow<List<SessionRecord>>` and have `DashboardViewModel` collect it automatically.
- The heatmap does not show row date labels inside the canvas (they would overlap cell content). A future improvement could add a narrow label column to the left of the grid using a separate composable column.
- `formatNumber` handles thousands only (e.g., "1,050"). For users with 1,000,000+ minutes, the function would need extending — acceptable given current scope.
- Weekly chart uses the current ISO week (Mon–Sun). No navigation to past weeks is implemented in this sprint.
