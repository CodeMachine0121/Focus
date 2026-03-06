package org.coding.afternoon.focus

enum class PeerStatus { IDLE, FOCUSING, ON_BREAK }

data class Peer(
    val id: String,
    val displayName: String,
    val status: PeerStatus,
    val focusingForMinutes: Int = 0,
    val lastSeen: Long = System.currentTimeMillis()
) {
    val isStale: Boolean get() = System.currentTimeMillis() - lastSeen > 90_000L
    val statusEmoji: String get() = when (status) {
        PeerStatus.IDLE -> "😴"
        PeerStatus.FOCUSING -> "🔥"
        PeerStatus.ON_BREAK -> "☕"
    }
    val statusLabel: String get() = when (status) {
        PeerStatus.IDLE -> "Idle"
        PeerStatus.FOCUSING -> "Focusing (${focusingForMinutes}m)"
        PeerStatus.ON_BREAK -> "On Break"
    }
}
