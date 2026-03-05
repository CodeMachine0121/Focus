# Sprint 8 — Designer Brief
## Feature: Productivity Dashboard & Streak Tracker

---

## Design Principles

- **Information hierarchy**: Most motivating stats (streak) are at the top, supporting detail (heatmap) below.
- **Glanceable**: All key numbers visible without scrolling on a 390×780 window.
- **Material 3**: Uses ElevatedCard, surface colors, and M3 typography scale. No custom design tokens.
- **Canvas-first charts**: Bar chart and heatmap are drawn with Compose Canvas for pixel-perfect control without external chart libraries.
- **Accessible empty state**: First-time users see an encouraging message, not blank space or zero-filled charts.

---

## Layout Structure

The screen is a vertically scrollable `Column` with the following sections top to bottom:

```
┌──────────────────────────────────────────────┐
│  Dashboard                          [title]  │
├──────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  🔥 3    │  │ 123 ses  │  │ 3,050 min│   │
│  │ day      │  │  total   │  │  total   │   │
│  │ streak   │  │ sessions │  │ minutes  │   │
│  └──────────┘  └──────────┘  └──────────┘   │
├──────────────────────────────────────────────┤
│  This Week                                   │
│  ┌────────────────────────────────────────┐  │
│  │       █                               │  │
│  │    █  █     █                         │  │
│  │    █  █  █  █  █                      │  │
│  │ M  T  W  T  F  S  S                   │  │
│  └────────────────────────────────────────┘  │
├──────────────────────────────────────────────┤
│  Last 30 Days                                │
│  ┌────────────────────────────────────────┐  │
│  │ ░ ░ ▒ ░ ░ ▓    (row 1: days 1-6)     │  │
│  │ ░ ▒ ░ ░ ░ ▒    (row 2: days 7-12)    │  │
│  │ ▓ ░ ░ ▒ ░ ░    (row 3: days 13-18)   │  │
│  │ ░ ░ ▒ ░ ▒ ░    (row 4: days 19-24)   │  │
│  │ ░ ▒ ░ ░ ░ ▓    (row 5: days 25-30)   │  │
│  └────────────────────────────────────────┘  │
├──────────────────────────────────────────────┤
│  Personal Records                            │
│  ┌──────────────────┐ ┌──────────────────┐   │
│  │ Longest Session  │ │ Best Day         │   │
│  │ 50 min           │ │ 75 min           │   │
│  │                  │ │ 2026-03-02       │   │
│  └──────────────────┘ └──────────────────┘   │
└──────────────────────────────────────────────┘
```

---

## Component Specs

### 1. Screen Header

- `Text` with "Dashboard", `fontSize = 22.sp`, `fontWeight = Bold`
- `padding(bottom = 16.dp)`

---

### 2. Stats Cards Row

- `Row` with `Arrangement.spacedBy(8.dp)`, `fillMaxWidth`
- Three `ElevatedCard` items, each with `weight(1f)`
- Each card:
  - `padding(12.dp)` inside
  - Value: `fontSize = 24.sp`, `fontWeight = Bold`, `color = primary`
  - Label: `fontSize = 11.sp`, `color = onSurfaceVariant`
  - Streak card label: "Current Streak"; value: "N days" (or "0 days")
  - Sessions card label: "Total Sessions"; value: count as string
  - Minutes card label: "Total Minutes"; value: formatted with commas if > 999

**Streak card special treatment:**
- If `currentStreak >= 3`: value text is prefixed with a fire indicator color (`tertiaryContainer` background tint)
- If `currentStreak == 0`: value shown in `onSurfaceVariant` color

---

### 3. Weekly Bar Chart

**Container:** `ElevatedCard`, `fillMaxWidth`, `height = 160.dp` (canvas area) + 24.dp label row

**Canvas drawing (Compose Canvas):**
- Available width divided into 7 equal columns
- Bar width = `columnWidth * 0.6f`, centered within column
- Max bar height = canvas height - 8.dp top padding
- Bar height = `(minutes / maxMinutes.toFloat()) * maxBarHeight` (if maxMinutes == 0, all zero height)
- Bar color: today's bar uses `primary`, other bars use `primaryContainer`
- Corner radius on bars: `4.dp`
- Day labels ("Mon", "Tue", ...) drawn below the canvas as a `Row` of `Text` items, `fontSize = 10.sp`, centered per column
- Minute value drawn above each bar if > 0: `fontSize = 9.sp`, centered

