# Issue Analysis — Support Sprint

## Issue #1: History page should show title and see the detail

### Root Cause
`SessionRecord` has no `label` field. The `HistoryScreen` only displays `date`, `startTime`, and `durationMinutes`. Even though `FocusTimerViewModel` captures a `sessionLabel`, it is discarded when `onSessionDismissed` fires — the callback signature is `((Int) -> Unit)?`, passing only duration.

### Proposed Fix
1. Add `label: String = ""` to `SessionRecord`; update `toJson`/`fromJson` (backwards-compatible).
2. Change `FocusTimerViewModel.onSessionDismissed` to `((Int, String) -> Unit)?`; pass `sessionLabel` in `dismiss()`.
3. Update `SessionRepository.record()` to accept `label: String = ""`.
4. Update `App.kt` callback to forward the label.
5. Rewrite `HistoryScreen` `SessionRow` to show label as primary text (fallback "Unnamed Session"), with expandable detail on click.

### Files to Modify
- `SessionRecord.kt`
- `SessionRepository.kt`
- `FocusTimerViewModel.kt`
- `App.kt`
- `HistoryScreen.kt`

---

## Issue #2: UI should be more modern

### Root Cause
The `FocusScreen` uses plain `Column` layout with no visual hierarchy or color differentiation. The timer ring is bare Canvas on a white background. Buttons are minimal `OutlinedButton`s with no elevation or contrast.

### Proposed Fix
- Wrap the entire screen in a `Surface` with `MaterialTheme.colorScheme.background`.
- Enclose the timer Canvas+text in a `Card` with elevation for depth.
- Style the phase/cycle line as a colored `SuggestionChip`.
- Use `FilledTonalButton` for the primary action and style the Reset as a distinct `OutlinedButton`.
- Add subtle spacing improvements throughout.

### Files to Modify
- `FocusScreen.kt`

---

## Issue #3: Icon doesn't show indication

### Root Cause
`SystemTrayManager.createTrayImage()` always produces the same static indigo circle regardless of timer state. The polling loop updates only the tooltip, never the icon image.

### Proposed Fix
- Add `createTrayImage(isActive: Boolean, phase: TimerPhase)` that draws different colored circles:
  - Idle: gray (`#787878`)
  - Focusing (Running/Paused): red (`#E53935`)
  - Break: blue (`#1E88E5`)
- In the polling loop, detect state changes and call `trayIcon.image = createTrayImage(...)` accordingly.

### Files to Modify
- `SystemTrayManager.kt`
