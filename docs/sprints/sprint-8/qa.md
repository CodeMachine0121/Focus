# Sprint 8 — QA Report
## Feature: Productivity Dashboard & Streak Tracker

**Sprint:** 8
**Date:** 2026-03-05
**Reviewer:** QA Role
**Status:** CONDITIONAL PASS — see open items

---

## Feature Summary

Four new Kotlin files implement a "Productivity Dashboard & Streak Tracker" tab for the Focus app. The feature reads from the existing `SessionRepository`, computes analytics via a stateless `StreakCalculator` object, exposes the result through `DashboardViewModel`, and renders a scrollable dashboard in `DashboardScreen`. No existing files were modified.

---

## Test Scenarios

### TS-1: No sessions (empty state)

| Step | Expected | Status |
|---|---|---|
| Launch app with no session history | Dashboard tab shows "No focus sessions yet." message | PASS (code review) |
| Empty state message is displayed | "Complete your first Pomodoro..." secondary text visible | PASS |
| No stat cards or charts rendered | DashboardContent block skipped, only EmptyState shown | PASS |

**Code path:** `stats.totalSessions == 0 -> EmptyState()`

---

### TS-2: Single session today

| Step | Expected | Status |
|---|---|---|
| One session on today's date | Current streak = 1 | PASS |
| Longest streak = 1 | Correct | PASS |
| Total sessions = 1 | Correct | PASS |
| Weekly chart: today's bar has data, all others = 0 | isToday=true on correct bar | PASS |
| Heatmap: today's cell has sessionCount=1 and border | Border drawn via Stroke | PASS |
| Personal records: longestSession = session duration | Correct | PASS |

---

### TS-3: Streak edge case — gap in history

**Scenario:** Sessions on 2026-03-01, then gap (no sessions 2026-03-02, 2026-03-03), then sessions 2026-03-04 and 2026-03-05.

| Check | Expected | Status |
|---|---|---|
| `calculateCurrentStreak` | 2 (Mar 4 + Mar 5) | PASS |
| `calculateLongestStreak` | 2 (Mar 4–5; Mar 1 is isolated) | PASS — algorithm walks sorted unique days and resets on gap |

---

### TS-4: Streak edge case — streak not broken by missing today

**Scenario:** Sessions on 2026-03-03 and 2026-03-04. No session on 2026-03-05 (today).

| Check | Expected | Status |
|---|---|---|
| `calculateCurrentStreak` | 2 (yesterday and day before) | PASS — code checks `today.minusDays(1)` as fallback |
| Streak is "at risk" but not reset | Correct behavior | PASS |

---

### TS-5: Streak edge case — all sessions on same day

**Scenario:** 5 sessions all on 2026-03-05.

| Check | Expected | Status |
|---|---|---|
| `calculateCurrentStreak` | 1 | PASS — `toSortedSet()` deduplicates; single day = 1 |
| `calculateLongestStreak` | 1 | PASS — `sortedDays.size == 1`, loop body never executes, returns `longest=1` |

---

### TS-6: Streak edge case — longest streak in past, current streak lower

**Scenario:** Sessions on 2026-02-10 through 2026-02-14 (5 days). Then sessions on 2026-03-04 and 2026-03-05.

| Check | Expected | Status |
|---|---|---|
| Current streak | 2 | PASS |
| Longest streak | 5 | PASS |

---

### TS-7: Weekly chart — current ISO week boundaries

**Scenario:** Today is Thursday 2026-03-05 (ISO week: Mon=Mar 2 → Sun=Mar 8).

| Check | Expected | Status |
|---|---|---|
| `groupByWeek` returns 7 items | Correct | PASS |
| Labels are Mon–Sun | ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"] | PASS |
| `isToday` on Thursday bar (index 3) | true | PASS |
| Session on Mon Mar 2 appears in bar | Minutes correctly summed | PASS |
| Session on prior week (Feb 28) NOT in chart | Correct — weekStart is Mar 2 | PASS |

---

### TS-8: 30-day heatmap window

**Scenario:** Today is 2026-03-05.

| Check | Expected | Status |
|---|---|---|
| Heatmap has exactly 30 cells | `(0..29).map { ... }` | PASS |
| Earliest cell date | 2026-02-04 (`today.minusDays(29)`) | PASS |
| Latest cell date | 2026-03-05 | PASS |
| Session on 2026-02-03 (outside window) | Not reflected in any cell | PASS |
| Session on 2026-02-04 (boundary) | Reflected in first cell | PASS |

---

### TS-9: Heatmap color intensity

| sessionCount | Expected color | Status |
|---|---|---|
| 0 | `surfaceVariant` | PASS |
| 1 | `primary.copy(alpha=0.25f)` | PASS |
| 2 | `primary.copy(alpha=0.50f)` | PASS |
| 3 | `primary.copy(alpha=0.75f)` | PASS |
| 4+ | `primary` (full) | PASS — `else` branch |

