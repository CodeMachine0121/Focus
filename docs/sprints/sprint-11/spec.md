# Sprint 11 — Product Spec: Achievement & Gamification System

## Data Models

### Achievement
```kotlin
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
```

### Challenge
```kotlin
data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false
)
```

### UserLevel
```kotlin
data class UserLevel(
    val level: Int,
    val totalMinutes: Int,
    val currentLevelMinutes: Int,
    val nextLevelThreshold: Int
)
```

---

## User Stories

### Story 1 — View Level & XP Progress
**Given** I have completed focus sessions totaling some number of minutes,  
**When** I open the Achievements screen,  
**Then** I see my current level (1–10), level title, emoji avatar, and a progress bar showing % toward the next level.

**Acceptance Criteria:**
- Level is derived from `totalMinutes` against fixed thresholds
- Progress bar is visually distinct (gold accent on purple background)
- Total minutes label is shown

---

### Story 2 — View Achievement Gallery
**Given** I am on the Achievements screen,  
**When** I scroll to the gallery section,  
**Then** I see a 2-column grid of all 13 achievements; unlocked ones show their emoji with a gold background; locked ones show 🔒.

**Acceptance Criteria:**
- 13 defined achievements covering session count, total hours, and streak milestones
- Each card shows: emoji/lock, title, description (truncated to 2 lines)
- No pagination required for MVP (all shown in a fixed-height grid)

---

### Story 3 — View Daily Challenges
**Given** it is a calendar day,  
**When** I open the Achievements screen,  
**Then** I see today's 4 challenges with progress bars and completion indicators.

**Acceptance Criteria:**
- Challenges count sessions and minutes completed **today** only
- Progress bar fills proportionally
- A ✅ appears when the challenge is completed
- Challenges reset on the next calendar day (derived from today's sessions)

---

### Story 4 — Achievement Unlock Celebration
**Given** I have just met the criteria for a new achievement,  
**When** I open the Achievements screen,  
**Then** a dialog shows "🎉 Achievement Unlocked!" with the achievement's emoji and title.

**Acceptance Criteria:**
- Dialog appears only for achievements that were locked on the previous state
- User can dismiss the dialog with "Awesome!" button
- Only one dialog shown at a time (first newly-unlocked wins)

---

## Level Thresholds

| Level | Name | Minutes Required |
|-------|------|-----------------|
| 1 | Rookie Focuser | 0 |
| 2 | Apprentice | 60 |
| 3 | Focused Mind | 180 |
| 4 | Flow Seeker | 360 |
| 5 | Focus Warrior | 720 |
| 6 | Deep Worker | 1,200 |
| 7 | Flow Master | 2,000 |
| 8 | Productivity Sage | 3,000 |
| 9 | Focus Legend | 5,000 |
| 10 | Transcendent | 8,000 |

---

## Out of Scope (MVP)
- Push notifications for achievement unlocks
- Social/sharing features
- Weekly challenges (daily only for MVP)
- Custom achievement images
- Achievement timestamps displayed in gallery
- Server-side persistence