**Zero state:** All bars rendered at height 1.dp (minimum visible line) so the chart space isn't confusingly blank.

---

### 4. 30-Day Heatmap

**Container:** `ElevatedCard`, `fillMaxWidth`

**Layout:**
- 5 rows × 6 columns grid = 30 cells (days laid out oldest → newest, left-to-right, top-to-bottom)
- Cell size: `(availableWidth - 2*cardPadding - 5*gap) / 6`, square
- Gap between cells: `4.dp`

**Color intensity levels (M3 green scale using `surfaceVariant` → `primary`):**
| sessionCount | Background Color |
|---|---|
| 0 | `surfaceVariant` (very light) |
| 1 | `primary.copy(alpha = 0.25f)` |
| 2 | `primary.copy(alpha = 0.50f)` |
| 3 | `primary.copy(alpha = 0.75f)` |
| 4+ | `primary` (full intensity) |

- Cells drawn as rounded rectangles (cornerRadius = `4.dp`)
- No text inside cells (too small); tooltip on hover is out of scope
- Row of abbreviated date labels shown on left side for context: just the first cell's date per row formatted as "Feb 4" style — abbreviated month + day

**Today's cell:** drawn with a thin border (`1.dp`, color = `outline`) in addition to intensity fill.

---

### 5. Personal Records

**Container:** `Row` of two `ElevatedCard` items, `weight(1f)` each, `Arrangement.spacedBy(8.dp)`

- Each card has a title label (`fontSize = 12.sp`, `onSurfaceVariant`) and a value (`fontSize = 20.sp`, `Bold`, `primary`)
- "Longest Session" card: value = "N min" or "--" if no data
- "Best Day" card: value = "N min" + sub-label of the date in `fontSize = 11.sp`

---

### 6. Empty State

Shown **instead of all sections** (cards, charts, records) when `sessions.isEmpty()`.

```
┌──────────────────────────────────────────────┐
│                                              │
│           (no icon, just text)               │
│                                              │
│        No focus sessions yet.                │
│   Complete your first Pomodoro to start      │
│       tracking your productivity!            │
│                                              │
└──────────────────────────────────────────────┘
```

- `Box` with `fillMaxSize`, `contentAlignment = Center`
- Primary text: `fontSize = 16.sp`, `onSurfaceVariant`, centered
- Secondary text: `fontSize = 14.sp`, `onSurfaceVariant.copy(alpha = 0.7f)`, centered
- No icon (keeps complexity low; icon requires resource management)

---

## Color Usage Summary

| Element | M3 Token |
|---|---|
| Screen background | `surface` (default) |
| Card background | `ElevatedCard` default (`surfaceContainerLow`) |
| Stat values | `primary` |
| Stat labels | `onSurfaceVariant` |
| Today's bar | `primary` |
| Other week bars | `primaryContainer` |
| Heatmap 0 sessions | `surfaceVariant` |
| Heatmap high sessions | `primary` |
| Today heatmap border | `outline` |
| Empty state text | `onSurfaceVariant` |

---

## Typography Summary

| Element | Size | Weight |
|---|---|---|
| Screen title | 22.sp | Bold |
| Stat value | 24.sp | Bold |
| Stat label | 11.sp | Normal |
| Chart day label | 10.sp | Normal |
| Chart bar value | 9.sp | Normal |
| Record value | 20.sp | Bold |
| Record label | 12.sp | Normal |
| Empty state primary | 16.sp | Normal |

---

## Scrollability

The Dashboard is wrapped in a `verticalScroll` modifier to handle window resizing or content overflow gracefully.

---

## Spacing & Padding

- Screen padding: `16.dp` horizontal, `16.dp` top
- Section spacing: `16.dp` between sections
- Card internal padding: `12.dp`
- Chart section title to chart: `8.dp`
