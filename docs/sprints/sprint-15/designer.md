# Sprint 15 — Design Spec: Peer Accountability Network

## Screen: PeerScreen

### Layout Overview
```
┌─────────────────────────────────────────┐
│  👤 Your Profile                        │
│  ┌─────────────────────────────────┐    │
│  │ Display Name  [Alice          ] │    │
│  └─────────────────────────────────┘    │
│  [ 🟢 Start Broadcasting           ]    │
│                                         │
├─────────────────────────────────────────┤
│  🌐 Peers on Network (2)                │
│                                         │
│  ┌─────────────────────────────────┐    │
│  │ 🔥 Bob          [FOCUSING] green│    │
│  │    Focusing (12m)               │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ ☕ Carol        [ON_BREAK] blue │    │
│  │    On Break                     │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ℹ️ Peers are discovered automatically… │
└─────────────────────────────────────────┘
```

### Component Details

#### Your Profile Card
- **Background**: Light green (`0xFFE8F5E9`) when broadcasting, off-white (`0xFFFAFAFA`) when private
- **Name field**: `OutlinedTextField`, disabled while broadcasting to prevent mid-session name changes
- **Toggle button**: Full-width, green (`0xFF43A047`) when stopped, red (`0xFFE53935`) when active
- **Broadcast label**: Small gray text showing "Broadcasting as: {name}" when active

#### Peers List
- Section header: bold 18sp with live count `🌐 Peers on Network (N)`
- Each peer rendered as a `Card` with horizontal layout:
  - Left: status emoji (24sp) + name (SemiBold) + status label (12sp gray)
  - Right: colored pill badge with status name

#### Status Colors
| Status | Emoji | Badge Color |
|---|---|---|
| `IDLE` | 😴 | Gray |
| `FOCUSING` | 🔥 | `#4CAF50` (green) |
| `ON_BREAK` | ☕ | `#2196F3` (blue) |

#### Empty State
- Centered within a Card
- Large search emoji 🔍 (32sp)
- Context-sensitive message:
  - Not broadcasting: "Start broadcasting to see peers"
  - Broadcasting: "Searching for peers..."

#### Footer
- 12sp gray italic info text explaining LAN-only nature

### Interaction Design
- All state changes are immediate (no loading spinners needed for toggle)
- Name field auto-trims whitespace on broadcast start
- Blank name defaults to "Focus User"
