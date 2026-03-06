# Sprint 14 — Design Spec: ExportScreen

## Layout

```
┌─────────────────────────────────────────────────────┐
│  📤 Export & Reports                                 │
│                                                     │
│ ┌─ Period Selector ──────────────────────────────┐  │
│ │  [Last 7 days] [Last 30 days] [Last 90 days]   │  │
│ │  [All time]                                     │  │
│ └────────────────────────────────────────────────┘  │
│                                                     │
│ ┌─ Report Preview ───────────────────────────────┐  │
│ │  📊 Report Preview                              │  │
│ │                                                 │  │
│ │  [Sessions: 42]  [Hours: 12.5]  [Avg: 25 min] │  │
│ │  [Best Day: 90 min]                             │  │
│ │                                                 │  │
│ │ ┌─ Monospace Terminal Block ─────────────────┐ │  │
│ │ │ ═══════════════════════════════════        │ │  │
│ │ │  FOCUS PRODUCTIVITY REPORT                 │ │  │
│ │ │ ═══════════════════════════════════        │ │  │
│ │ │ Period: Jan 01 → Jan 30                    │ │  │
│ │ │ ─────────────────────────────              │ │  │
│ │ │ Total Sessions  : 42                       │ │  │
│ │ │ Total Focus Time: 12.5 hours (750 min)     │ │  │
│ │ │ Avg Session     : 18 min                   │ │  │
│ │ │ Best Day        : 2024-01-15 (90 min)      │ │  │
│ │ │ ─────────────────────────────              │ │  │
│ │ │ DAILY BREAKDOWN:                           │ │  │
│ │ │   2024-01-01  ███ 30m                      │ │  │
│ │ │   2024-01-15  █████████ 90m                │ │  │
│ │ └────────────────────────────────────────────┘ │  │
│ └────────────────────────────────────────────────┘  │
│                                                     │
│  [💾 Export CSV]          [📋 Copy Report]          │
│                                                     │
│  ✅ CSV exported to focus-report.csv                │
└─────────────────────────────────────────────────────┘
```

## Design Tokens
- **Card background**: white with Material3 Card elevation
- **Accent blue**: `#1976D2` (stat values)
- **Terminal block bg**: `#1E1E1E` (dark)
- **Terminal text**: `#00FF41` (Matrix green, monospace)
- **Success message**: `#4CAF50`
- **Error message**: Red
- **Period chips**: Material3 `FilterChip`, selected = blue fill

## Typography
- Title: Bold, 22sp
- Section labels: SemiBold, 16sp
- Stat values: Bold, 18sp
- Stat labels: Gray, 11sp
- Report text: Monospace, 11sp

## Interactions
- Period chip click → immediately regenerates report
- Export CSV → opens native OS save dialog
- Copy Report → writes to clipboard, shows status message below buttons
- Status message auto-visible when non-empty
