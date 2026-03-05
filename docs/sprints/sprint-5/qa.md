# Sprint 5 QA Report: System Tray Integration

## Feature Summary

Sprint 5 adds a persistent system tray icon to the Focus desktop app. The icon:
- Appears when the app launches, disappears on exit.
- Updates its tooltip every ~500 ms to show the current timer state (e.g. "Focus - 24:59 remaining", "Focus - Paused", "Focus - Complete!").
- Fires a native OS balloon/popup notification when a session completes.
- Exposes a right-click context menu with Start, Pause, Reset, and Quit.
- Degrades gracefully when `SystemTray.isSupported()` returns false.

Implementation spans two files:
- **NEW**: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/SystemTrayManager.kt`
- **MODIFIED**: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/main.kt` (+12 lines)

---

## Test Scenarios (Gherkin Scenario -> Verification Result)

### Scenario 1: System tray icon lifecycle

**Spec**: Tray icon appears on launch; tooltip reads "Focus - Ready"; icon is removed on app exit.

**Verification**:
- `SystemTrayManager.install()` calls `SystemTray.getSystemTray().add(icon)` on construction.
- Initial `TrayIcon` is constructed with tooltip `"Focus - Ready"` — matches spec.
- `SystemTrayManager.uninstall()` calls `SystemTray.getSystemTray().remove(it)` and cancels the coroutine scope.
- `DisposableEffect` in `main.kt` ensures `uninstall()` is called when the composable leaves composition (i.e., window closes).

**Result**: PASS

---

### Scenario 2: Tooltip updates during active countdown

**Spec**: Tooltip updates every second during Running state; reads "Focus - MM:SS remaining"; shows "Focus - Paused" on pause; "Focus - Ready" on reset.

**Verification**:
- `startPolling()` launches a coroutine that loops with `delay(500)`, comparing `timerState` and `remainingSeconds` against previous values.
- On change, `buildTooltip()` produces the correct string per state:
  - `TimerState.Running` → `"Focus - %02d:%02d remaining".format(mm, ss)` — correct two-digit formatting.
  - `TimerState.Paused` → `"Focus - Paused"`.
  - `TimerState.Idle` → `"Focus - Ready"`.
- `EventQueue.invokeLater` is used for AWT thread safety when setting tooltip.

**Edge case noted**: The poll interval is 500 ms, so tooltip lags at most 500 ms behind the actual ViewModel value — acceptable for a tooltip.

**Result**: PASS

---

### Scenario 3: Native notification fires when session completes

**Spec**: When countdown reaches 00:00, a native balloon notification appears with caption "Focus Complete" and text "Your focus session is complete."

**Verification**:
- `startPolling()` checks `state == TimerState.Completed && lastState != TimerState.Completed` before calling `showCompletionNotification()` — the notification fires exactly once per transition to Completed.
- `showCompletionNotification()` calls `trayIcon?.displayMessage("Focus Complete", "Your focus session is complete.", TrayIcon.MessageType.INFO)` — matches spec text exactly.
- Tray tooltip also changes to "Focus - Complete!" on this transition.

**Result**: PASS

---

### Scenario 4: Context menu controls mirror in-app controls

**Spec**: Right-click shows Start, Pause, Reset, Quit; each mirrors the ViewModel action.

**Verification**:
- `buildPopupMenu()` creates `MenuItem` items for Start, Pause, Reset, and Quit in that order with a separator before Quit.
- Start → `viewModel.start()`, Pause → `viewModel.pause()`, Reset → `viewModel.reset()`, all wrapped in `EventQueue.invokeLater` for AWT thread safety.
- Quit → `onQuit()` which is wired to `::exitApplication` from `main.kt`.

**Edge case noted**: There is no menu item enable/disable based on timer state. For example, "Start" is clickable even when the timer is already Running. `FocusTimerViewModel.start()` guards against this (`if (timerState == TimerState.Running ... return`) so the worst outcome is a no-op click, not a crash. However, from a UX standpoint, graying out inapplicable items would be ideal — flagged as a future improvement.

