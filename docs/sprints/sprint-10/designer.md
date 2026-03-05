# UI/UX Design: Multi-Timer Workspace

## Sprint 10 | Designer Role

---

## Design Principles

- Each timer card must feel **self-contained and scannable** — the user should immediately know a timer's label, time remaining, and status without reading more than one element.
- The workspace should feel **calm under load** — six timers simultaneously counting down should not feel chaotic. Cards use muted accent colors with only the progress arc animating.
- **Spatial consistency**: All cards are equal size in a 2-column grid. No card grows or shifts when its state changes.
- Completion state uses a **warm amber/green** highlight so the user can spot finished timers at a glance.

---

## Layout: Workspace Screen

```
+--------------------------------------------------+
|  [Timer]  [History]  [Workspace]                 |  <- PrimaryTabRow
+--------------------------------------------------+
|                                                  |
|  +--------------------+  +--------------------+  |
|  |  LABEL: Build      |  |  LABEL: Review     |  |
|  |                    |  |                    |  |
|  |   ( ======= )      |  |   (  -  -  -  )    |  |
|  |     44:58          |  |     25:00          |  |
|  |   [Pause] [Reset]  |  |   [Start] [Reset]  |  |
|  |         [Remove]   |  |         [Remove]   |  |
|  +--------------------+  +--------------------+  |
|                                                  |
|  +--------------------+  +--------------------+  |
|  |  LABEL: Deploy     |  |  LABEL: Standup    |  |
|  |                    |  |                    |  |
|  |   (   DONE!  )     |  |   ( == )           |  |
|  |     00:00          |  |     09:12          |  |
|  |   [Reset] [Remove] |  |   [Pause] [Reset]  |  |
|  |  [* Completed *]   |  |         [Remove]   |  |
|  +--------------------+  +--------------------+  |
|                                                  |
|  +--------------------+                          |
|  |  Empty slot        |                          |
|  +--------------------+                          |
|                                                  |
|           [ + Add Timer ]                        |  <- Button at bottom
+--------------------------------------------------+
```

---

## Component Specifications

### WorkspaceScreen

- Root: `Column` filling the screen.
- A `LazyVerticalGrid` with `GridCells.Fixed(2)`, `contentPadding = PaddingValues(12.dp)`, `verticalArrangement = Arrangement.spacedBy(12.dp)`, `horizontalArrangement = Arrangement.spacedBy(12.dp)`.
- Below the grid (or overlaid as a `Box`): an **"Add Timer" button** (`Button` or `ExtendedFloatingActionButton`) centered horizontally, disabled when timer count == 6.
- An `AddTimerDialog` composable rendered conditionally when `showDialog == true`.

### WorkspaceTimerCard

Each card is a `Card` with `elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)` and `modifier = Modifier.fillMaxWidth()`.

Internal layout (top to bottom, `Column`, `Alignment.CenterHorizontally`):

1. **Label row** — `Text` with `fontSize = 13.sp`, `fontWeight = FontWeight.Medium`, `maxLines = 1`, `overflow = TextOverflow.Ellipsis`, color = `MaterialTheme.colorScheme.onSurfaceVariant`. Padded `8.dp` top.

2. **Progress arc + time** — `Box(contentAlignment = Alignment.Center)`:
   - `Canvas(modifier = Modifier.size(96.dp))` drawing two arcs:
     - **Track arc**: full 360°, color = `MaterialTheme.colorScheme.surfaceVariant`, stroke width = 8.dp, `StrokeCap.Round`.
     - **Progress arc**: `sweepAngle = 360f * progress`, color varies by state (see color table below), stroke width = 8.dp, `StrokeCap.Round`, starts at -90° (12 o'clock).
   - Centered `Text` with MM:SS, `fontSize = 22.sp`, `fontWeight = FontWeight.Bold`.

3. **Status indicator** (Completed only) — a small `Surface` pill with `shape = RoundedCornerShape(50%)`, background = `Color(0xFF4CAF50)` (green), containing `Text("Done", fontSize = 11.sp, color = White)`. Visible only when `state == Completed`.

4. **Control buttons row** — `Row(horizontalArrangement = Arrangement.spacedBy(4.dp))`:
   - **Start / Pause / Resume** button (primary `Button`): label is "Start" (Idle), "Pause" (Running), "Resume" (Paused). Hidden when Completed.
   - **Reset** `OutlinedButton`: always visible, enabled when state != Idle.
   - **Remove** `IconButton` with `Icons.Default.Close` icon, always enabled.

#### Color Table by State

| State     | Progress Arc Color                   | Card Border     |
|-----------|--------------------------------------|-----------------|
| Idle      | `MaterialTheme.colorScheme.primary`  | None            |
| Running   | `MaterialTheme.colorScheme.primary`  | None            |
| Paused    | `MaterialTheme.colorScheme.secondary`| Dashed (optional)|
| Completed | `Color(0xFF4CAF50)` (green)           | Subtle green tint|

When `state == Completed`, the card background is tinted using `MaterialTheme.colorScheme.surface` blended with a faint green: `Color(0xFFE8F5E9)`.

---

### AddTimerDialog

An `AlertDialog` overlay:

```
+----------------------------------+
|  Add Timer                       |
|                                  |
|  Label:                          |
|  [ _________________________ ]   |  <- OutlinedTextField, maxChars 60
|                                  |
|  Duration (minutes):             |
|  [ ___ ]                         |  <- OutlinedTextField, numeric, 1-999
|                                  |
|  [  Cancel  ]   [  Add  ]        |
+----------------------------------+
```

- **Label field**: `OutlinedTextField`, `singleLine = true`, max 60 chars, optional (defaults to "Timer N" if blank).
- **Duration field**: `OutlinedTextField`, `keyboardType = KeyboardType.Number`, digits only, must be >= 1. The "Add" button (`TextButton`) remains disabled until duration > 0.
- **Cancel** dismisses without changes.
- **Add** calls `workspaceViewModel.addTimer(label, minutes)` and closes the dialog.

---

## Interaction States & Transitions

```
[Idle]  --[Start]-->  [Running]  --[Pause]-->  [Paused]
  ^                       |                       |
  |                    (zero)                 [Resume]-->  [Running]
  |                       v
  +--------[Reset]--  [Completed]
```

- The "Start" button label becomes "Pause" when Running, "Resume" when Paused.
- Reset is available from Running, Paused, and Completed states.
- The Remove button is available in all states.

---

## Accessibility & Usability Notes

- All interactive elements have `contentDescription` set for screen reader support.
- Timer cards use sufficient color contrast (WCAG AA) — the MM:SS text is bold white or on-surface dark.
- The completion green (`#4CAF50`) meets 4.5:1 contrast against white text.
- The grid is scrollable (LazyVerticalGrid handles this natively) so 6 cards are always reachable even if the window is short.
- Disabled "Add Timer" button includes a tooltip or helper text: "Maximum 6 timers reached."

---

## Spacing & Sizing Summary

| Element                 | Value          |
|-------------------------|----------------|
| Grid columns            | 2 (fixed)      |
| Grid item spacing (H/V) | 12.dp          |
| Grid content padding    | 12.dp          |
| Card corner radius      | 12.dp          |
| Progress arc canvas     | 96.dp x 96.dp  |
| Arc stroke width        | 8.dp           |
| MM:SS font size         | 22.sp          |
| Label font size         | 13.sp          |
| "Done" badge font size  | 11.sp          |
| Button row spacing      | 4.dp           |
| Card internal padding   | 12.dp          |
