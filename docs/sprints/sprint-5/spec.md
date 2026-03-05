# Sprint 5 Specification: System Tray Integration

## Feature Title

System Tray Icon with Live Timer Status, Completion Notification, and Context Menu

---

## Background

```gherkin
Background:
  Given the Focus timer desktop app is running on a JVM-capable OS
  And the OS supports java.awt.SystemTray (SystemTray.isSupported() == true)
  And the timer is in its default Idle state with 25:00 set
```

---

## Scenarios

### Scenario 1: Tray icon appears on launch and is removed on exit

```gherkin
Scenario: System tray icon lifecycle
  Given the app has just launched
  Then a tray icon should be visible in the OS system tray / menu bar
  And the tray icon tooltip should read "Focus - Ready"

  When the user closes the main window (exits the app)
  Then the tray icon should be removed from the system tray
```

---

### Scenario 2: Tray tooltip reflects live countdown while timer is running

```gherkin
Scenario: Tooltip updates during active countdown
  Given the timer is in Idle state with 25 minutes set
  When the user clicks Start (via the UI or tray context menu)
  Then the tray icon tooltip should update every second to show "Focus - MM:SS remaining"
    Examples:
      | elapsed | expected tooltip          |
      | 0s      | "Focus - 25:00 remaining" |
      | 1s      | "Focus - 24:59 remaining" |
      | 60s     | "Focus - 24:00 remaining" |

  When the user clicks Pause
  Then the tray icon tooltip should read "Focus - Paused"

  When the user clicks Reset
  Then the tray icon tooltip should read "Focus - Ready"
```

---

### Scenario 3: Native notification fires when the session completes

```gherkin
Scenario: Completion balloon notification
  Given the timer is running with 1 second remaining
  When the countdown reaches 00:00
  Then a native OS balloon / popup notification is displayed
  And the notification caption reads "Focus Complete"
  And the notification text reads "Your focus session is complete."
  And the tray icon tooltip changes to "Focus - Complete!"
```

---

### Scenario 4: Context menu provides Start, Pause, Reset, and Quit actions

```gherkin
Scenario: Context menu controls mirror in-app controls
  Given the tray icon is visible
  When the user right-clicks the tray icon
  Then a context menu appears with items: "Start", "Pause", "Reset", "Quit"

  When the user clicks "Start" in the context menu and the timer is Idle
  Then the timer transitions to Running state
  And the tooltip begins updating with remaining time

  When the user clicks "Pause" in the context menu and the timer is Running
  Then the timer transitions to Paused state
  And the tooltip shows "Focus - Paused"

  When the user clicks "Reset" in the context menu
  Then the timer resets to Idle state with the full duration restored
  And the tooltip shows "Focus - Ready"

  When the user clicks "Quit" in the context menu
  Then the application exits cleanly and the tray icon is removed
```

---

### Scenario 5: Graceful degradation when SystemTray is not supported

```gherkin
Scenario: App runs normally without system tray on unsupported OS
  Given SystemTray.isSupported() returns false
  When the app launches
  Then no exception is thrown
  And the main window opens normally
  And all timer functions (Start, Pause, Reset) work as before
```

---

## Implementation Notes

- Use `java.awt.SystemTray`, `java.awt.TrayIcon`, and `java.awt.PopupMenu` — no extra dependencies.
- The tray icon image should be a simple programmatically generated `BufferedImage` (e.g. a filled circle in the app's primary color).
- Tooltip updates should be driven by observing `FocusTimerViewModel` state, using a coroutine that polls or collects state changes on the main thread.
- The `Quit` menu item must call `exitApplication()` / `System.exit(0)` so the process terminates cleanly.
- Encapsulate all tray logic in a new file `SystemTrayManager.kt` to keep `main.kt` diff minimal.
