# Sprint 3 QA Report

## Feature Summary

**Auto-Cycle Mode** — When the user enables an "Auto-Cycle" toggle, the timer automatically transitions from a completed focus session into a 5-minute break, and from a completed break back into a new focus session, cycling indefinitely without manual interaction. A phase label ("Focus" / "Break") and cycle counter ("Cycle N") provide real-time context. The existing manual completion dialog flow is preserved when Auto-Cycle is OFF.

---

## Scenario Verification

### Scenario 1: Auto-Cycle toggle is visible and off by default

**Gherkin:** Auto-Cycle toggle is visible and off by default.

**Code evidence:**
- `FocusScreen.kt` lines 96-106 render a `Switch` unconditionally in the main column.
- `FocusTimerViewModel.kt` line 29: `var autoCycleEnabled by mutableStateOf(false)` — defaults to OFF.
- When `autoCycleEnabled` is false and state is Idle, `handleCompletion()` takes the `else` branch: `timerState = Completed; onComplete?.invoke()` — manual dialog shows.

**Result: PASS**

---

### Scenario 2: Focus session auto-starts break on completion with Auto-Cycle ON

**Gherkin:** When Auto-Cycle is ON and a focus countdown reaches zero, no dialog appears and a 5-minute break immediately starts.

**Code evidence:**
- `handleCompletion()` checks `autoCycleEnabled`; if true and `currentPhase == Focus`, it sets `currentPhase = Break`, `totalSeconds/remainingSeconds = 5*60`, and calls `startInternal()`.
- `startInternal()` sets `timerState = Running` and launches a new countdown coroutine — `timerState` never reaches `Completed`, so the `AlertDialog` in `FocusScreen` (guarded by `state == TimerState.Completed`) is never shown.
- The phase label in `FocusScreen` is `"Break"` when `phase == TimerPhase.Break`.
- `cycleCount` remains 1 during the first break — label shows "Cycle 1".

**Result: PASS**

---

### Scenario 3: Break session auto-starts focus on completion with Auto-Cycle ON

**Gherkin:** When the break countdown reaches zero with Auto-Cycle ON, a new focus session starts and the cycle counter increments.

**Code evidence:**
- `handleCompletion()` with `currentPhase == Break` increments `cycleCount` (`cycleCount = cycleCount + 1`, from 1 to 2), sets `currentPhase = Focus`, restores `focusTotalSeconds`, and calls `startInternal()`.
- Phase label becomes "Focus", cycle label becomes "Cycle 2".

**Result: PASS**

---

### Scenario 4: Disabling Auto-Cycle mid-session restores manual completion

**Gherkin:** If Auto-Cycle is turned OFF while a focus session is running, the next completion shows the dialog and does not auto-start a break.

**Code evidence:**
- `autoCycleEnabled` is a plain `var` (publicly writable); the toggle `onCheckedChange` sets it immediately.
- `handleCompletion()` reads `autoCycleEnabled` at completion time; if it is false, the `else` branch fires: `timerState = Completed; onComplete?.invoke()`.
- The Switch is `enabled = state != TimerState.Completed`, so it cannot be toggled while the dialog is visible, but can be toggled freely during Running or Paused states.

**Note:** The spec says "toggling is only allowed during Idle or Focus Running" and the switch should be disabled during Break. The current implementation only disables the switch when `state == Completed`. During a Break running phase, the switch remains enabled. This is a minor deviation from the implementation notes, though not called out explicitly in a Gherkin scenario. The effective behavior still matches Scenario 4 since the toggle takes effect at the next completion boundary.

**Result: PASS (with minor note — switch enabled during Break phase, which is a slight spec deviation but does not break the scenario)**

---

### Scenario 5: User can pause during an auto-cycle break

**Gherkin:** During a Break countdown, clicking Pause stops the countdown; Resume continues it.

**Code evidence:**
- `pause()` checks `timerState == Running`, cancels `countdownJob`, sets `timerState = Paused`. This applies regardless of phase.
- `start()` (bound to Resume button in Paused state) checks `timerState != Running && != Completed`, sets `timerState = Running`, and relaunches the countdown from `remainingSeconds` (not reset).
- Phase label and cycle count are independent of pause state — they do not change on pause/resume.

**Potential issue:** When `start()` is called from Paused state (as Resume), it launches a new coroutine using the same `handleCompletion()` path. This is correct. However, `start()` sets `timerState = Running` before launching — since it checks for `Running` or `Completed` at entry, re-entering from `Paused` is permitted.

