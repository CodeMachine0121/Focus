# Sprint 13 — Design Specification
## Deep Focus Mode Screen

### Design Philosophy
Dark, minimal, serious. This screen should *feel* focused. When DFM is Active,
the UI shifts to a deep red/orange palette to signal "you're in the zone — don't
touch anything." When Inactive, a neutral slate-blue communicates calm readiness.

---

### Screen Layout: `DeepFocusScreen`

```
┌─────────────────────────────────────────┐
│         🔕 Deep Focus Mode              │  ← Header card, full-width
│     ● ACTIVE  /  ○ INACTIVE             │    Active: #E53935 (deep red)
│    [ Activate Deep Focus ] button       │    Inactive: #546E7A (slate)
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ⚡ When active: enables macOS Do Not    │  ← Info banner, warm amber #FFF3E0
│   Disturb, dims your display, and warns │
│   you about distracting apps.           │
└─────────────────────────────────────────┘

⚙️ Settings  (greyed out when Active)
┌─────────────────────────────────────────┐
│ Enable Do Not Disturb      [ Toggle ]   │
│ Silences macOS notifications            │
│ ─────────────────────────────────────── │
│ Dim Display                [ Toggle ]   │
│ Reduces screen brightness               │
│                                         │
│ Dim Level: 30%                          │
│ ████░░░░░░░░░░░░  [slider 10%–80%]     │
└─────────────────────────────────────────┘

🚫 Distracting Apps
┌─────────────────────┐ ┌───────┐
│ App name            │ │  Add  │
└─────────────────────┘ └───────┘
• Safari                         [Remove]
• Chrome                         [Remove]
• Slack                          [Remove]
• Discord                        [Remove]
```

---

### Color Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `activeRed` | `#E53935` | Header card when Active |
| `inactiveSlate` | `#546E7A` | Header card when Inactive |
| `pulseGreen` | `#69F0AE` | Status indicator dot when Active |
| `warmAmber` | `#FFF3E0` | Info banner background |
| `removeRed` | `Color.Red` | Remove button text |

### Status Indicator
A 12dp filled circle (CircleShape):
- **Active**: `#69F0AE` (bright green) — "system is engaged"
- **Inactive**: `White @ 50% opacity` — "standing by"

### Interaction Rules
- All settings controls (`Switch`, `Slider`, `TextField`, `Button`) are
  `enabled = !isActive` to prevent mid-session config changes.
- Header card color transitions instantly on toggle (no animation needed in v1).
- Blocked apps list is scrollable via `LazyColumn`.

### Typography
- Screen title: 22sp Bold, white
- Section headers: 18sp Bold
- Setting labels: 14sp SemiBold
- Descriptions: 12sp, `Color.Gray`
- Info banner: 13sp
