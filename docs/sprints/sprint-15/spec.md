# Sprint 15 — Product Spec: Peer Accountability Network

## User Stories

| ID | Story | Priority |
|---|---|---|
| P15-1 | As a user, I can enter a display name so peers can identify me on the network | Must |
| P15-2 | As a user, I can toggle broadcasting on/off so I control when my status is visible | Must |
| P15-3 | As a user, I can see a live list of peers on the same LAN with their current status | Must |
| P15-4 | As a user, I can see how long a peer has been focusing so I can join or respect their session | Should |
| P15-5 | As a user, stale peers (not seen for 90s) disappear automatically so the list stays accurate | Must |
| P15-6 | As a user, no data is sent outside my local network so my privacy is guaranteed | Must |

## Data Model

```kotlin
enum class PeerStatus { IDLE, FOCUSING, ON_BREAK }

data class Peer(
    val id: String,              // stable UUID, stored in Preferences
    val displayName: String,     // user-entered name
    val status: PeerStatus,      // current timer state
    val focusingForMinutes: Int, // elapsed focus minutes (0 if not focusing)
    val lastSeen: Long           // System.currentTimeMillis() of last received broadcast
) {
    val isStale: Boolean get() = System.currentTimeMillis() - lastSeen > 90_000L
}
```

## Network Protocol

| Property | Value |
|---|---|
| Transport | UDP multicast |
| Multicast group | `239.255.42.42` |
| Port | `9877` |
| Broadcast interval | Every 30 seconds |
| Peer expiry | 90 seconds since last message |

### Message Format (JSON, max ~256 bytes)
```json
{"id":"550e8400-e29b-41d4-a716-446655440000","name":"Alice","status":"FOCUSING","minutes":15}
```

### Status Values
- `"IDLE"` — timer not running
- `"FOCUSING"` — timer running in Focus phase
- `"ON_BREAK"` — timer running in Break phase

## Acceptance Criteria

- [ ] Toggling "Start Broadcasting" begins UDP multicast and shows broadcast label
- [ ] Display name field is editable only when not broadcasting
- [ ] Peers list updates within 30 seconds of a peer changing status
- [ ] Peers not heard from in 90+ seconds are removed from the list
- [ ] The user's own messages are ignored (filtered by UUID)
- [ ] Stopping broadcast clears the peers list immediately
- [ ] App compiles and navigation tab "🤝 Peers" is reachable
