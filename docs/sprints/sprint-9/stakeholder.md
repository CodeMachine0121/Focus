# Sprint 9 — Stakeholder Document
## Feature: Mindful Break Activity Coach

**Sprint:** 9
**Story Points:** 8 (Fibonacci)
**Date:** 2026-03-05
**Stakeholder:** Product Owner / End-User Representative

---

## 1. User Problem Statement

Knowledge workers using a Pomodoro timer typically understand the importance of taking breaks, but they lack guidance on *what to do* during those 5-minute intervals. The result:

- Users scroll social media or check email during breaks, which does not provide genuine cognitive rest.
- Prolonged desk work without physical micro-activity leads to neck tension, eye strain, and accumulated fatigue across an 8-hour workday.
- Many users feel vaguely guilty about unstructured break time and restart focus sessions without having actually recovered.
- There is no feedback loop: users cannot tell whether their break habits are improving over time.

This creates a cycle where the Pomodoro technique is applied at the session level, but the *quality* of breaks is left entirely to chance. The timer app currently has no opinion about how breaks should be spent.

---

## 2. Proposed Feature

**Mindful Break Activity Coach** — a curated, interactive guide embedded directly in the Focus app that suggests short, evidence-informed activities for every break.

The coach presents:
- A library of 20+ activities organized into five categories: Movement (desk exercises), Eye Rest, Mindfulness (breathing, meditation), Hydration/Nutrition, and Creative (journaling, sketching prompts).
- A dedicated "Break Coach" tab visible at all times in the app.
- A daily recommended activity sequence and the ability to mark each activity as done.
- A "Shuffle" button to surface a random activity when the user wants variety.
- A lightweight daily completion tracker showing how many break activities were completed today.
- A non-blocking banner notification when the Auto-Cycle timer enters Break phase, suggesting a specific activity — without interrupting or altering the timer in any way.

The Break Coach is a standalone system. It does not modify the timer, the session history, or any other existing feature.

---

## 3. Target Users

- Knowledge workers who already use the Pomodoro technique but want more structured, health-conscious breaks.
- Users who experience physical discomfort (eye strain, neck pain, back tension) from extended desk work.
- Users who want a lightweight accountability mechanism for their break habits.

---

## 4. Business Value

- Differentiates the Focus app from generic Pomodoro timers by addressing the full work-rest cycle.
- Increases daily active usage: users return to the Break Coach tab throughout the day.
- Provides a foundation for future personalization features (activity preferences, streaks, custom activities).

---

## 5. Acceptance Criteria

| # | Criterion | Priority |
|---|-----------|----------|
| AC-1 | A "Break Coach" tab appears in the app alongside Timer and History. | Must Have |
| AC-2 | The Break Coach displays a list of activities filtered by category. "All" shows every activity. | Must Have |
| AC-3 | Each activity card shows an emoji icon, title, description, and estimated duration. | Must Have |
| AC-4 | The user can tap "Done" on an activity to mark it as completed for today. Done activities show a visual completed state. | Must Have |
| AC-5 | The user can tap "Shuffle" to randomly select and highlight one activity. | Must Have |
| AC-6 | A daily progress indicator shows how many activities have been completed today (e.g., "3 / 8 done"). | Must Have |
| AC-7 | Today's completion count persists across app restarts (survives JVM exit and relaunch on the same calendar day). | Must Have |
| AC-8 | When the Auto-Cycle timer transitions into Break phase, a non-blocking snackbar/banner suggests a specific activity. The timer is not paused or affected in any way. | Should Have |
| AC-9 | The activity library contains at least 20 distinct activities distributed across all 5 categories. | Must Have |
| AC-10 | Completed activity IDs reset at midnight (next calendar day they start fresh). | Must Have |
| AC-11 | The Break Coach feature is entirely read-only with respect to the timer — it never calls start(), pause(), reset(), or dismiss(). | Must Have |

---

## 6. Out of Scope (Sprint 9)

- Custom user-defined activities.
- Activity streaks / weekly statistics.
- Push notifications or OS-level alerts.
- Sound playback for guided breathing.
- Syncing activity data to a remote server.
