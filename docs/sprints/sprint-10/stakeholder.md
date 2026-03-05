# Stakeholder Requirements: Multi-Timer Workspace

## Sprint 10 | Story Points: 13 (Fibonacci)

---

## User Problem

Power users of the Focus app frequently manage multiple concurrent time-bounded tasks within a single work session. The existing app supports only a single Pomodoro timer, forcing users to either juggle mental time-tracking for parallel workstreams or switch between multiple external timer apps.

Concrete scenarios where this limitation causes friction:

- A software engineer running a 45-minute build alongside a 25-minute review window and a 10-minute stand-up countdown simultaneously.
- A chef or home cook tracking four separate dishes with different finish times.
- A team facilitator running parallel breakout sessions of different lengths.
- A student managing parallel timed exercises across subjects.

In all of these cases, the user needs to see, at a glance, how much time remains on each task without leaving the app or opening separate windows.

---

## Proposed Feature

A new **Workspace** tab added to the existing Focus app that presents a grid of up to **6 independent mini-timers**. Each timer is self-contained with its own label, duration, countdown, and controls. Timers run fully in parallel — a user can have three running, one paused, and one completed all at the same time.

The workspace is session-only (not persisted to disk). It is entirely separate from the existing Pomodoro timer — it does not share state, sound logic, or view models with `FocusTimerViewModel`.

---

## Acceptance Criteria

1. A "Workspace" tab is accessible from the main navigation alongside "Timer" and "History".
2. The workspace displays existing timers in a 2-column grid layout.
3. A user can add a new timer via a dialog that accepts a label and a duration (in minutes).
4. Each timer card shows: label, remaining time as MM:SS, a circular progress arc, and Start / Pause / Reset / Remove controls.
5. Each timer operates independently — starting, pausing, or completing one timer has no effect on any other timer.
6. When a timer reaches zero, it plays a short 2-note beep (distinct from the 3-note Pomodoro chime) and displays a visual completion badge on the card.
7. The workspace enforces a maximum of 6 timers; the "Add Timer" control is disabled or hidden when the limit is reached.
8. A timer can be removed at any time regardless of its state; removal cancels the underlying coroutine immediately.
9. Timer state is not persisted — closing and reopening the app resets the workspace to empty.
10. The feature does not modify any existing files (`App.kt`, `FocusTimerViewModel.kt`, `FocusScreen.kt`, etc.).

---

## Out of Scope

- Persisting workspace timer state to disk (future sprint).
- More than 6 simultaneous timers.
- Nested or hierarchical timers.
- Sound configuration or volume control per timer.
- Integration with session history logging.
- Auto-cycle mode for workspace timers.
- Drag-to-reorder timer cards.
