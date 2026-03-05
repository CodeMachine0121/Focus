# UI/UX Design: Ambient Sound Engine — Sounds Tab

## Sprint
Sprint 7

## Design Goals

1. **Immediate clarity** — the user understands in under two seconds whether audio is playing, what sound is selected, and what the volume is.
2. **Independence signal** — the layout communicates that this is a standalone control panel, not tied to the timer.
3. **Minimal cognitive load** — one action to start sound, one to stop. No nested menus, no modals.
4. **Consistency** — reuses Material3 components already present in the app (OutlinedButton, Slider, Switch pattern from FocusScreen, PrimaryTabRow from App.kt).
5. **Graceful states** — idle, playing, and error states each have distinct but subtle visual treatments.

---

## Sounds Tab Layout

The Sounds tab is a single scrollable Column centered horizontally with a fixed content width of ~320dp, matching the FocusScreen's horizontal padding conventions. All controls are vertically stacked with consistent spacing.

### Section Structure

1. **Header / Section label** — "Ambient Sound" title text
2. **Status indicator row** — pulsing animated dot + status text ("Playing" / "Stopped")
3. **Sound type selector** — four radio-button style rows (one per sound type)
4. **Volume control** — label + percentage + horizontal Slider
5. **Play/Stop button** — prominent full-width button
6. **(Optional) Error message** — shown only when audio device unavailable

---

## ASCII Wireframe

```
+------------------------------------------+
|  Timer  |  Sounds  |  History             |  <- PrimaryTabRow (tab index 1)
+------------------------------------------+
|                                          |
|         Ambient Sound                    |  <- Section heading (MaterialTheme.typography.titleMedium)
|                                          |
|   ( ) Pulsing dot   Stopped              |  <- Status row (dot is grey when stopped)
|                                          |
|   Sound                                  |  <- Label (labelSmall, secondary color)
|   +------------------------------------+ |
|   | (o) White Noise                    | |  <- RadioButton row (selected)
|   | ( ) Brown Noise                    | |
|   | ( ) Rain                           | |
|   | ( ) Binaural Focus                 | |
|   +------------------------------------+ |
|                                          |
|   Volume                        50%      |  <- Row: label left, value right
|   [--|---------------------------]        |  <- Slider (0..1, steps=0)
|                                          |
|   +------------------------------------+ |
|   |           Play                     | |  <- Button (filled when stopped)
|   +------------------------------------+ |
|                                          |
|   (error message shown here if           |  <- Shown only on audio failure
|    audio device unavailable)             |    (MaterialTheme.colorScheme.error)
|                                          |
+------------------------------------------+
```

Playing state variant of the status row and button:

```
|   [*] Pulsing dot   Playing              |  <- dot pulses (alpha animation 0.4->1.0)
|                                          |
|   +------------------------------------+ |
|   |           Stop                     | |  <- Button label changes, uses error color tint
|   +------------------------------------+ |
```

---

## Component Specifications

### Sound Type Selector

- Component: `Column` of `Row` items, each containing a `RadioButton` + `Text`
- Selection triggers `viewModel.selectSound(type)`
- All four options always visible (no dropdown); the list fits within the 390px window width
- Row padding: `vertical = 4.dp`, `horizontal = 8.dp`
- Text style: `MaterialTheme.typography.bodyMedium`
- Sound type display names:
  - `WHITE_NOISE` → "White Noise"
  - `BROWN_NOISE` → "Brown Noise"
  - `RAIN` → "Rain"
  - `BINAURAL_FOCUS` → "Binaural Focus"

### Volume Slider

- Component: Material3 `Slider`
- Range: `0f..1f`, `steps = 0` (continuous)
- `onValueChange` calls `viewModel.setVolume(value)` immediately (real-time)
- Value label: percentage string `"${(volume * 100).roundToInt()}%"` displayed inline to the right of the "Volume" label
- Modifier: `fillMaxWidth()`

### Play/Stop Button

- Component: Material3 `Button` (filled, full width)
- Label: "Play" when `isPlaying == false`, "Stop" when `isPlaying == true`
- Color: default primary when stopped; `ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)` when playing, to make the stop action visually prominent
- `onClick` calls `viewModel.togglePlayback()`

### Pulsing Status Indicator

- Component: `Canvas` drawing a filled circle of radius `6.dp`
- When stopped: circle color = `MaterialTheme.colorScheme.outline` (neutral grey), no animation
- When playing: circle color = `MaterialTheme.colorScheme.primary`, alpha animated with `rememberInfiniteTransition` pulsing between `0.35f` and `1.0f` over a 900ms cycle (`tween` easing `FastOutSlowIn`)
- Indicator is placed in a `Row` with `Spacer(4.dp)` before the status text label

### Status Text

- "Playing" when `isPlaying == true`, color = `MaterialTheme.colorScheme.primary`
- "Stopped" when `isPlaying == false`, color = `MaterialTheme.colorScheme.onSurfaceVariant`
- "Audio unavailable" when in error state, color = `MaterialTheme.colorScheme.error`
- Typography: `MaterialTheme.typography.labelMedium`

### Error Message

- Shown below the Play/Stop button using `AnimatedVisibility(visible = errorMessage != null)`
- Text component with `MaterialTheme.colorScheme.error` color
- Typography: `MaterialTheme.typography.bodySmall`
- Message: "Audio device unavailable. Check system audio settings."

---

## Interaction Flows

### Happy Path — Start Playback

1. User opens Sounds tab (stopped state, White Noise pre-selected, volume at 50%)
2. User taps a sound type radio button (e.g., Rain) → selection updates immediately, no audio starts
3. User optionally moves the volume slider → percentage label updates in real time
4. User presses "Play" button
5. Button label changes to "Stop" with error color tint
6. Pulsing dot begins pulsing; status text reads "Playing"
7. Rain ambient audio begins within ~50ms (one buffer cycle)

### Stop Playback

1. User presses "Stop"
2. Audio stops within one buffer cycle
3. Button returns to "Play" label and primary color
4. Pulsing stops; dot turns grey; status text reads "Stopped"

### Switch Sound Type While Playing

1. User selects a different sound type radio button while playing
2. Current audio stops; new audio starts seamlessly
3. isPlaying remains true; visual state (pulsing) does not flicker

### Tab Navigation During Playback

1. User starts playing White Noise on Sounds tab
2. User taps "Timer" tab
3. Audio continues — AmbientSoundViewModel outlives the Composable
4. User returns to Sounds tab; UI shows correct "Playing" state with pulsing indicator

---

## Spacing and Sizing Reference

| Element                  | Value                  |
|--------------------------|------------------------|
| Outer horizontal padding | 32.dp (matches FocusScreen) |
| Outer vertical padding   | 24.dp                  |
| Between major sections   | 20.dp Spacer           |
| Between radio rows       | 0.dp (natural row height) |
| Between label and slider | 8.dp                   |
| Button height            | default Material3      |
| Status dot radius        | 6.dp                   |
| Sound selector card elevation | 0 (flat, no card — consistent with rest of app) |

---

## Accessibility Notes

- All interactive elements (RadioButton, Slider, Button) have sufficient touch targets per Material3 guidelines.
- Status text is always present as a text label alongside the pulsing dot — animation alone is not the sole indicator of state.
- Sound type rows use RadioButton which has built-in semantics for screen readers.
- Volume slider uses default Slider semantics; the percentage label provides a numeric complement.
