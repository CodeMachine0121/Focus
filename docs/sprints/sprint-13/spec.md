# Sprint 13 — Product Specification
## Deep Focus Mode

### User Stories

**US-1** As a knowledge worker, I want to activate Deep Focus Mode so that macOS
stops sending me notifications during my focus session.

**US-2** As a user sensitive to visual distraction, I want the screen to
automatically dim when I enter deep focus so that the display is less stimulating.

**US-3** As a user who gets distracted by specific apps, I want to configure a
list of distracting applications so that the app warns me when they are running
during a focus session.

**US-4** As a user, I want all system changes to be automatically reversed when my
session ends so that I don't have to remember to restore my settings manually.

**US-5** As a user, I want my Deep Focus settings (DND preference, dim level,
blocked apps) to persist across app restarts.

---

### Data Model

```kotlin
data class DeepFocusSettings(
    val isDndEnabled: Boolean = true,       // Enable macOS Do Not Disturb
    val isDimEnabled: Boolean = true,       // Enable display dimming
    val dimLevel: Float = 0.3f,            // Target brightness 0.0–1.0
    val blockedApps: List<String> = listOf("Safari", "Chrome", "Slack", "Discord")
)
```

Persistence: `java.util.prefs.Preferences` under `DeepFocusManager` class node.

---

### Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| AC-1 | Deep Focus screen accessible via 🔕 nav tab | Must |
| AC-2 | Toggle button activates/deactivates DFM | Must |
| AC-3 | DND command executes on activation when `isDndEnabled = true` | Must |
| AC-4 | Brightness command executes on activation when `isDimEnabled = true` | Must |
| AC-5 | Original brightness is captured before dimming and restored on deactivate | Must |
| AC-6 | Settings controls are disabled while DFM is active | Must |
| AC-7 | Blocked apps list persists between app restarts | Must |
| AC-8 | Any `ProcessBuilder`/`Runtime.exec` failure is caught; app does not crash | Must |
| AC-9 | Slider range 10%–80% prevents user from blacking out the screen entirely | Should |
| AC-10 | Info banner explains what DFM does before first activation | Should |

### Graceful Degradation
If `osascript`, `brightness`, or `pgrep` are unavailable or return non-zero:
- Catch the exception, log to stdout with `[DeepFocus]` prefix.
- Continue activating remaining features (partial activation is acceptable).
- UI shows "Active" regardless — user experience > perfect system control.
