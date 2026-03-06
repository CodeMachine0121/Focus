# Issue #3 Resolution — System Tray Icon State Indication

## Issue Summary
The system tray icon was a static indigo circle, giving no visual indication of whether a focus session was active, paused, or in a break phase. Users had to hover to read the tooltip to know the app's current state.

## Root Cause
`SystemTrayManager.createTrayImage()` was a parameterless function that always returned the same indigo icon. The `startPolling()` loop updated only `trayIcon.toolTip`; it never modified `trayIcon.image`.

## Fix Implemented

### Icon generation (`SystemTrayManager.kt`)
- Replaced the static `createTrayImage()` with `createTrayImage(isActive: Boolean, phase: TimerPhase)`.
- Three distinct icon states:
  - **Idle** (`isActive = false`): gray ring `#787878` — neutral, unobtrusive.
  - **Focusing / Paused** (`isActive = true, phase = Focus`): red ring `#E53935` — attention-grabbing, signals work mode.
  - **Break** (`isActive = true, phase = Break`): blue ring `#1E88E5` — calm, signals rest.
- Icon uses a donut (filled outer circle + white inner circle) for clear visibility at small tray sizes.

### Polling loop (`startPolling()`)
- Added `val phase = viewModel.currentPhase` alongside the existing state/remaining reads.
- On any change, computes `isActive = state == Running || state == Paused`.
- Calls `createTrayImage(isActive, phase)` and sets `trayIcon.image = newIcon` on the AWT Event Queue alongside the tooltip update.
- `lastState` change detection means the icon only updates when something actually changes — no unnecessary redraws every 500 ms.

## Files Modified
- `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/SystemTrayManager.kt`

## Test Steps
1. Launch the app. Confirm the tray icon is **gray** (idle).
2. Click Start. Confirm the tray icon turns **red** (focusing).
3. Click Pause. Confirm the tray icon stays **red** (still paused focus session).
4. Enable Auto-Cycle and let the focus timer complete. Confirm the icon turns **blue** (break).
5. Hover over the tray icon. Confirm the tooltip correctly shows remaining time / phase.
6. Reset the timer. Confirm the icon returns to **gray**.

## Build Result
`BUILD SUCCESSFUL` — `./gradlew compileKotlinJvm` with Java 21.
