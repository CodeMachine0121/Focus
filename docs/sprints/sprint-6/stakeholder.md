# Sprint 6 Stakeholder Requirements

## User Problem

Power users of the Focus app run multiple Pomodoro sessions each day toward different objectives — finishing a report, reviewing code, studying a course module — but there is no way to plan or track those objectives within the app itself. Currently, the only planning affordance is the per-session "session label" field, which disappears after each session ends. Users have to maintain a separate to-do list (sticky notes, Notion, paper) just to remember what they intended to work on that day.

This creates two concrete friction points:

1. **Fragmented context switching**: The user must alt-tab between the timer app and their external task list to know what to work on next, breaking their flow at the exact moment they should be starting focused work.
2. **No daily accountability view**: At the end of the day, there is no in-app record of which objectives were actually completed (vs. just how many minutes were logged). The session history shows raw durations but carries no semantic meaning about accomplishments.

## Proposed Feature: Daily Focus Goal Planner

Add a dedicated "Goals" tab to the app that serves as a structured daily planning board. Before starting their first Pomodoro of the day, a user can define up to 5 focus goals — each with a short title and an estimate of how many Pomodoros it will take. As they complete sessions, they can mark goals done directly in the app. A progress bar at the top of the Goals tab shows how many of today's goals are complete at a glance.

Goals are scoped to the current calendar day. When the app is restarted on a new day, a fresh blank slate is presented. Completed goals from previous days are discarded automatically.

This is explicitly NOT an enhancement of the existing per-session label feature. The session label remains a quick, ephemeral annotation on a single session. Goals operate at a higher level: they represent the day's intended outcomes, not the running session's topic.

## Key User Stories

- As a user, I want to write down my top 1-5 focus goals at the start of my day so I have a clear plan before I start the timer.
- As a user, I want to mark a goal as complete so I get a satisfying sense of closure and can see my progress.
- As a user, I want to delete a goal I've abandoned so my list stays accurate.
- As a user, I want my goals to survive an app restart during the same calendar day so I don't have to re-enter them.
- As a user, I want to see a daily progress bar so I know at a glance how many goals I've finished.

## Acceptance Criteria

1. A new "Goals" tab is visible in the main navigation alongside "Timer" and "History".
2. The Goals tab displays a progress bar showing `completed / total` goals for today.
3. Users can add a new goal with a title (required, max 60 characters) and estimated Pomodoros (1-10, required).
4. The goal list is limited to 5 goals maximum. The add button is disabled when 5 goals exist.
5. Each goal card displays the title, estimated Pomodoro count, and completion status.
6. Users can mark any incomplete goal as complete. Completed goals are visually distinguished (strikethrough title, muted color).
7. Users can delete any goal (complete or incomplete) with a delete action on the card.
8. Goals are persisted using Java Preferences and survive app restarts on the same calendar day.
9. Goals created on a previous calendar day are not shown on a new day (stale data is silently discarded on load).
10. An empty state message is shown when no goals exist for the day.
11. The feature introduces no new dependencies — only standard JVM + Compose + androidx.lifecycle APIs already in the project.

## Out of Scope

- Associating a specific timer session with a specific goal (linking session label to a goal card).
- Editing a goal's title or Pomodoro estimate after creation.
- Carrying goals forward to the next day.
- Sorting or reordering goals.
- Goal categories or tags.
- Cloud sync or export of goals.

## Fibonacci Complexity: 8

The story earns 8 points because it requires a new data model, a new persistence layer following the existing Preferences pattern, a new ViewModel with multiple state operations, and a non-trivial Compose screen with dialogs, FAB, progress bar, and card list — all of which must be built from scratch without touching existing files.
