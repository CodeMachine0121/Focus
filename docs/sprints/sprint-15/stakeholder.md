# Sprint 15 — Stakeholder Brief: Peer Accountability Network

## User Problem
Remote workers and distributed study groups want to maintain shared focus rituals — the same way co-located teams hold each other accountable through visible presence. Today there is no way to see whether a teammate is currently focusing, on break, or idle without sending a message and interrupting them. Existing solutions require cloud accounts, subscriptions, or complex setup.

## Proposed Feature: Peer Accountability Network
A **zero-configuration, privacy-first, LAN-only** peer discovery and status-sharing system built directly into the Focus app.

### Core Capabilities
| Capability | Description |
|---|---|
| Zero-config discovery | Detect other Focus users on the same Wi-Fi/LAN with no manual IP entry |
| Real-time status | See peers' current state: Idle / Focusing / On Break |
| Focus duration | Know how long a peer has been in a focus session |
| Opt-in broadcasting | Users choose to share their status; off by default |
| Privacy-first | Absolutely no data leaves the local network; no accounts; no cloud |

## Story Points
**13 (Fibonacci)** — significant network infrastructure, background service lifecycle, and UI work.

## Success Criteria
1. Two macOS machines on the same LAN can see each other's focus status within 60 seconds of both starting broadcasts.
2. Stopping broadcast immediately removes the user from peers' view (within one 90-second expiry cycle).
3. The feature is entirely opt-in — the app works normally if the user never touches the Peers tab.
4. No external services, accounts, or internet connection are required.
