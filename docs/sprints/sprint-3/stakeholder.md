# Sprint 3 Stakeholder Requirements

## User Problem

After each focus session completes, users must manually dismiss the dialog, choose a break duration, and press Start again. This constant context-switching interrupts the rhythm of the Pomodoro technique, which depends on predictable, automatic alternation between focus and break intervals.

## Proposed Feature: Auto-Cycle Mode

Add an **Auto-Cycle** toggle to the focus timer. When enabled, after a focus session completes the app automatically starts a short break timer (defaulting to 5 minutes), and after the break completes it automatically starts another focus session — cycling indefinitely until the user pauses or turns off Auto-Cycle.

The completion dialog is suppressed during auto-cycling; instead, a non-blocking status label shows what phase is active ("Focus" or "Break") and which cycle number is currently running (e.g., "Cycle 2 — Break").

## Acceptance Criteria

- There is an "Auto-Cycle" toggle (checkbox or switch) visible in the Idle and Running states.
- When Auto-Cycle is ON and a focus session completes, the app immediately starts a break timer (5 min default) without user interaction.
- When Auto-Cycle is ON and a break timer completes, the app immediately starts the next focus session using the same duration as the previous focus session.
- A visible cycle counter and phase label ("Focus" / "Break") update in real time so the user always knows where they are in the sequence.
- When Auto-Cycle is OFF (or toggled off mid-session), the existing manual behavior is preserved: the completion dialog appears and the timer stays idle after dismissal.

## Out of Scope

- Configurable break duration per cycle (break is always 5 min in this sprint).
- Long-break logic after N cycles (classic Pomodoro 4-cycle pattern).
- Sound or system notification alerts.
- Persisting Auto-Cycle preference across app restarts.
- Any changes to the existing non-auto-cycle flow (dialog, Reset, Pause behavior).
