# Sprint 4 Stakeholder Requirements

## User Problem

Users have no way to label what they are focusing on during a session, so completed sessions feel anonymous and disposable. Without a session label, the timer provides no sense of intentionality or personal ownership.

## Proposed Feature: Session Label (Focus Intent)

Before starting a timer, the user can type a short label describing what they intend to work on (e.g., "Write chapter 3", "Code review", "Deep work block"). The label is displayed prominently inside or beneath the countdown ring while the timer runs and is shown in the completion dialog so the user gets a satisfying acknowledgment of what they finished.

The label field is optional — leaving it blank works exactly as today.

## Acceptance Criteria

- A text input field labeled "What are you focusing on?" is visible on the FocusScreen when the timer is Idle.
- When the timer starts, the label (if non-empty) is displayed below the countdown ring while the timer is Running or Paused.
- The completion AlertDialog shows the session label in its body text (e.g., "You completed: Write chapter 3") when a label was entered; falls back to the generic "Your focus session is complete." message when no label was provided.
- The label input is cleared (reset to empty) when the user resets the timer or dismisses the completion dialog.
- The label field is disabled (not editable) while the timer is Running or Paused.

## Out of Scope

- Persisting labels to disk or showing label history.
- Enforcing a minimum or maximum label length beyond a reasonable UI character limit (we cap at 60 chars in the input but do not validate server-side).
- Tagging or categorizing sessions.
- Any analytics or statistics on labeled vs. unlabeled sessions.
