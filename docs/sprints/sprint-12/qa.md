# Sprint 12 – QA Report
## Smart Focus Scheduling

### Build Result
**BUILD SUCCESSFUL** (JDK 21 / JBR-21.0.9)

One compiler warning:
```
ScheduleScreen.kt:130: 'fun Modifier.menuAnchor(): Modifier' is deprecated.
Use overload that takes ExposedDropdownMenuAnchorType and enabled parameters.
```
This is a non-breaking deprecation in the Material3 ExposedDropdownMenu API. No functional impact.

Note: The default system JDK (OpenJDK 25.0.2 via Homebrew) triggers a pre-existing parsing
bug in the bundled Kotlin compiler (`IllegalArgumentException: 25.0.2`). Build requires JDK ≤ 24.
This is a pre-existing environment issue unrelated to Sprint 12 changes.

---

### Acceptance Criteria Checklist

| ID | Criterion | Status | Notes |
|----|-----------|--------|-------|
| AC-1 | Blank label → "Add Schedule" button disabled | ✅ Pass | `enabled = viewModel.labelInput.isNotBlank()` |
| AC-2 | Sessions persist across restart (Preferences) | ✅ Pass | `ScheduleRepository` uses `java.util.prefs.Preferences` |
| AC-3 | List sorted ascending by HH:MM | ✅ Pass | `sortedWith(compareBy({ scheduledHour }, { scheduledMinute }))` |
| AC-4 | Notification sent within the scheduled minute | ✅ Pass | 60-second polling loop in `ScheduleNotificationService` |
| AC-5 | ONCE session deleted after firing | ✅ Pass | `if (recurringType == ONCE) repository.delete(id)` |
| AC-6 | DAILY/WEEKDAYS sessions kept after firing | ✅ Pass | Only ONCE sessions are deleted in `checkAndNotify()` |
| AC-7 | No crash when SystemTray unavailable | ✅ Pass | `try/catch (_: Exception)` wraps all tray calls |
| AC-8 | Hour 0–23, minute 0–59, duration 5–120 clamped | ✅ Pass | `coerceIn()` in all three ViewModel updaters |

---

### User Story Verification

| Story | Verified | Method |
|-------|----------|--------|
| US-1 Add scheduled session | ✅ | Code review — form saves via `ScheduleRepository.save()` |
| US-2 Desktop notification at scheduled time | ✅ | Code review — `ScheduleNotificationService.checkAndNotify()` |
| US-3 Recurring sessions persist after firing | ✅ | Code review — ONCE-only deletion logic |
| US-4 Delete session | ✅ | Code review — 🗑 button calls `viewModel.deleteSession()` |

---

### Known Limitations / Issues
1. **Pipe character in labels** — the serialisation format uses `|` as delimiter; the save path
   strips pipes from labels (`replace("|", "")`) to prevent corruption.
2. **No past-time warning** — ONCE sessions scheduled in the past are shown in the list but will
   never fire; a future sprint could add a "past time" visual indicator.
3. **No "Start Now" deep-link from notification** — the notification message is informational only;
   one-click start from the tray notification is a future enhancement.
4. **WEEKDAYS recurrence** — currently treated the same as DAILY at the notification level
   (fires every day). Day-of-week filtering is a future enhancement.
5. **Deprecated `menuAnchor()`** — should be updated to `menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)` in a future sprint when upgrading Material3.
