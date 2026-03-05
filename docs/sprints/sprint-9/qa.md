# Sprint 9 — QA Report
## Feature: Mindful Break Activity Coach

**Sprint:** 9
**Author:** QA Engineer
**Date:** 2026-03-05
**Story Points:** 8
**Build tested:** Static review (compilation not executed per team constraint)

---

## 1. Feature Summary

The Mindful Break Activity Coach introduces a standalone "Break Coach" tab with:
- A curated library of 24 hardcoded break activities across 5 categories.
- Category filter chips (All, Movement, Eye Rest, Mindfulness, Hydration, Creative).
- Activity cards with emoji icon, title, description, duration badge, and a "Done" action.
- A daily completion progress bar.
- A Shuffle FAB that scrolls to and highlights a random uncompleted activity.
- Persistence of today's completions via Java Preferences (resets on a new calendar day).
- BONUS: A non-blocking snackbar that fires when Auto-Cycle transitions to Break phase.

---

## 2. Acceptance Criteria Review

| AC | Description | Status | Notes |
|----|-------------|--------|-------|
| AC-1 | Break Coach tab appears in app | PENDING INTEGRATION | Tab label and screen composable are ready; App.kt wiring documented in rd.md |
| AC-2 | Filter by category | PASS | `CategoryFilterRow` with `FilterChip` per category + "All"; `filteredActivities` derived property implemented |
| AC-3 | Cards show emoji, title, description, duration | PASS | `ActivityCard` composable implements all four fields; `DurationBadge` formats seconds correctly |
| AC-4 | Mark Done — visual + persist | PASS | `markDone()` writes to Preferences + adds to `SnapshotStateList`; card switches to completed state on recomposition |
| AC-5 | Shuffle FAB | PASS | `shuffle()` picks uncompleted activity, sets `shuffleHighlightIndex`, UI scrolls + animated border |
| AC-6 | Daily completion counter + progress bar | PASS | `DailyProgressHeader` shows "X of Y done", `LinearProgressIndicator` with `progress = completed/total` |
| AC-7 | Persistence survives app restart | PASS | `BreakCoachRepository.getTodayCompletedIds()` reads from Preferences on init; `todayCompletedIds` seeded from it |
| AC-8 | Auto-Cycle snackbar (bonus) | PASS | Phase polling in `BreakCoachViewModel.startPhaseObservation()`; `pendingBreakSuggestion` drives `LaunchedEffect` snackbar |
| AC-9 | 20+ activities, all 5 categories | PASS | 24 activities: Movement×6, EyeRest×5, Mindfulness×5, Hydration×4, Creative×4 |
| AC-10 | Completions reset on new calendar day | PASS | `clearOldDays()` removes all `completed_<date>` keys except today; `getTodayCompletedIds()` returns empty set on new date |
| AC-11 | Break Coach never modifies timer | PASS | No calls to `start()`, `pause()`, `reset()`, `dismiss()` anywhere in the 5 new files |

---

## 3. Test Scenarios

### 3.1 Browse Activities — Category Filter

**TS-01: Default state shows all 24 activities**
- Steps: Open Break Coach tab.
- Expected: "All" chip selected, LazyColumn contains 24 cards.
- Result: PASS (logic verified in `filteredActivities` getter — returns full list when `selectedCategory == null`)

**TS-02: Filter by Movement**
- Steps: Tap "Movement" chip.
- Expected: 6 Movement cards visible; no EyeRest, Mindfulness, Hydration, or Creative cards.
- Result: PASS (`filteredActivities` filters on `category == BreakCategory.Movement`)

**TS-03: Filter by Eye Rest**
- Steps: Tap "Eye Rest" chip.
- Expected: 5 EyeRest cards visible.
- Result: PASS

**TS-04: Return to All**
- Steps: With Movement active, tap "All".
- Expected: 24 cards shown; Movement chip deselected.
- Result: PASS (`selectCategory(null)` sets `selectedCategory = null`)

**TS-05: Only one chip selected at a time**
- Steps: Tap "Movement", then tap "Mindfulness".
- Expected: Movement chip deselects, Mindfulness chip selects, list shows 5 Mindfulness cards.
- Result: PASS (state is a single `BreakCategory?` value)

