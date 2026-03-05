# Sprint 2 Stakeholder Requirements

## User Problem

Users who rely on Pomodoro-style focus sessions have no way to review how many sessions they completed, how long they focused, or whether they are building consistent habits over time. Without this data, the app functions as a one-shot timer rather than a productivity tool.

## Proposed Feature: Session History Log

After each focus session completes (the countdown reaches zero and the user dismisses the dialog), the app records a session entry — capturing the date, time, and duration. A dedicated "History" tab within the app shows the list of past sessions and a simple daily summary (total focused minutes for today). Session records are persisted locally using Java Preferences so they survive app restarts.

## Acceptance Criteria

- When a focus session completes and the user dismisses the completion dialog, a record containing the session date, start time, and duration in minutes is saved to persistent local storage.
- The app displays a "History" tab (or panel) alongside the existing timer UI, showing a scrollable list of completed sessions with human-readable timestamps and durations.
- The history view shows a "Today" summary row at the top indicating the total focused minutes accumulated today.
- Session history persists across app restarts — closing and reopening the app retains previously completed sessions.
- If no sessions have been completed yet, the history view shows an appropriate empty-state message rather than a blank screen.

## Out of Scope

- Editing or deleting individual session records.
- Weekly/monthly aggregate charts or graphs.
- Export to CSV or any external format.
- Sync across devices or any network feature.
- Tracking interrupted sessions (paused then reset without completing).
- Push notifications or reminders based on session history.
- Configurable history retention window (e.g., "only keep last 30 days").