**Result**: PASS (functional correctness maintained via ViewModel guards)

---

### Scenario 5: Graceful degradation when SystemTray is not supported

**Spec**: When `SystemTray.isSupported()` returns false, no exception is thrown and the app functions normally.

**Verification**:
- `SystemTrayManager.install()` opens with `if (!SystemTray.isSupported()) return false`.
- In `main.kt`, the return value of `install()` is not checked — this is acceptable because failure is silent by design.
- `uninstall()` guards with `trayIcon?.let { ... }` — safe to call even if `install()` was never completed.
- No code path in `SystemTrayManager` throws unchecked exceptions under the non-supported path.

**Result**: PASS

---

## Code Review Notes

### Correctness

1. **Thread safety**: All AWT/Swing calls (`trayIcon?.toolTip = ...`, `displayMessage`, menu action listeners) are dispatched via `EventQueue.invokeLater` or are already on the AWT event thread. This is correct.
2. **ViewModel observation**: The poller reads `viewModel.timerState` and `viewModel.remainingSeconds` directly. These are Compose `mutableStateOf` properties. Reading them from a non-Compose coroutine (Dispatchers.Default) does not subscribe to recomposition, but since the poller loops continuously at 500 ms, it will always pick up changes within that window. This is correct for a tooltip/tray use case.
3. **Coroutine cancellation**: `scope.cancel()` is called in `uninstall()` before `trayIcon` removal. The poll coroutine will stop, and no further AWT events will be queued after cancellation. There is a tiny window where an `invokeLater` may have been enqueued before cancel but not yet executed — this is benign (it would just set a tooltip on an already-removed icon, which AWT ignores).
4. **Icon generation**: `createTrayImage()` produces a 64x64 ARGB `BufferedImage` with `isImageAutoSize = true` on the `TrayIcon` — the OS will scale it to the appropriate tray size. Correct approach.

### Edge Cases & Potential Issues

- **macOS System Tray on headless / CI environments**: `SystemTray.isSupported()` returns false in headless mode. The guard handles this correctly.
- **Multiple calls to `install()`**: If `install()` is somehow called twice, a second tray icon would be added. The `DisposableEffect` in Compose guarantees it is called at most once per composition lifetime, so this is not a practical risk.
- **`onQuit` blocking the AWT thread**: `onQuit` is `::exitApplication` from the Compose `application` DSL. Calling it from the AWT thread (menu action listener) triggers the application shutdown. This is acceptable — Compose for Desktop handles this correctly.
- **Menu item state not reflecting timer state**: As noted in Scenario 4, menu items are always enabled. Not a bug (ViewModel guards prevent invalid transitions), but a UX gap.
- **Notification availability on macOS**: `TrayIcon.displayMessage` on macOS with newer JDKs may silently no-op if the app does not have notification permissions in System Preferences. The balloon API is best-effort — the spec and implementation accept this limitation.

---

## Pass/Fail Verdict Per Scenario

| Scenario | Description | Verdict |
|----------|-------------|---------|
| 1 | Tray icon lifecycle (appears on launch, removed on exit) | PASS |
| 2 | Tooltip updates during active countdown | PASS |
| 3 | Native balloon notification on completion | PASS |
| 4 | Context menu Start/Pause/Reset/Quit actions | PASS |
| 5 | Graceful degradation when SystemTray not supported | PASS |

---

## Overall Sprint Verdict: PASS

All 5 Gherkin scenarios are correctly implemented and verified by code review. The build compiles cleanly (verified via `./gradlew compileKotlinJvm`). The implementation uses only standard JVM APIs (`java.awt.SystemTray`, `java.awt.TrayIcon`, `java.awt.PopupMenu`) with no new dependencies. The diff is minimal: one new file and a 12-line addition to `main.kt`.

**Recommended follow-up items (not blocking)**:
1. Disable/re-enable context menu items based on current timer state for better UX.
2. Investigate macOS notification permissions documentation for end-user setup guidance.
3. Left-click on tray icon to toggle window visibility (proposed for a future sprint).
