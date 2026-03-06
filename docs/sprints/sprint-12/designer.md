# Sprint 12 – Design Document
## Smart Focus Scheduling · ScheduleScreen

### Color Theme
| Token | Value | Usage |
|-------|-------|-------|
| Primary | `#00796B` (Teal 700) | Buttons, badges, highlights |
| Secondary | `#B2DFDB` (Teal 100) | Card backgrounds |
| Accent | `#004D40` (Teal 900) | Delete icon, dark text |

Teal distinguishes the Schedule screen from Timer (blue), Goals (amber), and Sounds (purple).

---

### Screen Layout: ScheduleScreen

```
┌─────────────────────────────────────────────────────┐
│  📅  Smart Focus Scheduling                   [h1]   │
├─────────────────────────────────────────────────────┤
│ ┌─── Add Schedule ──────────────────────────────┐   │
│ │  Label: [___________________________]          │   │
│ │                                                │   │
│ │  Time:  [−] 09 : 00 [+]   Duration: [−] 25 [+]│   │
│ │                                                │   │
│ │  Repeat: [ Once ▾ ]                            │   │
│ │                                                │   │
│ │                         [  Add Schedule  ]     │   │
│ └────────────────────────────────────────────────┘   │
│                                                       │
│  Upcoming Sessions                            [h2]   │
│ ┌────────────────────────────────────────────────┐   │
│ │ ┌──────┐  Report Writing          [Daily]  🗑  │   │
│ │ │ 09:00│                                       │   │
│ │ └──────┘                                       │   │
│ ├────────────────────────────────────────────────┤   │
│ │ ┌──────┐  Team Sync               [Once ]  🗑  │   │
│ │ │ 14:30│                                       │   │
│ │ └──────┘                                       │   │
│ └────────────────────────────────────────────────┘   │
│                                                       │
│   (empty state)                                       │
│     📅                                               │
│   No scheduled sessions yet.                         │
│   Add one above to get started!                      │
└─────────────────────────────────────────────────────┘
```

---

### Component Details

#### Add Schedule Card
- **OutlinedTextField** — label, single line, max 60 chars
- **Time Row** — `[−]` Text button · `HH:MM` monospace display · `[+]` Text button (hour),
  same pattern for minutes
- **Duration Row** — `[−]` · value + " min" · `[+]`
- **Repeat Dropdown** — ExposedDropdownMenuBox listing Once / Daily / Weekdays
- **Add Schedule Button** — full-width, teal, disabled when label is blank

#### Session Card
- Left: teal rounded box showing `HH:MM` in bold monospace
- Center: label (bold), recurrence chip (small teal label)
- Right: 🗑 IconButton (removes session)

#### Empty State
- Centered vertically in the list area
- Large 📅 emoji (48 sp)
- "No scheduled sessions yet." (16 sp, muted)
- "Add one above to get started!" (13 sp, muted italic)