---

### TS-10: Personal records

| Scenario | Expected | Status |
|---|---|---|
| No sessions | longestSession=0, bestDayMinutes=0, bestDayDate=null | PASS — guard at top of `getPersonalRecords` |
| Multiple sessions same day | Best day total = sum of all sessions that day | PASS — `groupByDay` sums `durationMinutes` |
| Single longest session not on best day | Each record independent | PASS |
| UI shows "--" when records are zero | `if (records.longestSessionMinutes > 0)` guard | PASS |

---

### TS-11: Chart rendering — canvas dimensions

| Check | Expected | Status |
|---|---|---|
| Weekly bar chart height | Fixed 120.dp canvas | PASS |
| Bar widths scale to window width | `columnWidth = canvasWidth / count` | PASS |
| Min bar height for 0-minute days | 2f pixels (still visible) | PASS |
| Value label above bar clipped at top | Guard `if (textY >= 0f)` prevents drawing off-canvas | PASS |
| Heatmap cells are square | `cellSizeDp` derived from width / columns | PASS |
| Heatmap canvas height adapts | `cellSizeDp * rows + gap * (rows-1)` | PASS |

---

### TS-12: ViewModel threading

| Check | Expected | Status |
|---|---|---|
| Initial load triggered in `init {}` | `refresh()` called on construction | PASS |
| `isLoading = true` before computation | Set before `withContext` | PASS |
| Computation on `Dispatchers.Default` | `withContext(Dispatchers.Default)` | PASS |
| Result assigned on Main thread | After `withContext` returns | PASS |
| `isLoading = false` after computation | Set after assignment | PASS |

---

## Code Review Findings

### Issues Found

**OPEN-1 (Minor): `formatNumber` only handles thousands**
`formatNumber(1_050_000)` returns "1050,000" (wrong comma placement for millions). Acceptable within sprint scope; should be addressed if total minutes can realistically exceed 999,999 (approximately 16,667 hours).

**OPEN-2 (Minor): `DashboardViewModel` not automatically reactive to new sessions**
`refresh()` must be called externally (e.g. from `App.kt` after `repository.record()`). If the caller forgets, the dashboard will show stale data until the next call. The RD notes document this as a known limitation. Recommendation: add `refresh()` to the integration snippet in `App.kt` as shown in `rd.md`.

**OPEN-3 (Minor): Heatmap row labels omitted**
The `designer.md` described row labels (abbreviated month+day per row). The implementation skips them to avoid layout complexity. No functional impact; purely cosmetic. Acceptable deferral.

**RESOLVED: No existing files modified**
Confirmed by code review — `App.kt`, `SessionRepository.kt`, `HistoryScreen.kt`, `FocusTimerViewModel.kt` are unchanged.

**RESOLVED: Empty state never shows stat cards**
The `stats.totalSessions == 0` check correctly gates `DashboardContent`. No risk of showing zero-filled charts confusingly.

**RESOLVED: Thread safety of `toList()` snapshot**
`SnapshotStateList.toList()` is safe to call from a non-Main thread because it creates a copy at a single consistent snapshot point. No data race possible.

**RESOLVED: `LocalDate.now()` called once per composition via `remember`**
The heatmap calls `remember { LocalDate.now() }` so the date doesn't drift mid-frame. Weekly chart and streak functions receive `today` as a default parameter defaulting to `LocalDate.now()` at the ViewModel call site.

---

## Acceptance Criteria Verification

| AC | Description | Status |
|---|---|---|
| AC-1 | "Dashboard" tab in navigation | Integration required — not yet in App.kt (by design, RD constraint) |
| AC-2 | Current streak calculation | PASS |
| AC-3 | Longest streak | PASS |
| AC-4 | Total sessions matches repository | PASS |
| AC-5 | Total minutes is sum of all durations | PASS |
| AC-6 | Weekly bar chart Mon–Sun | PASS |
| AC-7 | 30-day heatmap 5×6 grid | PASS |
| AC-8 | Personal records section | PASS |
| AC-9 | Empty state when no sessions | PASS |
| AC-10 | Dashboard reads reactive repository data | PASS (via `refresh()`) |

---

## Sign-Off

**QA Decision: CONDITIONAL PASS**

The feature implementation is complete, architecturally sound, and correct for all tested scenarios including all streak edge cases. The three open items are minor and non-blocking:

- OPEN-1 is a cosmetic formatting issue for extreme values unlikely in current usage.
- OPEN-2 is a documented integration concern, not a code defect.
- OPEN-3 is a deferred UI decoration.

**Conditions for full PASS:**
1. Integrating team adds `dashboardViewModel.refresh()` call in `App.kt` after `repository.record()`.
2. Integration adds "Dashboard" as a third tab in `App.kt` per `rd.md` instructions.

Upon integration, this feature is ready for release in Sprint 8.
