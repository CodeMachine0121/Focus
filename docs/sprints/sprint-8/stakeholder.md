# Sprint 8 — Stakeholder Brief
## Feature: Productivity Dashboard & Streak Tracker

---

## User Problem

Users of the Focus app complete Pomodoro sessions daily, but the existing History tab only shows a raw chronological list of completed sessions with a basic "today total" summary. There is no mechanism to:

- Understand long-term habits and trends (am I being consistent this week vs last week?)
- See streaks — the most motivating gamification element in productivity apps
- Identify high-output days vs low-output days at a glance
- Feel a sense of progression and reward for sustained focus effort

Without these insights, users have no feedback loop to reinforce the habit of daily focused work. The app records the data but does not turn it into actionable visibility.

---

## Proposed Feature

**Productivity Dashboard & Streak Tracker** — a dedicated "Dashboard" tab that transforms raw session records into a rich visual analytics view. It reads from the same underlying data source (SessionRepository) as the History tab but presents a completely different experience: gamified streaks, visual charts, and personal records.

### Core Components

1. **Streak Tracker** — Current daily streak (consecutive days with at least one session) and all-time longest streak. Prominently displayed to encourage daily habit maintenance.

2. **Summary Stats Cards** — Total sessions all-time and total focus minutes all-time, displayed as a quick-glance stat bar.

3. **Weekly Bar Chart** — Canvas-rendered bar chart showing minutes focused each day of the current week (Mon–Sun). Gives users an at-a-glance view of this week's distribution.

4. **30-Day Heatmap** — A GitHub-contribution-style grid showing session density per calendar day over the last 30 days, colored by intensity (no sessions → light green, many sessions → dark green). Reveals patterns of consistency.

5. **Personal Records** — Longest single session ever, and best single day total. Celebrates achievements.

6. **Empty State** — Graceful empty state when no session history exists yet.

---

## Business Value

- Increases daily retention: streak mechanics are proven to drive return-visit behavior (Duolingo, Habitica, GitHub).
- Differentiates the app from basic timer utilities.
- Turns passive data collection (session logging) into active motivation.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC-1 | A "Dashboard" tab appears in the app navigation alongside Timer and History |
| AC-2 | Current streak shows the number of consecutive days (including today) with at least 1 completed session; resets to 0 if today has no session yet after yesterday also had none |
| AC-3 | Longest streak shows the highest historical consecutive-day count ever reached |
| AC-4 | Total sessions count matches the number of records in SessionRepository |
| AC-5 | Total minutes is the sum of all session durationMinutes values |
| AC-6 | Weekly bar chart renders bars for Mon–Sun of the current ISO week, proportional to minutes focused |
| AC-7 | 30-day heatmap renders a 5-row × 6-column grid covering the last 30 calendar days, colored by session count intensity |
| AC-8 | Personal records show the single session with the longest duration, and the calendar day with the most total minutes |
| AC-9 | Empty state is shown with an encouraging message when no sessions have been recorded |
| AC-10 | Dashboard reads data from SessionRepository.sessions (reactive) and updates when new sessions are added |

---

## Story Points

**Fibonacci Complexity: 8**

Rationale: The feature involves non-trivial date arithmetic (streak calculation, ISO week grouping, 30-day windowing), Canvas-rendered visualizations (bar chart + heatmap), a new ViewModel, and a full Compose screen — but builds on existing infrastructure (SessionRepository, SessionRecord) without requiring backend changes or new storage.
