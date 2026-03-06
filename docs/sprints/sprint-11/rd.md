# Sprint 11 — RD Implementation Plan: Achievement & Gamification System

## Architecture Decisions

1. **No new persistence beyond Preferences**: Achievements are computed from `SessionRepository.sessions` (already persisted) and `StreakCalculator`. Only two boolean flags (`ach_early_bird`, `ach_night_owl`) need separate Preferences storage.
2. **StreakCalculator is an `object`**: Call `StreakCalculator.calculateCurrentStreak(records)` — no instantiation needed.
3. **SessionRepository uses `sessions: SnapshotStateList`**: Use `repository.sessions.toList()` (not a `loadAll()` method) to get all records.
4. **ViewModel holds previous state for delta detection**: `newlyUnlocked` is computed by diffing the previous and new achievement lists on each `refresh()` call.
5. **AchievementViewModel accepts dependencies via constructor**: Follows existing ViewModel pattern (GoalViewModel, DashboardViewModel).

## Files to Create

| File | Role |
|------|------|
| `Achievement.kt` | Data models: `Achievement`, `Challenge`, `UserLevel` |
| `AchievementRepository.kt` | Derives achievements, challenges, and level from raw stats |
| `AchievementViewModel.kt` | Orchestrates data loading, exposes Compose state |
| `AchievementScreen.kt` | Compose UI: level card, challenges, achievement gallery |

## Files to Modify

| File | Change |
|------|--------|
| `App.kt` | Add `NavItem("🏆", "Achievements")`, wire `AchievementViewModel`, add `when` case |

## Key Implementation Notes

### Achievement Evaluation Logic
```
totalSessions  = repository.sessions.size
totalMinutes   = repository.sessions.sumOf { it.durationMinutes }
streakDays     = StreakCalculator.calculateCurrentStreak(repository.sessions.toList())
todaySessions  = repository.sessions.filter { it.date == SessionRecord.today() }
todayMinutes   = todaySessions.sumOf { it.durationMinutes }
```

### Level Thresholds Array
```kotlin
private val levelThresholds = listOf(0, 60, 180, 360, 720, 1200, 2000, 3000, 5000, 8000)
```
Level is the 1-based index of the last threshold not exceeded by `totalMinutes`. Max level is 10.

### AchievementScreen nested LazyVerticalGrid
`LazyVerticalGrid` cannot be placed inside a `LazyColumn` item without a fixed height. Set `Modifier.height(600.dp)` on the grid — acceptable for MVP since 13 items at ~100dp each fit within this bound.

### App.kt Integration
```kotlin
val achievementRepository = remember { AchievementRepository() }
val achievementViewModel = remember { AchievementViewModel(repository, achievementRepository) }
```
Then in `when(selectedTab)`:
```kotlin
7 -> AchievementScreen(achievementViewModel)
```

## Dependency Notes
- `AchievementViewModel` does NOT need `StreakCalculator` as a constructor parameter — it calls the object directly: `StreakCalculator.calculateCurrentStreak(sessions)`.
- This avoids unnecessary constructor complexity and matches the `object` declaration.
