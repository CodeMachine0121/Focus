# Sprint 11 — Stakeholder Brief: Achievement & Gamification System

## User Problem
Focus is a utility-first app: it helps users run timers and track sessions, but it offers no intrinsic motivation to keep coming back. Users who build consistent habits do so through sheer willpower. There is no feedback loop that rewards effort, celebrates milestones, or gives users a sense of progression. Without engagement hooks, retention drops after the first week.

## Proposed Feature: Achievement & Gamification System
**Story Points: 13**

Add a gamification layer that transforms raw usage data (session count, total minutes, streak days) into visible progression, badges, and daily challenges.

## Key User Needs

| Need | Description |
|------|-------------|
| Earn badges | Unlock achievements for milestones: first session, 5-day streak, 100 total hours, etc. |
| Level-up system | Level (1–10) based on total focus minutes; progress bar toward next level |
| Daily challenges | Short-term goals reset daily: "Complete 5 sessions today", "Focus 60 min today" |
| Visual celebration | Dialog/banner when an achievement is newly unlocked |
| Achievement gallery | Browse all achievements, see which are locked vs. unlocked |
| Progress visibility | Always know how close you are to the next level and challenge completion |

## Success Criteria
- User opens the Achievement screen and sees their current level with an XP progress bar
- Unlocked badges are highlighted in gold; locked ones show a lock icon
- Active daily challenges show real-time progress
- When a new achievement is earned (on screen load), an "Achievement Unlocked!" dialog appears
- The screen is reachable from the NavigationRail ("🏆 Achievements" tab)
