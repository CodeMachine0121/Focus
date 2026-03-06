# Sprint 12 – Engineering Design
## Smart Focus Scheduling

### Files Created
| File | Responsibility |
|------|---------------|
| `ScheduledSession.kt` | Data model + `RecurringType` enum |
| `ScheduleRepository.kt` | Preferences-based CRUD persistence |
| `ScheduleNotificationService.kt` | Background coroutine scheduler + AWT notification |
| `ScheduleViewModel.kt` | Compose state holder for the form |
| `ScheduleScreen.kt` | Compose UI |
| `App.kt` (modified) | Adds tab + wires dependencies |

---

### Persistence (ScheduleRepository)
Uses `java.util.prefs.Preferences` node `org/coding/afternoon/focus/schedules`.

Each session is serialised as a pipe-delimited string:
```
"<id>|<label>|<hour>|<minute>|<duration>|<recurringType>|<isActive>"
```
All sessions are stored under a single key `"sessions"` as a newline-joined blob —
identical to the pattern used by `SessionRepository` and `GoalRepository`.

CRUD operations:
- `save(session)` — appends to loaded list, re-serialises
- `loadAll()` — parses all lines, skips malformed entries
- `delete(id)` — filters out by id, re-serialises
- `update(session)` — replaces by id, re-serialises

---

### Background Scheduler (ScheduleNotificationService)
```
CoroutineScope(Dispatchers.IO + SupervisorJob())
  └─ launch { while(isActive) { checkAndNotify(); delay(60_000) } }
```

`checkAndNotify()`:
1. `LocalTime.now()` → current hour + minute
2. Load all active sessions from repository
3. For each matching session → `sendNotification(session)`
4. If `recurringType == ONCE` → `repository.delete(session.id)`

Notification: `SystemTray.getSystemTray().trayIcons[0].displayMessage(...)`.
Wrapped in try/catch — silently no-ops if SystemTray is unavailable or no tray icon exists.

`start()` is called from `main.kt` (or `App.kt` `LaunchedEffect`) after the tray icon is set up.
`stop()` cancels the coroutine job.

---

### ViewModel (ScheduleViewModel)
Extends `ViewModel`. Holds form state as `mutableStateOf` properties.

Key methods:
- `load()` — refreshes `sessions` from repository
- `addSession()` — validates, saves, reloads
- `deleteSession(id)` — deletes, reloads

---

### App.kt Changes
```kotlin
NavItem("📅", "Schedule")  // index 7

val scheduleRepository = remember { ScheduleRepository() }
val scheduleViewModel   = remember { ScheduleViewModel(scheduleRepository) }
val notificationService = remember { ScheduleNotificationService(scheduleRepository) }
LaunchedEffect(Unit) { notificationService.start() }

7 -> ScheduleScreen(scheduleViewModel)
```

---

### Risk & Mitigations
| Risk | Mitigation |
|------|-----------|
| SystemTray not available (CI, headless) | try/catch silently skips |
| Pipe character in label breaks parsing | label is trimmed but pipes not escaped — documented limitation; label field could strip `|` chars |
| Coroutine keeps running after window close | `notificationService.stop()` can be called on app exit |
