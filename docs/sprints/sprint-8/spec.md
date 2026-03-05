# Sprint 8 — Product Spec
## Feature: Productivity Dashboard & Streak Tracker

---

## Overview

The Dashboard is a read-only analytics view over the user's session history. It presents statistics, visual charts, and personal records computed from `SessionRepository.sessions`. It is accessed via a new "Dashboard" tab in `App.kt`.

---

## Data Definitions

### SessionRecord (existing)
```
date: String          // "yyyy-MM-dd"
startTime: String     // "HH:mm:ss"
durationMinutes: Int  // > 0
```

### DayData
```
date: LocalDate
totalMinutes: Int     // sum of all session durations that day
sessionCount: Int     // number of sessions that day
```

### WeeklyBarData
```
dayLabel: String      // "Mon", "Tue", ..., "Sun"
minutes: Int          // total focus minutes for that day
isToday: Boolean      // whether this bar represents today
```

### HeatmapCell
```
date: LocalDate
sessionCount: Int     // 0..N, used to determine color intensity
```

### PersonalRecords
```
longestSessionMinutes: Int   // longest single session duration ever
bestDayMinutes: Int          // highest total minutes in a single calendar day
bestDayDate: String?         // "yyyy-MM-dd" of that day, null if no data
```

### DashboardStats
```
currentStreak: Int
longestStreak: Int
totalSessions: Int
totalMinutes: Int
weeklyData: List<WeeklyBarData>   // 7 items, Mon–Sun of current ISO week
heatmapData: List<HeatmapCell>    // 30 items, last 30 calendar days
personalRecords: PersonalRecords
```

---

## Streak Algorithm Definition

A **streak** is a sequence of consecutive calendar days each containing at least one completed session.

- **Current streak**: Count backwards from today. If today has at least one session, count it. Then count yesterday, the day before, etc., stopping at the first day with no session. If today has no session but yesterday does, the streak is still the count of consecutive days ending yesterday (the streak is "at risk" today but not broken yet).
- **Longest streak**: Iterate over all days with sessions in chronological order and find the maximum run of consecutive days.

Edge cases:
- No sessions: both streaks = 0.
- All sessions on the same day: current = 1, longest = 1.
- Gap of 2+ days between any sessions breaks the streak.

---

## Gherkin Scenarios

### Feature: Streak Calculation

```gherkin
Feature: Daily streak calculation

  Background:
    Given the current date is "2026-03-05"

  Scenario: No sessions recorded
    Given there are no session records
    When I view the Dashboard
    Then current streak is 0
    And longest streak is 0

  Scenario: Single session today
    Given there is a session on "2026-03-05"
    When I view the Dashboard
    Then current streak is 1
    And longest streak is 1

  Scenario: Sessions on consecutive days including today
    Given there are sessions on "2026-03-03", "2026-03-04", "2026-03-05"
    When I view the Dashboard
    Then current streak is 3
    And longest streak is 3

  Scenario: Sessions on consecutive days but not today
    Given there are sessions on "2026-03-03", "2026-03-04"
    And there is no session on "2026-03-05"
    When I view the Dashboard
    Then current streak is 2
    And longest streak is 2

  Scenario: Gap breaks streak
    Given there are sessions on "2026-03-01", "2026-03-03", "2026-03-04", "2026-03-05"
    When I view the Dashboard
    Then current streak is 3
    And longest streak is 3

  Scenario: Longest streak is in the past
    Given there are sessions on "2026-02-10", "2026-02-11", "2026-02-12", "2026-02-13"
    And there is a session on "2026-03-05"
    When I view the Dashboard
    Then current streak is 1
    And longest streak is 4

  Scenario: Multiple sessions on the same day count as one streak day
    Given there are 3 sessions on "2026-03-04"
    And there is 1 session on "2026-03-05"
    When I view the Dashboard
    Then current streak is 2
    And longest streak is 2
```

### Feature: Weekly Bar Chart

```gherkin
Feature: Weekly bar chart data

  Background:
    Given the current date is Thursday "2026-03-05" (ISO week: Mon=Mar 2, Sun=Mar 8)

  Scenario: No sessions this week
    Given there are no sessions in the current ISO week
    When I view the Dashboard
    Then weekly bar chart has 7 bars
    And all bars have height 0
    And bar labels are "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"

  Scenario: Sessions on some days this week
    Given there is a session of 25 minutes on "2026-03-02" (Mon)
    And there is a session of 50 minutes on "2026-03-04" (Wed)
    When I view the Dashboard
    Then Mon bar height corresponds to 25 minutes
    And Wed bar height corresponds to 50 minutes
    And all other bars have height 0

  Scenario: Today's bar is visually highlighted
    Given there is a session on "2026-03-05"
    When I view the Dashboard
    Then the Thu bar has isToday = true
    And all other bars have isToday = false
```

### Feature: 30-Day Heatmap

```gherkin
Feature: 30-day heatmap

  Background:
    Given the current date is "2026-03-05"

  Scenario: Empty heatmap
    Given there are no sessions
    When I view the Dashboard
    Then the heatmap has 30 cells
    And all cells have sessionCount = 0

  Scenario: Session density reflected in cells
    Given there are 4 sessions on "2026-03-01"
    And there is 1 session on "2026-03-05"
    When I view the Dashboard
    Then the cell for "2026-03-01" has sessionCount = 4
    And the cell for "2026-03-05" has sessionCount = 1
    And a cell outside the 30-day window is not shown

  Scenario: Heatmap covers exactly the last 30 days
    When I view the Dashboard
    Then the earliest cell date is "2026-02-04"
    And the latest cell date is "2026-03-05"
```

### Feature: Personal Records

```gherkin
Feature: Personal records

  Scenario: No sessions
    Given there are no sessions
    When I view the Dashboard
    Then longest session shows "--"
    And best day shows "--"

  Scenario: Single session
    Given there is a session of 45 minutes on "2026-03-05"
    When I view the Dashboard
    Then longest session is 45 minutes
    And best day total is 45 minutes on "2026-03-05"

  Scenario: Multiple sessions across days
    Given sessions: 25 min on "2026-03-01", 50 min on "2026-03-02", 25 min on "2026-03-02"
    When I view the Dashboard
    Then longest single session is 50 minutes
    And best day is "2026-03-02" with 75 minutes
```

### Feature: Empty State

```gherkin
Feature: Empty state

  Scenario: No sessions recorded
    Given there are no session records
    When I navigate to the Dashboard tab
    Then I see an encouraging empty state message
    And no charts or stat cards are shown with misleading zeros
    And the message prompts the user to complete their first session
```

### Feature: Total Stats

```gherkin
Feature: All-time statistics

  Scenario: Stats match repository data
    Given there are 10 sessions with durations summing to 250 minutes
    When I view the Dashboard
    Then total sessions shows 10
    And total minutes shows 250
```

---

## Out of Scope (Sprint 8)

- Editing or deleting sessions from the Dashboard
- Filtering by session label
- Exporting data
- Comparing weeks against each other
- Push notifications or reminders based on streak
