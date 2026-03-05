# Sprint 5 Stakeholder Requirements

## User Problem

When the focus timer completes, the app window grabs focus and surfaces itself — but if the user is on a different virtual desktop or has other windows maximized, the completion event is easy to miss. There is no persistent visual indicator that a session has ended, and the app provides no way to quickly start another session without bringing the window back into focus manually.

## Proposed Feature: System Tray Integration

Add a system tray icon that:
1. Persists in the OS menu bar / notification area at all times while the app is running
2. Updates its tooltip to reflect the live timer state (e.g. "Focus - 23:45 remaining", "Focus - Paused", "Focus - Complete!")
3. Shows a native OS balloon/popup notification when the session completes
4. Provides a right-click context menu with actions: **Start**, **Pause**, **Reset**, and **Quit**

This lets users keep the main window minimized or hidden and still control the timer and receive completion alerts entirely through the system tray.

## Acceptance Criteria

- The system tray icon is visible in the OS tray/menu bar as soon as the app launches and is removed when the app exits.
- The tray icon tooltip updates every second while the timer is running to show the remaining time (format: "MM:SS remaining").
- When the timer completes, a native OS balloon notification is displayed with title "Focus Complete" and message "Your focus session is complete."
- The right-click context menu exposes Start, Pause, Reset, and Quit actions that mirror the in-app button behavior.
- The feature degrades gracefully on systems where `SystemTray.isSupported()` returns false — the app still launches and functions normally without a tray icon.

## Out of Scope

- Left-click on the tray icon to show/hide the window (not in this sprint).
- Custom tray icon artwork / branding assets (use a programmatically generated icon).
- Per-session history or statistics displayed in the tray menu.
- macOS-specific menu bar extras (NSStatusItem) — standard AWT SystemTray is sufficient.
- Keyboard global shortcuts / hotkeys.
