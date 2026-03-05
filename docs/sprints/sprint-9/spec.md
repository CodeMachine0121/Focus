# Sprint 9 — Product Owner Spec
## Feature: Mindful Break Activity Coach

**Sprint:** 9
**Story Points:** 8
**Author:** Product Owner
**Date:** 2026-03-05

---

## 1. Overview

The Mindful Break Activity Coach adds a "Break Coach" tab to the Focus app. It presents a curated library of guided micro-activities organized into five categories, allows the user to mark activities as done, shuffles to a random activity, and tracks daily completion. When Auto-Cycle transitions to Break phase, a non-blocking snackbar prompts an activity suggestion.

---

## 2. Data Model

### BreakCategory (enum)
```
Movement     — desk exercises, stretches
EyeRest      — eye exercises, screen distance breaks
Mindfulness  — breathing, meditation, body scan
Hydration    — water, snack reminders
Creative     — journaling, sketching, imagination prompts
```

### BreakActivity (data class)
```
id:              String      — stable unique identifier (e.g. "neck_roll")
title:           String      — short display name (e.g. "Neck Roll")
description:     String      — 1-2 sentence instruction
category:        BreakCategory
durationSeconds: Int         — expected time to complete (e.g. 60)
emoji:           String      — single emoji for card icon (e.g. "🔄")
```

### DailyCompletionRecord (internal, not a public data class)
- Stored in Java Preferences as a comma-separated list of activity IDs under a date-keyed node.
- Key format: `completed_<YYYY-MM-DD>`

---

## 3. Activity Library (representative excerpt — full library has 20+ entries)

| ID | Category | Title | Description | Duration |
|----|----------|-------|-------------|----------|
| neck_roll | Movement | Neck Roll | Gently roll your head in a slow circle, 3 reps each direction. | 60s |
| shoulder_shrug | Movement | Shoulder Shrug | Raise shoulders to ears, hold 3 seconds, release. Repeat 5 times. | 45s |
| desk_pushup | Movement | Desk Push-Up | Place hands on desk edge, do 10 push-ups against the desk. | 60s |
| wrist_stretch | Movement | Wrist Stretch | Extend arm palm-up, gently pull fingers back for 15 seconds each hand. | 45s |
| standing_calf_raise | Movement | Calf Raise | Stand and rise onto tiptoes 15 times to activate leg circulation. | 60s |
| twenty_twenty | EyeRest | 20-20-20 Rule | Look at something 20 feet away for 20 seconds to relax eye muscles. | 30s |
| eye_palming | EyeRest | Eye Palming | Cup warm palms over closed eyes for 30 seconds of darkness. | 45s |
| blink_reset | EyeRest | Blink Reset | Blink rapidly 20 times to re-lubricate eyes, then close them softly for 10 seconds. | 45s |
| figure_eight_eyes | EyeRest | Figure-Eight Eyes | Trace an imaginary figure-8 with your eyes slowly, 3 times in each direction. | 60s |
| box_breathing | Mindfulness | Box Breathing | Inhale 4s, hold 4s, exhale 4s, hold 4s. Repeat 4 cycles. | 90s |
| body_scan | Mindfulness | 1-Minute Body Scan | Close eyes and mentally scan from scalp to toes, noticing any tension. | 60s |
| gratitude_pause | Mindfulness | Gratitude Pause | Think of 3 things you are grateful for right now. | 60s |
| mindful_breath | Mindfulness | Mindful Breath | Focus solely on your next 10 breaths, counting each exhale. | 90s |
| drink_water | Hydration | Drink Water | Stand up, walk to the kitchen, and drink a full glass of water. | 60s |
| healthy_snack | Hydration | Healthy Snack | Grab a piece of fruit or a handful of nuts. Eat slowly and away from the screen. | 120s |
| tea_break | Hydration | Tea Break | Brew a cup of herbal tea. Use the steeping time to step away from the screen. | 180s |
| journal_prompt | Creative | Journal Prompt | Write 3 sentences about what you are working on and what comes next. | 120s |
| quick_sketch | Creative | Quick Sketch | Draw a simple doodle of anything in your view — no artistic skill required. | 120s |
| window_gaze | Creative | Window Gaze | Look out a window and invent a short backstory for someone you see. | 60s |
| desk_tidy | Creative | Desk Tidy | Spend 90 seconds organizing one small section of your physical workspace. | 90s |

---

## 4. Feature Behaviour

### 4.1 Break Coach Tab

- Appears as the third tab ("Break Coach") in the app's tab bar.
- Default state: category filter is "All", full activity list is shown.
- Activities that have been marked done today appear with a visual completed state (checked icon, muted color).

### 4.2 Category Filter

