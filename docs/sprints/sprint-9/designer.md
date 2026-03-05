# Sprint 9 — Designer Document
## Feature: Mindful Break Activity Coach

**Sprint:** 9
**Author:** UX Designer
**Date:** 2026-03-05

---

## 1. Design Goals

1. The Break Coach tab should feel calm and inviting — not clinical or gym-app aggressive.
2. Activities should be scannable in 2 seconds: emoji + title + duration is enough.
3. Category filters must be horizontally scrollable so all 6 chips fit without overflow on a 390px window.
4. The completion progress bar gives a daily sense of accomplishment without gamification pressure.
5. The Shuffle FAB is the "I don't want to decide" escape hatch — it must be prominent.

---

## 2. Layout Overview

The Break Coach tab occupies the full content area below the tab bar. It is divided into three vertical zones:

```
+------------------------------------------+
|  ZONE A: Daily Progress Header            |
|  [Progress bar]  "3 of 20 done today"    |
+------------------------------------------+
|  ZONE B: Category Filter Chips           |
|  [All] [Movement] [Eye Rest] [Mindfulness]|
|  [Hydration] [Creative]  <scrollable>    |
+------------------------------------------+
|  ZONE C: Activity Card List (LazyColumn) |
|                                          |
|  +--------------------------------------+|
|  | EMOJI  Title            [2 min]      ||
|  |        Short description...          ||
|  |                    [Done] [Skip]     ||
|  +--------------------------------------+|
|  +--------------------------------------+|
|  | EMOJI  Title (DONE)     [1 min]  [x]||
|  |        Short description... (muted) ||
|  |                          [Completed] ||
|  +--------------------------------------+|
|  ...                                     |
+------------------------------------------+
              [ SHUFFLE FAB ]
```

---

## 3. Component Specifications

### 3.1 Daily Progress Header (Zone A)

- **Background:** `MaterialTheme.colorScheme.surfaceVariant` with 8dp rounded corners.
- **Padding:** 12dp horizontal, 10dp vertical.
- **Elements (left to right, or stacked):**
  - Label text: `"Today's Break Activities"` — `titleSmall`, bold.
  - Subtitle: `"X of Y done today"` — `bodySmall`, secondary color.
  - `LinearProgressIndicator`: full width, progress = completed/total, height ~6dp with rounded ends.
- **Placement:** Sticky — does not scroll with the list. Sits between tab bar and filter chips.

### 3.2 Category Filter Chips (Zone B)

- **Component:** `FilterChip` (Material3).
- **Layout:** `LazyRow` with 8dp spacing, 16dp start/end padding.
- **Chips:** All | Movement | Eye Rest | Mindfulness | Hydration | Creative
- **Selected state:** Filled chip with `primary` background, white label.
- **Unselected state:** Outlined chip, default surface.
- **Behaviour:** Single-selection; tapping an active chip re-selects "All" is the default.

### 3.3 Activity Card (Zone C)

**Normal (uncompleted) state:**

```
+--------------------------------------------------+
|  [emoji]   Title                     [Xm Xs]    |
|            Description line 1                   |
|            Description line 2 (if long)         |
|                              [Done]  [Skip]      |
+--------------------------------------------------+
```

- Card: `ElevatedCard`, elevation 2dp, 8dp corner radius.
- Emoji: 32sp text in a 48x48dp Box, `surfaceContainerHigh` background, circular clip.
- Title: `titleMedium`, bold.
- Duration badge: `bodySmall` in a `SuggestionChip` (read-only). Formatted as "1m 30s" or "2m" etc.
- Description: `bodySmall`, max 2 lines, `onSurfaceVariant` color.
- Done button: `FilledTonalButton`, label "Done", `primary` tonal.
- Skip button: `TextButton`, label "Skip", muted color.

**Completed state:**

- Card background: `surfaceContainerLowest` (washed out).
- Emoji: alpha 0.5.
- Title: `onSurface` with `lineThrough` text decoration.
- Done button replaced by: `Icon(Icons.Default.CheckCircle)` + "Done" text in `secondary` color (no action).
- Skip button hidden.

### 3.4 Shuffle FAB

