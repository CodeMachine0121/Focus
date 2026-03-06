# Sprint 15 — Engineering Design: Peer Accountability Network

## Architecture

```
PeerScreen (Compose UI)
    └── PeerViewModel (state holder, ViewModel)
            └── PeerNetworkService (background service)
                    ├── MulticastSocket (send loop, 30s interval)
                    └── MulticastSocket (receive loop, 5s timeout)
```

## PeerNetworkService

### UDP Multicast
- **Group**: `239.255.42.42` (administratively-scoped site-local)
- **Port**: `9877`
- `MulticastSocket(PORT)` bound on all interfaces
- `joinGroup(InetAddress.getByName(MULTICAST_GROUP))` on start
- `soTimeout = 5000ms` prevents blocking forever on receive

### Send Loop (Coroutine, Dispatchers.IO)
1. Build JSON message from current `FocusTimerViewModel` state
2. Create `DatagramPacket` to multicast group
3. `socket.send(packet)`
4. `delay(30_000)`
5. Repeat while coroutine active; all exceptions swallowed to prevent crash

### Receive Loop (Coroutine, Dispatchers.IO)
1. `socket.receive(packet)` — blocks up to 5s (soTimeout)
2. On `SocketTimeoutException`: run `cleanStalePeers()`, continue
3. On valid packet: parse JSON → `Peer?`
4. Filter own messages by UUID comparison
5. Update in-memory `peers: MutableMap<String, Peer>`
6. `withContext(Dispatchers.Main)` to notify listeners (Compose state update)

### Peer Expiry
- `Peer.lastSeen = System.currentTimeMillis()` set on every received packet
- `cleanStalePeers()` removes entries where `isStale` is true (`lastSeen > 90s ago`)
- Called on every timeout cycle and after every valid receive

### Persistence (java.util.prefs.Preferences)
- `peer_id`: stable UUID generated once, persisted across launches
- `display_name`: user's chosen display name

## PeerViewModel
- Wraps `PeerNetworkService`
- Exposes `peers: List<Peer>` as Compose `mutableStateOf`
- Exposes `isBroadcasting`, `nameInput`, `displayName` as state
- `startBroadcasting()` / `stopBroadcasting()` delegate to service

## JSON Parsing
No external JSON library — simple `String.substringAfter/Before` parsing:
```kotlin
val id = json.substringAfter("\"id\":\"").substringBefore("\"")
```
Sufficient for the controlled message format we generate ourselves.

## Threading Model
- All network I/O: `Dispatchers.IO`
- Compose state updates: `withContext(Dispatchers.Main)`
- `SupervisorJob` ensures one coroutine failure doesn't cancel others
- Socket closed in `stop()` to interrupt blocking receive

## Known Limitations
- Single `MulticastSocket` shared for send/receive (works on macOS; some restrictive corporate firewalls block multicast)
- No encryption (acceptable: LAN-only, no sensitive data beyond name and focus state)
- No peer authentication (by design: open trust model on private networks)
