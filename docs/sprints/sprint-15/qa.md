# Sprint 15 — QA Report: Peer Accountability Network

## Build Results

| Step | Result |
|---|---|
| `compileKotlinJvm` (JBR-21.0.9) | ✅ BUILD SUCCESSFUL |
| Warnings | 0 (deprecated `joinGroup(InetAddress)` fixed to `joinGroup(InetSocketAddress, NetworkInterface)`) |
| Errors | 0 |

> Note: The default system JDK (OpenJDK 25.0.2 Homebrew) triggers a Kotlin compiler bug with version string parsing. Build must use JBR-21.0.9 or the project's `org.gradle.java.home` override.

## QA Checklist

### Navigation
- [x] "🤝 Peers" tab appears as 8th item in NavigationRail
- [x] Tab renders `PeerScreen` correctly (tab index 7)

### Your Profile Card
- [x] Display Name field is editable when not broadcasting
- [x] Display Name field is disabled while broadcasting
- [x] Blank name defaults to "Focus User" on broadcast start
- [x] Button label switches: "🟢 Start Broadcasting" ↔ "🔴 Stop Broadcasting"
- [x] Card background turns green (0xFFE8F5E9) when broadcasting
- [x] "Broadcasting as: {name}" label appears when active

### Peers List
- [x] Header shows live count: "🌐 Peers on Network (N)"
- [x] Empty state card shown when peers list is empty
- [x] Empty state message changes based on broadcast state
- [x] Each peer card displays: emoji, name, status label, colored badge
- [x] FOCUSING badge: green (#4CAF50)
- [x] ON_BREAK badge: blue (#2196F3)
- [x] IDLE badge: gray

### Network Service (manual/integration)
- [ ] Two machines on same LAN discover each other within 30s
- [ ] Status updates propagate within one 30s cycle
- [ ] Stale peers (90s) removed automatically
- [ ] User's own broadcast message is ignored (UUID filter)
- [ ] Stop broadcasting clears the peer list immediately
- [ ] App continues functioning normally if network is unavailable

### Privacy
- [x] No internet calls — UDP multicast is LAN-layer only
- [x] No accounts or credentials
- [x] Feature is completely opt-in (inactive until "Start Broadcasting" pressed)

## Known Issues / Follow-ups
- Multicast may be blocked on some corporate or managed Wi-Fi networks (expected limitation, documented in rd.md)
- `NetworkInterface.getByInetAddress(InetAddress.getLocalHost())` may return `null` on some multi-homed setups — wrapped in try/catch so service gracefully fails to start rather than crashing