- **Component:** `FloatingActionButton` (standard size).
- **Icon:** `Icons.Default.Shuffle` (or equivalent).
- **Label:** None on the FAB itself; a `Text("Shuffle")` label can appear as `ExtendedFloatingActionButton` variant.
- **Position:** Bottom-end of the screen, 16dp margin. Sits inside a `Box` that wraps the entire `Scaffold` content.
- **Color:** `MaterialTheme.colorScheme.tertiaryContainer`.
- **Behaviour:** On click, picks a random uncompleted activity, scrolls `LazyListState` to its index, briefly highlights it with a `primary` border (animated for 1.5s).

### 3.5 Auto-Cycle Snackbar

- **Component:** `Snackbar` hosted by `SnackbarHost` in the Break Coach `Scaffold`.
- **Message:** "Break time! Try: [Activity Title] [emoji]"
- **Action label:** "Go" — scrolls to that activity in the list.
- **Duration:** `SnackbarDuration.Long` (auto-dismisses in ~10s).
- **Trigger:** Observed via `LaunchedEffect` on `currentPhase` state from `FocusTimerViewModel`.

---

## 4. Color Palette Usage (Material3)

| Element | Token |
|---------|-------|
| Selected filter chip | `primaryContainer` / `onPrimaryContainer` |
| Progress bar | `primary` track, `surfaceVariant` background |
| Done button | `secondaryContainer` / `onSecondaryContainer` |
| Completed card background | `surfaceContainerLowest` |
| Shuffle FAB | `tertiaryContainer` / `onTertiaryContainer` |
| Snackbar | Default `inverseSurface` |

---

## 5. Typography

| Role | Style |
|------|-------|
| Header title | `MaterialTheme.typography.titleSmall` bold |
| Progress subtitle | `MaterialTheme.typography.bodySmall` |
| Activity title | `MaterialTheme.typography.titleMedium` |
| Activity description | `MaterialTheme.typography.bodySmall` |
| Duration badge | `MaterialTheme.typography.labelSmall` |
| Filter chip label | `MaterialTheme.typography.labelMedium` |

---

## 6. ASCII Wireframe (Full Tab View)

```
+==========================================+
| [Timer]  [History]  [Break Coach]        |  <- PrimaryTabRow
+==========================================+
|                                          |
|  Today's Break Activities                |
|  3 of 20 done today                      |
|  [=========>                        ]    |  <- LinearProgressIndicator
|                                          |
+------------------------------------------+
|  [All] [Movement] [Eye Rest]             |
|  [Mindfulness] [Hydration] [Creative] -> |  <- FilterChip LazyRow
+------------------------------------------+
|                                          |
|  +--------------------------------------+|
|  |  [🔄]  Neck Roll           [1m]      ||
|  |        Gently roll your head in...   ||
|  |                     [Done]  [Skip]   ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  |  [👁]  20-20-20 Rule       [30s] ✓  ||  <- completed state
|  |        Look at something 20 ft...    ||
|  |                          [✓ Done]    ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  |  [🌬]  Box Breathing       [1m 30s]  ||
|  |        Inhale 4s, hold 4s, exhale... ||
|  |                     [Done]  [Skip]   ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  |  [💧]  Drink Water         [1m]      ||
|  |        Stand up, walk to kitchen...  ||
|  |                     [Done]  [Skip]   ||
|  +--------------------------------------+|
|                                          |
|  ...                        (scrollable) |
|                                          |
|                               [🔀 Shuffle] |  <- FAB bottom-end
+==========================================+

  +-----------------------------------------+
  |  Break time! Try: Box Breathing 🌬   [Go]|  <- Snackbar (bonus)
  +-----------------------------------------+
```

---

## 7. Interaction Flow

```
User opens Break Coach tab
         |
         v
 Default: All activities shown, filter = All
         |
         +---> Tap filter chip --> List re-filters
         |
         +---> Tap [Done] --> Card enters completed state
         |                    Counter increments
         |                    Preference persisted
         |
         +---> Tap [Skip] --> Card moves to bottom of list
         |                    (visual deprioritization only)
         |
         +---> Tap [Shuffle FAB] --> Random uncompleted picked
                                     List scrolls to card
                                     Card briefly highlighted
```

---

## 8. Responsive Considerations

- Window width is fixed at 390px. All layouts must work within this constraint.
- Category filter chips must be in a horizontally scrollable row (no wrapping).
- Activity card descriptions are capped at 2 lines with ellipsis overflow.
- The Shuffle FAB uses a standard (non-extended) FAB on the 390px window to avoid overlapping text.
