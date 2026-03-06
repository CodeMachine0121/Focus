# Sprint 13 — Engineering Design
## Deep Focus Mode: Architecture & Implementation

### Architecture Overview

```
DeepFocusScreen.kt          ← Compose UI layer
       │
DeepFocusViewModel.kt       ← State holder (androidx.lifecycle.ViewModel)
       │
DeepFocusManager.kt         ← macOS system integration layer
       │
   ProcessBuilder / Runtime.getRuntime().exec()
       │
   ┌───┴──────────────────────┐
   │  osascript (DND)         │
   │  brightness CLI (dim)    │
   │  pgrep (process check)   │
   └──────────────────────────┘
```

Settings persisted via `java.util.prefs.Preferences.userNodeForPackage(DeepFocusManager::class.java)`.

---

### macOS Integration Details

#### Do Not Disturb (DND)

macOS 12+ uses Focus modes; the legacy DND preference key:
```bash
defaults -currentHost write \
  ~/Library/Preferences/ByHost/com.apple.notificationcenterui \
  doNotDisturb -boolean true
```

osascript approach (menu bar control):
```applescript
tell application "System Events"
  tell process "Control Center"
    -- click DND menu bar item
  end tell
end tell
```

We use `defaults` as the primary method (reliable across macOS versions) and
`osascript` as supplementary. Both wrapped in try/catch.

#### Display Brightness

Primary: `brightness` CLI (Homebrew: `brew install brightness`):
```bash
brightness 0.3   # sets to 30%
```

Fallback: `osascript` targeting `System Preferences` (macOS < 13) or
`System Settings` (macOS 13+). Read current brightness via:
```applescript
tell application "System Events"
  get brightness of (first display whose name is "Built-in Retina Display")
end tell
```

Store original brightness in `DeepFocusManager.originalBrightness: Float` before
applying dim, restore on deactivate.

#### App Monitoring (Blocked Apps)

Use `pgrep -i <appname>` to detect running processes. Exit code 0 = found.
Log to stdout: `[DeepFocus] Warning: <app> is running`.

In v1, we **warn only** — no forced quit. Forced quit would be too disruptive
and require elevated permissions; save for v2.

---

### Class Responsibilities

**`DeepFocusSettings`** — pure data class, no logic.

**`DeepFocusManager`**:
- `loadSettings()` / `saveSettings()` — Preferences I/O
- `activate(settings)` — orchestrates DND + brightness + app check
- `deactivate(settings)` — restores DND + brightness
- `isCurrentlyActive()` — in-memory boolean state
- Private helpers: `enableDnd()`, `disableDnd()`, `getBrightness()`,
  `setBrightness()`, `warnBlockedApps()`, `runScript()`, `runCommand()`

**`DeepFocusViewModel`**:
- Exposes `settings: DeepFocusSettings` and `isActive: Boolean` as Compose state
- `toggleActive()`, `updateDndEnabled()`, `updateDimEnabled()`, `updateDimLevel()`
- `addBlockedApp()`, `removeBlockedApp()`, `updateNewBlockedApp()`

**`DeepFocusScreen`**:
- `LazyColumn` with items: header card, info banner, settings card, blocked apps
- Calls `viewModel.load()` in `LaunchedEffect(Unit)`

---

### Error Handling Policy
All `ProcessBuilder` / `Runtime.exec` calls are wrapped in `try { } catch (_: Exception) {}`.
No exception propagates to the UI layer. Failures are silent (logged to stdout).
This ensures the app never crashes due to missing CLI tools or permission issues.

---

### Navigation Integration
- Add `NavItem("🔕", "Deep Focus")` to `navItems` list in `App.kt`
- Instantiate in `App`: `val deepFocusManager = remember { DeepFocusManager() }`
  and `val deepFocusViewModel = remember { DeepFocusViewModel(deepFocusManager) }`
- Wire in `when(selectedTab)` as case `7` (0-indexed, after "Work" at index 6)
