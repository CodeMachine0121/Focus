# Sprint 12 – Smart Focus Scheduling
## Stakeholder Brief

### User Problem
Knowledge workers plan their day ahead of time but forget to actually start focus sessions.
They want to block "focus time" on their mental calendar (e.g. "2 pm – report writing, 45 min"),
have the app remind them at exactly that moment via a desktop notification, and be able to jump
straight into the session with one click — without hunting for the app window.

### Feature: Smart Focus Scheduling
A scheduler built directly into the Focus app that lets users:

1. **Add a scheduled focus block** — pick a time (HH:MM), write a label, choose duration, and
   optionally make it recurring (daily or weekdays-only).
2. **View upcoming sessions** — a sorted list of all planned blocks so the day's focus plan is
   visible at a glance.
3. **Receive a desktop notification** — at the scheduled minute the app fires a system tray
   notification: "⏱ Focus Time! — Time to start: Report Writing (45 min)".
4. **Recurring sessions** — DAILY and WEEKDAYS sessions re-trigger every applicable day without
   the user having to re-enter them.
5. **Delete or deactivate** any scheduled entry directly from the list.

### Story Points
**8** (Fibonacci) — background scheduler + persistence + notification integration + new screen.

### Key Acceptance Needs
| # | Need |
|---|------|
| 1 | Add a focus block with label, HH:MM time, duration (5–120 min), and recurring type |
| 2 | Scheduled list is sorted by time and persists across app restarts |
| 3 | Desktop notification fires within 1 minute of the scheduled time |
| 4 | ONCE sessions are removed after firing; DAILY/WEEKDAYS sessions remain active |
| 5 | Delete button removes a session immediately |
| 6 | Blank label is rejected with no crash |
| 7 | Notification is silently skipped if SystemTray is unavailable |
