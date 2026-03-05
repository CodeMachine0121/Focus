# Focus

A Pomodoro productivity timer for Desktop, built with Kotlin Multiplatform and Compose Multiplatform.

## Features

| Feature | Description |
|---------|-------------|
| ⏱ **Timer** | Pomodoro-style timer (default 25 min) with auto-cycling between focus and break phases. Supports custom durations, session labels, and pause/resume. |
| 📋 **History** | Session log with date, start time, and duration, persisted locally. |
| 🎯 **Goals** | Daily goal tracking (up to 10 goals/day) with title, estimated Pomodoros, and completion checkbox. |
| 🎵 **Sounds** | Ambient sound engine with white noise, ocean waves, rain, forest, and café sounds. Adjustable volume, independent of the timer. |
| 📊 **Dashboard** | Productivity analytics: current streak, total sessions, focused minutes, 7-day bar chart, 30-day heatmap, and personal records. |
| 🧘 **Break Coach** | 60+ categorised break activities (stretches, walks, meditation, etc.) with today's completion tracking. |
| 💼 **Workspace** | Auxiliary ambient workspace timer for general task tracking. |

## Getting Started

### Run

```shell
# macOS / Linux
./gradlew :composeApp:run

# Windows
.\gradlew.bat :composeApp:run
```

### Package

```shell
# macOS (DMG), Windows (MSI), or Linux (DEB) — detected automatically
./gradlew :composeApp:createDistributable
```

## Architecture

MVVM + Repository pattern with Compose Multiplatform UI.

```
UI (Compose screens)
  └── ViewModels (FocusTimer, Dashboard, Goal, AmbientSound, BreakCoach, Workspace)
        └── Repositories (Session, Goal, BreakCoach)
              └── Persistence (Java Preferences API — no external database)
```

Navigation is a sidebar with 7 tabs (⏱ 📋 🎯 🎵 📊 🧘 💼). The app minimises to the system tray when the window is closed.

## Tech Stack

- **Kotlin** 2.3.0 · **Compose Multiplatform** 1.10.0 · **Material3** 1.10.0
- **AndroidX Lifecycle ViewModel** 2.9.6 · **Kotlinx Coroutines** 1.10.2
- **Target platform:** Desktop JVM (macOS · Windows · Linux)

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