- Filter chips displayed horizontally: All | Movement | Eye Rest | Mindfulness | Hydration | Creative.
- Selecting a chip filters the list to that category. Only one chip active at a time.
- "All" always shows the complete 20+ activity list.

### 4.3 Mark Done

- Each activity card has a "Done" button.
- Tapping "Done" records the activity ID in today's completion set (persisted).
- The card transitions to a completed visual state immediately (optimistic UI).
- Tapping "Done" again on a completed activity has no effect (idempotent).

### 4.4 Shuffle

- A Floating Action Button (FAB) or prominent button labelled "Shuffle".
- Tapping selects a random uncompleted activity from the current filtered list (if all are done, picks from all).
- The list scrolls to the selected activity and it is briefly highlighted.

### 4.5 Daily Completion Counter

- A progress bar + text at the top of the tab: e.g., "3 of 20 activities done today".
- Updates in real time as activities are marked done.
- Resets to 0 on a new calendar day.

### 4.6 Auto-Cycle Break Phase Notification (Bonus)

- The BreakCoachViewModel observes `FocusTimerViewModel.currentPhase`.
- When phase transitions to `TimerPhase.Break`, a snackbar appears within the Break Coach tab reading: "Break time! Try: [random activity title]".
- The snackbar is dismissible and does not block interaction.
- The timer is never modified.

---

## 5. Gherkin Scenarios

```gherkin
Feature: Break Coach — Browse Activities by Category

  Scenario: Default view shows all activities
    Given the user opens the "Break Coach" tab
    Then the "All" filter chip is selected
    And at least 20 activity cards are visible

  Scenario: Filter by Movement category
    Given the user opens the "Break Coach" tab
    When the user taps the "Movement" filter chip
    Then only Movement activities are shown
    And no Eye Rest, Mindfulness, Hydration, or Creative activities are visible

  Scenario: Switch from Movement back to All
    Given the user is viewing Movement activities
    When the user taps the "All" filter chip
    Then all 20+ activities are shown again

Feature: Break Coach — Mark Activity Done

  Scenario: Mark an activity as done
    Given the user opens the "Break Coach" tab
    And the "Neck Roll" activity is not yet completed today
    When the user taps "Done" on the "Neck Roll" card
    Then the "Neck Roll" card shows a completed state
    And the daily completion counter increments by 1

  Scenario: Marking done is idempotent
    Given "Neck Roll" has already been marked done today
    When the user taps "Done" on the "Neck Roll" card again
    Then the completion count does not change

  Scenario: Done state persists across app restart
    Given the user marks "Box Breathing" as done
    When the user closes and relaunches the app on the same calendar day
    Then "Box Breathing" still shows as completed
    And the completion counter reflects the previously completed activities

  Scenario: Done state resets on a new day
    Given the user has completed 5 activities on 2026-03-05
    When the app is opened on 2026-03-06
    Then the completion counter shows 0
    And all activity cards show as not completed

Feature: Break Coach — Shuffle

  Scenario: Shuffle picks an uncompleted activity
    Given 15 activities have been marked done today
    And 5 activities remain uncompleted
    When the user taps the "Shuffle" button
    Then one of the 5 uncompleted activities is highlighted

  Scenario: Shuffle when all activities are done falls back to any activity
    Given all 20 activities have been marked done today
    When the user taps "Shuffle"
    Then one of the 20 activities is highlighted (no crash, no empty state)

Feature: Break Coach — Completion Counter

  Scenario: Counter reflects real-time completions
    Given the completion counter shows "0 of 20 activities done today"
    When the user marks 3 activities as done
    Then the counter shows "3 of 20 activities done today"

Feature: Break Coach — Auto-Cycle Integration (Bonus)

  Scenario: Break phase triggers activity suggestion snackbar
    Given the user has Auto-Cycle enabled on the Timer tab
    And the focus session completes, transitioning to Break phase
    When the Break Coach tab is visible or the user switches to it
    Then a non-blocking snackbar appears suggesting a random activity
    And the timer continues running without interruption

  Scenario: Break Coach never modifies the timer
    Given the Break Coach tab is open
    When the user interacts with any activity card (Done, Shuffle)
    Then the timer state remains unchanged
    And remainingSeconds does not change
```

---

## 6. Non-Functional Requirements

- The activity library is hardcoded in `BreakActivityLibrary.kt`; no network calls.
- Persistence uses `java.util.prefs.Preferences` — no files, no database.
- The BreakCoachViewModel must extend `androidx.lifecycle.ViewModel`.
- All Compose state must be observable via `mutableStateOf` or `SnapshotStateList`.
- The Break Coach feature must compile without errors on the existing Kotlin + Compose for Desktop stack.
