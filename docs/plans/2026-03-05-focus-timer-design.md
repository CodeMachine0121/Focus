# Focus Timer Design

## Overview

A Pomodoro-style focus timer desktop app built with Kotlin Multiplatform + Compose for Desktop (JVM). Users set a time slice, watch a countdown with a circular progress ring, and receive a forced foreground alert when time is up.

## Requirements

- Preset quick-select buttons: 25 / 15 / 5 minutes
- Custom time input (manual minutes entry)
- Circular progress ring visualizing remaining time
- Start / Pause / Reset controls
- On completion: bring window to foreground forcefully + show AlertDialog

## Architecture

### Files

```
composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/
  main.kt                  # Entry point — passes ComposeWindow reference to ViewModel
  App.kt                   # Root composable
  FocusTimerViewModel.kt   # Timer state & logic (coroutine-driven)
  FocusScreen.kt           # Full UI screen
```

### FocusTimerViewModel

- Holds a `CoroutineScope` (SupervisorJob + Dispatchers.Default)
- State: `timerState: TimerState` (Idle | Running | Paused | Completed)
- State: `totalSeconds: Int`, `remainingSeconds: Int`
- On `start()`: launches coroutine with `delay(1_000)` loop, decrements `remainingSeconds`
- On completion: calls `onComplete()` callback provided by `main.kt`
- `onComplete` callback: calls `window.toFront()`, `window.isAlwaysOnTop = true` (briefly), sets `timerState = Completed`
- `isAlwaysOnTop` is reset to `false` after 500ms to avoid staying on top forever

### FocusScreen

- `TimerDisplay`: `Canvas`-based circular arc, center text showing `MM:SS`
- `PresetButtons`: Row of 3 Chip/Button — 25 min / 15 min / 5 min (disabled when Running)
- `CustomTimeInput`: `OutlinedTextField` accepting integer minutes (disabled when Running/Paused)
- `ControlButtons`: Start | Pause | Reset
- `CompletionDialog`: `AlertDialog` shown when `timerState == Completed`, dismissed on OK

### main.kt

- `application { Window(...) { ... } }` with `LocalWindow` or direct lambda capture of `ComposeWindow`
- Instantiates `FocusTimerViewModel`, passes `window` reference for foreground callback

## State Machine

```
Idle ──start()──> Running ──pause()──> Paused
                     │                    │
                  countdown=0          resume()──> Running
                     │
                  onComplete()──> Completed ──dismiss()──> Idle
     reset() from any state ──> Idle
```

## Key Technical Notes

- `window.toFront()` + `window.requestFocus()` on JVM brings window above other apps
- Briefly set `window.isAlwaysOnTop = true` then restore to ensure OS respects the request
- All timer logic runs on `Dispatchers.Default`; state updates use `mutableStateOf` which is thread-safe for reads, writes dispatched via coroutine
- `ComposeWindow` is accessible in `Window { }` lambda via `LocalWindow.current` or by capturing the AWT window from `WindowState`