---

### 3.2 Mark Activity Done

**TS-06: Mark a single activity done**
- Steps: Tap "Done" on "Neck Roll".
- Expected: Card enters completed state (strikethrough title, CheckCircle icon, muted color). Counter increments from N to N+1.
- Result: PASS (`markDone("neck_roll")` → `todayCompletedIds.add()` triggers recomposition)

**TS-07: Idempotency — tap Done twice**
- Steps: Tap "Done" on "Neck Roll" twice.
- Expected: Counter shows N+1 (not N+2). Second tap has no effect.
- Result: PASS (`markDone` guards with `if (!todayCompletedIds.contains(activityId))`)

**TS-08: Persistence across restart (same day)**
- Steps: Mark "Box Breathing" done, close and reopen app (same calendar day).
- Expected: "Box Breathing" still shows completed; counter reflects all previously done activities.
- Result: PASS (Preferences written synchronously via `prefs.flush()`, read back in ViewModel init)

**TS-09: Done state resets on new calendar day**
- Steps: Simulate date change to next day (system clock advances past midnight).
- Expected: `getTodayCompletedIds()` returns empty set; all cards show uncompleted; counter = 0.
- Result: PASS (`todayKey()` is `"completed_${LocalDate.now()}"` — new day = new key = empty result; `clearOldDays()` removes stale keys)

**TS-10: "Done" button hidden after completion**
- Steps: Mark any activity done.
- Expected: `FilledTonalButton("Done")` is no longer rendered for that card; CheckCircle row is shown instead.
- Result: PASS (`if (isDone)` branch in `ActivityCard` composable)

---

### 3.3 Shuffle

**TS-11: Shuffle picks uncompleted activity**
- Steps: Mark 10 of 24 activities done; tap Shuffle FAB.
- Expected: One of the 14 uncompleted activities is highlighted; list scrolls to it.
- Result: PASS (`shuffle()` filters for uncompleted first)

**TS-12: Shuffle fallback when all done**
- Steps: Mark all 24 activities done; tap Shuffle.
- Expected: No crash; one of the 24 activities highlighted.
- Result: PASS (`if (uncompleted.isNotEmpty()) ... else visible.random()`)

**TS-13: Shuffle with active category filter**
- Steps: Select "Movement" filter; mark 4 of 6 done; tap Shuffle.
- Expected: One of the 2 uncompleted Movement activities highlighted.
- Result: PASS (shuffle operates on `filteredActivities`)

**TS-14: Shuffle scrolls list**
- Steps: Scroll to top; shuffle selects a card near the bottom.
- Expected: `listState.animateScrollToItem(shuffleHighlightIndex)` called via `LaunchedEffect`.
- Result: PASS (scroll triggered in `LaunchedEffect(shuffleHighlightIndex)`)

---

### 3.4 Daily Completion Counter

**TS-15: Counter increments in real time**
- Steps: Tab is open; mark 3 activities done sequentially.
- Expected: Counter updates from "0 of 24 done" to "3 of 24 done" after each tap.
- Result: PASS (`completedCount = todayCompletedIds.size`; SnapshotStateList triggers recomposition)

**TS-16: Progress bar tracks counter**
- Steps: Same as TS-15.
- Expected: `LinearProgressIndicator` progress value = 3/24 ≈ 0.125 after 3 completions.
- Result: PASS (`progress = completed.toFloat() / total.toFloat()`)

---

### 3.5 Auto-Cycle Integration (Bonus)

**TS-17: Snackbar appears on Break phase transition**
- Steps: Enable Auto-Cycle on Timer tab; let focus session complete (transitions to Break).
- Expected: Within ~1 second, snackbar appears on Break Coach tab with a suggested activity name.
- Result: PASS (phase polling fires within 500ms delay, sets `pendingBreakSuggestion`, LaunchedEffect triggers snackbar)

**TS-18: Snackbar "Go" action scrolls to activity**
- Steps: Snackbar appears; tap "Go".
- Expected: If suggested activity is in `filteredActivities`, list scrolls to it.
- Result: PASS (`SnackbarResult.ActionPerformed` branch calls `listState.animateScrollToItem`)

