# Sprint 11 — Designer Brief: Achievement & Gamification System

## Color Palette
- **Level system**: Purple `#6750A4` (Material You primary) — conveys prestige and progression
- **Achievement gold**: `#FFD700` — universally associated with rewards and excellence
- **Locked state**: Gray `#9E9E9E` at 10% opacity — clearly de-emphasized
- **Challenge accent**: Material default LinearProgressIndicator colors

## Screen: AchievementScreen

### Layout (top to bottom, scrollable LazyColumn)

```
┌─────────────────────────────────────────┐
│  [Level Card — purple background]        │
│  🔥  Level 5                             │
│      Focus Warrior          720 min total│
│  ████████████░░░░  68%                   │
│  480 / 720 min to next level             │
└─────────────────────────────────────────┘

📋 Today's Challenges
┌─────────────────────────────────────────┐
│ Triple Focus            [✅ if done]     │
│ Complete 3 focus sessions today          │
│ ████████░░░░  2 / 3                     │
└─────────────────────────────────────────┘
... (4 challenge cards)

🏆 Achievements
┌───────────────┐  ┌───────────────┐
│ 🎯            │  │ 🔒            │
│ First Steps   │  │ Getting Started│
│ Complete your │  │ Complete 5    │
│ first session │  │ sessions      │
└───────────────┘  └───────────────┘
... (2-column grid, 13 achievements)
```

### Component Breakdown

#### LevelCard
- Container: `Card` with `containerColor = Color(0xFF6750A4)`
- Row: emoji (40sp) + Column (level label 12sp, title bold 20sp, total minutes 12sp 80% alpha)
- `LinearProgressIndicator` height 8dp, clipped round corners 4dp
  - `color = Color(0xFFFFD700)` (gold)
  - `trackColor = white 30% alpha`
- Progress text: white 70% alpha, 11sp

#### ChallengeCard
- Container: default `Card`
- Header row: title (SemiBold) + ✅ if completed (SpaceBetween)
- Description: 12sp gray
- `LinearProgressIndicator` height 6dp, round corners 3dp
- Count label: 11sp gray

#### AchievementCard
- Background: gold 15% alpha if unlocked, gray 10% alpha if locked
- Center-aligned column: emoji/🔒 (28sp), title (SemiBold 13sp), description (10sp gray, maxLines=2)
- No border for MVP; card elevation default

#### UnlockDialog (`AlertDialog`)
- Title: "🎉 Achievement Unlocked!"
- Body: `{emoji} {title}\n{description}`
- Single confirm button: "Awesome!"

## Navigation Addition
Add `NavItem("🏆", "Achievements")` as the 8th tab in the `NavigationRail`.

## Animations (MVP scope)
- The `AlertDialog` uses platform default fade-in — no custom animation required for MVP
- Future: confetti `Canvas`-based animation overlay triggered on unlock