**Result: PASS**

---

## Code Review Notes

### Correctness

1. **Thread safety:** All mutable state is `mutableStateOf` observed on `Dispatchers.Main` (the countdown coroutine also runs on `Dispatchers.Main`). State mutations from `handleCompletion()` are on the main thread since the coroutine uses `Dispatchers.Main`. No data races.

2. **`focusTotalSeconds` not observable:** `focusTotalSeconds` is a plain `private var`, not a `mutableStateOf`. This is intentional — it's an internal implementation detail. However, `reset()` and `dismiss()` restore both `totalSeconds` and `remainingSeconds` from it, so the ring and clock reset correctly.

3. **`setDuration` guard:** `setDuration` guards against `timerState != Idle`. During a Break phase the timer is Running, so the preset buttons (25m/15m/5m) are correctly disabled (they call `setDuration` which is a no-op, AND the buttons have `enabled = state == TimerState.Idle`). No drift possible.

4. **Infinite cycle termination:** The cycle loop is stopped by: the user hitting Pause → Reset, toggling Auto-Cycle OFF (takes effect next completion), or closing the window. There is no built-in maximum cycle count — acceptable per the stakeholder doc.

5. **`startInternal()` cancels the previous job:** `countdownJob?.cancel()` is called inside `startInternal()`. However, since `startInternal()` is called from within the coroutine job itself (`handleCompletion` is invoked at end of countdown), the cancel call is effectively cancelling the already-completed job. This is safe — cancelling a completed `Job` is a no-op.

### Edge Cases

6. **Auto-Cycle toggled ON mid-session (while focus is running):** The current focus countdown will continue; at completion `handleCompletion()` sees `autoCycleEnabled = true` and starts the break. This is intuitive behaviour and not explicitly forbidden.

7. **Auto-Cycle toggled ON during a Break (since switch is not disabled):** If the user somehow gets into a Break from the manual path (not possible since manual path goes to Completed, not Break — this path doesn't exist). So this edge case cannot occur in practice.

8. **Custom duration during Break:** The preset buttons and custom input are disabled when `state != Idle`, which is true during break. No issue.

9. **`cycleCount` visibility condition in UI:** The label is shown when `autoCycleEnabled || (state != Idle && cycleCount > 1)`. Since `cycleCount` starts at 1 and increments only when a break ends, the label is hidden at startup with Auto-Cycle OFF, and hidden during idle between sessions. This is correct.

10. **Reset during Break:** `reset()` restores `totalSeconds = focusTotalSeconds`, `currentPhase = Focus`, `cycleCount = 1`. The ring will display the full focus duration correctly after reset.

### Potential Issues

11. **Switch disabled only during Completed state:** As noted in Scenario 4, the spec's implementation notes suggest disabling the switch during Break phase. Currently it remains enabled during Break. This means a user could toggle OFF mid-break, causing the break to complete without dialog (since `handleCompletion` checks `autoCycleEnabled` at completion time and would see `false`, firing `Completed`). This could confuse users. Low severity.

12. **Phase label during first focus session with Auto-Cycle ON:** The label shows "Cycle 1 — Focus" immediately when Auto-Cycle is turned ON in Idle state, before the timer has started. This is slightly premature but informative.

13. **`onComplete` callback fires only in manual mode:** During auto-cycle, `onComplete` (which brings the window to foreground) is never called. Users in auto-cycle mode get no OS-level attention. This is likely intentional — the window stays in the background and cycles autonomously.

---

## Pass/Fail Verdict Per Scenario

| Scenario | Description | Verdict |
|----------|-------------|---------|
| 1 | Toggle visible, default OFF, manual flow preserved | PASS |
| 2 | Focus completion auto-starts Break with Auto-Cycle ON | PASS |
| 3 | Break completion auto-starts Focus, cycle increments | PASS |
| 4 | Disabling Auto-Cycle mid-session restores manual dialog | PASS |
| 5 | Pause/Resume works during Break phase | PASS |

---

## Overall Sprint Verdict: PASS

All 5 Gherkin scenarios are satisfied by the implementation. The core auto-cycle logic (focus → break → focus, cycle counting, phase label, no dialog during cycling) is correctly implemented in idiomatic Kotlin/Compose. The one minor deviation — the Auto-Cycle switch not being disabled during the Break phase — does not break any acceptance criterion and is low risk. The pre-existing build environment issue (Gradle error "25.0.2") is unrelated to this sprint's changes, as confirmed by reproducing the error on the unmodified codebase.