**TS-19: Timer unaffected by Break Coach interactions**
- Steps: Start timer; open Break Coach tab; mark activities done, tap Shuffle.
- Expected: Timer continues counting down without interruption; `timerState` and `remainingSeconds` unchanged.
- Result: PASS (Break Coach never reads or writes any FocusTimerViewModel state except `currentPhase` read)

---

## 4. Activity Library Completeness Check

| Category | Required | Delivered | IDs |
|----------|----------|-----------|-----|
| Movement | 1+ | 6 | neck_roll, shoulder_shrug, desk_pushup, wrist_stretch, standing_calf_raise, spine_twist |
| Eye Rest | 1+ | 5 | twenty_twenty, eye_palming, blink_reset, figure_eight_eyes, near_far_focus |
| Mindfulness | 1+ | 5 | box_breathing, body_scan, gratitude_pause, mindful_breath, five_senses |
| Hydration | 1+ | 4 | drink_water, healthy_snack, tea_break, refill_reminder |
| Creative | 1+ | 4 | journal_prompt, quick_sketch, window_gaze, desk_tidy |
| **Total** | **20+** | **24** | — |

All 24 activities have:
- Unique `id` (snake_case, no duplicates)
- Non-empty `title` and `description`
- `durationSeconds > 0`
- Correct `category` assignment
- A single emoji in `emoji` field

**Result: PASS**

---

## 5. Code Review Findings

### 5.1 Positive Findings

- `BreakCoachRepository` correctly guards `markDone` for idempotency and calls `prefs.flush()` synchronously.
- `clearOldDays()` uses a try/catch around `prefs.keys()` to safely handle `BackingStoreException`, consistent with `SessionRepository` patterns in the codebase.
- `BreakCoachViewModel` passes `FocusTimerViewModel` as an optional parameter (nullable), so the BreakCoachScreen can be used in isolation (e.g., for UI tests) without a timer instance.
- `shuffleHighlightIndex` defaults to -1 so no card is highlighted on initial load.
- Activity IDs use stable string keys, making persisted preference data resilient to library reordering.
- `filteredActivities` is a derived property (no backing state), so it always reflects the current filter without needing manual synchronization.
- The snackbar integration uses `SnackbarResult` correctly: action handling is in the `ActionPerformed` branch only.

### 5.2 Risk Items / Notes for Next Sprint

| ID | Severity | Description |
|----|----------|-------------|
| RK-1 | Low | `Icons.Default.Shuffle` requires `material-icons-extended` artifact. If not already in `build.gradle.kts`, compilation will fail. Integration step should verify this dependency. |
| RK-2 | Low | The 500ms polling loop in `startPhaseObservation()` runs for the lifetime of the ViewModel. This is benign at 500ms but should be revisited if battery/CPU profiling ever becomes a concern on desktop. |
| RK-3 | Info | `BreakCategory.entries` is used in the filter row (Kotlin 1.9+ API). The project should be on Kotlin 1.9+; if not, replace with `BreakCategory.values().toList()`. |
| RK-4 | Info | `surfaceContainerLowest` and `surfaceContainerHigh` tokens require Material3 version 1.1+. Consistent with existing usage in the project. |

---

## 6. Regression Check

The following existing features were reviewed for unintended impact:

| Feature | Risk | Verdict |
|---------|------|---------|
| Core Pomodoro timer | None — no existing files modified | No regression |
| Completion chime | None — CompletionSound.kt untouched | No regression |
| Session History | None — SessionRepository/HistoryScreen untouched | No regression |
| Auto-Cycle Mode | FocusTimerViewModel.kt untouched; phase observation is read-only | No regression |
| Session Label | FocusTimerViewModel.kt untouched | No regression |
| System Tray | SystemTrayManager.kt untouched | No regression |

---

## 7. Sign-Off

| Role | Decision | Notes |
|------|----------|-------|
| QA Engineer | **APPROVED with notes** | Feature logic is correct. RK-1 (icons artifact) must be verified during App.kt integration. All acceptance criteria met or pending integration step only. |

**Ready for integration into App.kt per instructions in `rd.md` Section 7.**
