# Sprint 2 QA Report

## Feature Summary

**Session History Log** — records completed focus sessions (date, time, duration) to Java `Preferences`-backed local storage and displays them in a new "History" tab with a today-total summary and empty-state handling.

---

## Test Scenarios

### Scenario 1: A completed session is saved to history

**Gherkin mapping:** "A completed focus session is saved to history"

**Verification:**
- `FocusTimerViewModel.dismiss()` now captures `totalSeconds / 60` as `completedDuration` before resetting state and invokes `onSessionDismissed?.invoke(completedDuration)`.
- In `App.kt`, `onSessionDismissed` is wired to `repository.record(durationMinutes)`.
- `SessionRepository.record()` calls `SessionRecord.now(durationMinutes)` to stamp the current date/time, prepends to `sessions`, then calls `saveToPrefs()` which writes to `Preferences` and flushes.
- The persisted format (newline-delimited JSON) is read back by `loadFromPrefs()` on the next app launch and populated into `sessions` during `SessionRepository` construction.

**Verdict: PASS** — record creation, persistence, and reload path are all correctly implemented.

---

### Scenario 2: History tab shows list of past sessions

**Gherkin mapping:** "User views their session history"

**Verification:**
- `App.kt` `TabRow` provides a "History" tab that renders `HistoryScreen(repository)`.
- `HistoryScreen` reads `repository.sessions` (a `SnapshotStateList`) which triggers recomposition when new items are added.
- `LazyColumn` iterates `sessions` and renders `SessionRow` for each entry, displaying `date`, `startTime`, and `durationMinutes`.
- Sessions are stored most-recent-first: `sessions.add(0, entry)` on record, and `sortedByDescending` on load.
- Today's summary card shows `todayTotalMinutes()` which sums durations for entries whose `date == today`.

**Verdict: PASS** — list display, ordering, and today summary are correctly implemented.

---

### Scenario 3: Empty state when no sessions exist

**Gherkin mapping:** "No sessions recorded yet"

**Verification:**
- `HistoryScreen` has an explicit `if (sessions.isEmpty())` branch that renders `Text("No sessions recorded yet.")` centered in the remaining space.
- The today summary card still renders showing "0 min" which is correct per the spec (today total is 0 when no sessions exist).

**Verdict: PASS** — empty state message is shown; no crash on empty list.

---

### Scenario 4: Today's total updates after multiple sessions

**Gherkin mapping:** "Today's total updates after each completed session"

**Verification:**
- `todayTotal` is computed via `remember(sessions.size) { repository.todayTotalMinutes() }` in `HistoryScreen`.
- `sessions.size` is the key, so recomposition is triggered whenever a session is added or removed.
- `todayTotalMinutes()` filters by today's date string and sums `durationMinutes`, so accumulation across sessions is correct.

**Note:** Using `sessions.size` as the `remember` key means the total recomputes when any session is added. This is correct for the add-only scenario in scope.

**Verdict: PASS**

---

### Scenario 5: Historical sessions from prior days appear but do not affect today total

**Gherkin mapping:** "Historical sessions from prior days appear but do not affect today total"

**Verification:**
- `SessionRecord.today()` returns `LocalDate.now().format(DATE_FMT)`.
- `todayTotalMinutes()` uses `filter { it.date == today }`, so only records with today's date string contribute to the total.
- Records from prior days are loaded and displayed in `HistoryScreen` without filtering, so they appear in the list.

**Verdict: PASS**

---

## Code Review Notes

### Correctness

1. **`dismiss()` always records** — `onSessionDismissed` is called unconditionally from `dismiss()`. If the user somehow calls `dismiss()` when not in `Completed` state (e.g., from a future refactor), a spurious record could be written. Currently safe because `dismiss()` is only called from the `AlertDialog` which is gated on `state == TimerState.Completed`. Low risk.

2. **`SideEffectOnce` implementation** — The custom `SideEffectOnce` composable runs the block on the first call in the composition, not inside a `SideEffect`. This means the callback is wired synchronously during the composition phase rather than after layout, which is technically an anti-pattern (side effects during composition). The correct approach would be `LaunchedEffect(Unit)` or the standard `SideEffect`. However, since the block merely assigns a lambda to a ViewModel property (a simple write, not a UI operation), this is functionally safe and will not cause observable bugs in practice.

3. **`totalSeconds / 60` duration calculation** — `completedDuration` is derived from `totalSeconds` (the original configured duration) rather than from actual elapsed time. This matches the stakeholder requirement of recording the configured duration, and is correct because `remainingSeconds` reaches zero exactly when the session completes.

4. **JSON parsing robustness** — The manual JSON encoder/decoder handles only the specific fields written by `toJson()`. It correctly uses a non-capturing exception handler (`catch (_: Exception)`). Edge case: if a user's system clock produces a date/time containing a `"` character (impossible for `yyyy-MM-dd` and `HH:mm:ss` patterns), the JSON would be malformed. This is not a practical risk.

5. **Preferences key length** — Java Preferences keys have a maximum value size of 8 KB per JVM spec. For large numbers of sessions this could overflow. A defensive cap (e.g., keep latest 500 sessions) would be prudent for production but is outside sprint scope.

### Edge Cases

- **`durationMinutes <= 0` guard in `record()`** — correctly prevents zero-duration records if `totalSeconds < 60` (e.g., custom input of "0" was blocked by ViewModel already, but the guard is defense-in-depth).
- **App restart persistence** — `SessionRepository` constructor calls `loadFromPrefs()` immediately, so history is available without any explicit load call.
- **`sessions` thread safety** — `SnapshotStateList` mutations and `Preferences` writes both happen on the Main dispatcher (UI thread) since `repository.record()` is called from the dismiss button click handler. No concurrency issue.

### Potential Issues

- **`remember(sessions.size)` for `todayTotal`** — If sessions are removed (not in scope this sprint, but possible in future), the recomputation still triggers. This is correct behavior.
- **Tab state not persisted** — After app restart, the user always lands on the Timer tab. This is acceptable UX for sprint 2.

---

## Pass/Fail Summary

| Scenario | Verdict |
|---|---|
| 1. Completed session saved and persisted | PASS |
| 2. History tab shows session list | PASS |
| 3. Empty state when no sessions | PASS |
| 4. Today total accumulates across sessions | PASS |
| 5. Prior-day sessions visible but excluded from today total | PASS |

---

## Overall Sprint Verdict: PASS

All 5 Gherkin scenarios have corresponding correct implementations. The core acceptance criteria from the stakeholder document are satisfied:

- Session records are saved on dismiss with date, time, and duration. [PASS]
- History tab displays a scrollable session list with human-readable data. [PASS]
- A "Today" summary row shows total focused minutes for today. [PASS]
- Persistence via Java Preferences survives app restart. [PASS]
- Empty-state message is shown when no sessions exist. [PASS]

No external dependencies were added. Code is idiomatic Kotlin/Compose. Changes to existing files were minimal (4 lines added to `FocusTimerViewModel.kt`, `App.kt` rewritten to add tab navigation). Two minor non-blocking issues were noted (SideEffectOnce composition-phase side effect; Preferences size ceiling) and should be addressed in a future sprint.
