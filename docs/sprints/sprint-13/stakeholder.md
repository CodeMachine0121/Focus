# Sprint 13 — Stakeholder Brief
## Deep Focus Mode: macOS System Integration

### User Problem
Even with the Focus timer running, macOS notifications, other apps, and browser tabs
keep pulling the user's attention away. The timer tells you *to* focus, but the OS
keeps fighting back. Users report that they still get Slack pings, emails pop up,
and YouTube is one ⌘-Tab away. The app must actively collaborate with macOS to
create a genuinely distraction-free environment.

### Proposed Feature: Deep Focus Mode
A single toggle that, when activated during a focus session, reaches into macOS and:

1. **Enables macOS Do Not Disturb (Focus mode)** via `osascript` / system defaults
   so zero notifications interrupt the session.
2. **Dims the display** to a user-configured level via the `brightness` CLI or
   `osascript`, reducing visual stimulation and saving battery.
3. **Monitors "blocked" apps** — the user configures a list of distracting
   applications (e.g., Safari, Chrome, Slack, Discord). When DFM activates,
   the app checks if any are running and logs/warns about them.
4. **Restores all settings** automatically when the session ends or the user
   manually deactivates, so there's no manual cleanup required.

### Story Points
**13** (Fibonacci) — significant macOS API surface, multiple integration points,
graceful degradation requirements.

### Success Criteria
- [ ] User can toggle Deep Focus Mode on/off from a dedicated screen.
- [ ] When activated, macOS Do Not Disturb is programmatically enabled.
- [ ] Display brightness is reduced to the configured dim level.
- [ ] When deactivated (or session ends), DND is disabled and brightness restored.
- [ ] Blocked-apps list is configurable and persisted across app restarts.
- [ ] If any system command fails (permissions, missing CLI), the app logs the error
      and continues — it does NOT crash.
- [ ] All settings are editable only when DFM is inactive.
