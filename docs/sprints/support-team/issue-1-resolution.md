# Issue #1 Resolution — History Page: Title & Detail View

## Issue Summary
The History page displayed session rows with only the date, start time, and duration. Users had no way to see what they had been focusing on (session label/title), and there was no way to drill into a session's full details.

## Root Cause
`SessionRecord` had no `label` field. Even though `FocusTimerViewModel` captured a `sessionLabel` from the user, the `onSessionDismissed` callback only forwarded the duration integer — the label was discarded. `HistoryScreen` therefore had nothing to display.

## Fix Implemented

### Data model (`SessionRecord.kt`)
- Added `label: String = ""` field to the data class (default empty for backwards compatibility with existing persisted records).
- Updated `now()` factory to accept `label` parameter.
- Updated `toJson` to serialize the label field.
- Updated `fromJson` to deserialize label (falls back to `""` if missing, keeping compatibility with old records).

### Repository (`SessionRepository.kt`)
- Updated `record(durationMinutes: Int)` → `record(durationMinutes: Int, label: String = "")`.

### ViewModel (`FocusTimerViewModel.kt`)
- Changed `onSessionDismissed: ((Int) -> Unit)?` → `((Int, String) -> Unit)?`.
- Updated `dismiss()` to capture `sessionLabel` before clearing it, then forward both values.

### App wiring (`App.kt`)
- Updated the `onSessionDismissed` lambda to forward the label: `repository.record(durationMinutes, label)`.

### UI (`HistoryScreen.kt`)
- Session label shown as **bold primary text** (falls back to "Unnamed Session" if blank).
- Date shown as secondary text below the title.
- Duration shown right-aligned in primary color.
- Each card is now **clickable** — clicking toggles an animated `AnimatedVisibility` expanded section showing Date, Start Time, Duration, and Label detail rows.
- Expanded card gets `secondaryContainer` background and elevated shadow for visual distinction.

## Files Modified
- `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/SessionRecord.kt`
- `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/SessionRepository.kt`
- `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/FocusTimerViewModel.kt`
- `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/App.kt`
- `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/HistoryScreen.kt`

## Test Steps
1. Launch the app.
2. Enter a session label in "What are you focusing on?" and run a short timer.
3. After dismissing the completion dialog, navigate to the **History** tab.
4. Confirm the session appears with the label as the primary title.
5. Click the session card — confirm the detail section expands showing date, start time, duration, and label.
6. Click again — confirm it collapses.
7. Sessions recorded before this fix should appear with "Unnamed Session" as fallback.

## Build Result
`BUILD SUCCESSFUL` — `./gradlew compileKotlinJvm` with Java 21.
