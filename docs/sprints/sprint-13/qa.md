# Sprint 13 — QA Report
## Deep Focus Mode: macOS System Integration

### Build Status
**✅ BUILD SUCCESSFUL**
```
Task :composeApp:compileKotlinJvm
BUILD SUCCESSFUL in 2s
8 actionable tasks: 8 executed
```
*Note: Build requires JAVA_HOME pointing to JDK 21 (JBR-21.0.9). Java 25.0.2 (Homebrew)
triggers a version-parse bug in the embedded Kotlin compiler (JavaVersion.parse fails on
"25.0.2" format). JBR-21 is the stable runtime for this project.*

---

### Acceptance Criteria Verification

| # | Criterion | Status | Notes |
|---|-----------|--------|-------|
| AC-1 | 🔕 Deep Focus tab in NavigationRail | ✅ | Added at index 7 in App.kt |
| AC-2 | Toggle activates/deactivates DFM | ✅ | `viewModel.toggleActive()` wired to Button |
| AC-3 | DND command runs on activation | ✅ | `enableDnd()` calls `defaults` + osascript |
| AC-4 | Brightness dimmed on activation | ✅ | `setBrightness(settings.dimLevel)` via `brightness` CLI |
| AC-5 | Original brightness captured and restored | ✅ | `originalBrightness` stored before dim, restored on deactivate |
| AC-6 | Settings controls disabled while active | ✅ | All controls have `enabled = !isActive` |
| AC-7 | Blocked apps list persisted | ✅ | `Preferences.put("blocked_apps", ...)` on every change |
| AC-8 | No crash on missing CLI tools | ✅ | All `Runtime.exec` / `ProcessBuilder` wrapped in try/catch |
| AC-9 | Slider range 10%–80% | ✅ | `valueRange = 0.1f..0.8f` in Slider |
| AC-10 | Info banner shown | ✅ | Amber card with explanation always visible |

---

### Files Added / Modified

| File | Change |
|------|--------|
| `DeepFocusSettings.kt` | NEW — data model |
| `DeepFocusManager.kt` | NEW — macOS system integration |
| `DeepFocusViewModel.kt` | NEW — Compose state holder |
| `DeepFocusScreen.kt` | NEW — full Compose UI |
| `App.kt` | MODIFIED — +NavItem, +viewModel instances, +case 7 |

---

### Manual Test Plan (to run on device)

1. **Activate DFM**: Click "Activate Deep Focus" → card turns red, status = ACTIVE.
2. **DND check**: Open Notification Center — DND should be enabled.
3. **Brightness check**: Screen should dim to configured level (default 30%).
4. **Settings locked**: Verify switches, slider, text field, Add/Remove buttons are greyed out.
5. **Deactivate DFM**: Click "Deactivate" → card turns slate, status = INACTIVE.
6. **Restore check**: DND disabled, screen brightness restored to pre-session level.
7. **Add blocked app**: Type "YouTube" + Add → appears in list.
8. **Persistence**: Restart app → "YouTube" still in blocked apps list.
9. **Remove app**: Click Remove → app disappears from list.
10. **Graceful failure**: Rename/move `brightness` CLI → activation still completes without crash.

---

### Known Limitations / Future Work
- macOS 12+ Focus Modes (per-app DND) not yet integrated; using legacy `doNotDisturb` key.
- `brightness` CLI requires separate Homebrew install (`brew install brightness`); without it, display dimming silently skips.
- Forced app-quit is intentionally deferred to v2 (requires user permission prompt).
- Java 25 incompatibility with Kotlin compiler is a project-level concern, not Sprint 13 specific.
