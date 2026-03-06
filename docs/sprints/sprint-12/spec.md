# Sprint 12 – Product Specification
## Smart Focus Scheduling

### Data Model

```kotlin
enum class RecurringType { ONCE, DAILY, WEEKDAYS }

data class ScheduledSession(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val scheduledHour: Int,       // 0–23
    val scheduledMinute: Int,     // 0–59
    val durationMinutes: Int = 25,
    val recurringType: RecurringType = RecurringType.ONCE,
    val isActive: Boolean = true
)
```

Serialisation format (pipe-delimited string per session):
`"id|label|hour|minute|duration|recurringType|isActive"`

---

### User Stories

#### US-1 · Add a Scheduled Session
> **As a** user  
> **I want to** add a future focus block with a label, time, duration and recurrence  
> **So that** I have a structured plan for the day

**Given** the Schedule screen is open  
**When** I fill in a non-blank label, choose 14:00, 45 min, DAILY, and press "Add Schedule"  
**Then** the session appears in the list sorted by time and persists after app restart

#### US-2 · Receive a Notification
> **As a** user  
> **I want to** receive a desktop notification at the scheduled time  
> **So that** I start my focus session without watching the clock

**Given** a scheduled session exists for 14:00 ONCE  
**When** the system clock reaches 14:00  
**Then** a system tray message "⏱ Focus Time! — Time to start: \<label\> (\<duration\> min)" is shown  
**And** the ONCE session is removed from the list

#### US-3 · Recurring Sessions
> **As a** user  
> **I want** DAILY and WEEKDAYS sessions to repeat automatically  
> **So that** I don't have to re-enter them every day

**Given** a DAILY session exists  
**When** it fires today  
**Then** the session remains active for tomorrow

**Given** a WEEKDAYS session exists  
**When** it fires on Friday  
**Then** the session remains active for Monday

#### US-4 · Delete a Session
> **As a** user  
> **I want to** delete a scheduled session  
> **So that** cancelled plans don't clutter my list

**Given** a session is in the list  
**When** I press the 🗑 button  
**Then** it is removed immediately from the UI and from persistent storage

---

### Acceptance Criteria

| ID | Criterion | Priority |
|----|-----------|----------|
| AC-1 | Blank label → button disabled / no session created | Must |
| AC-2 | Sessions persist across app restart (Preferences) | Must |
| AC-3 | List sorted ascending by HH:MM | Must |
| AC-4 | Notification sent within the scheduled minute | Must |
| AC-5 | ONCE session deleted after firing | Must |
| AC-6 | DAILY/WEEKDAYS session kept after firing | Must |
| AC-7 | No crash when SystemTray is unavailable | Must |
| AC-8 | Hour input clamped to 0–23, minute to 0–59, duration to 5–120 | Must |

---

### Edge Cases
- **Past-time sessions** — shown in the list; if time already passed today the notification fires
  when that minute is reached the next day (for recurring) or never (for ONCE).
- **Overlapping sessions** — allowed; both notifications fire.
- **App not running** — notifications cannot fire; sessions remain in Preferences for when app
  restarts.
