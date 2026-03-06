package org.coding.afternoon.focus

import kotlinx.coroutines.*
import java.net.*
import java.util.prefs.Preferences

class PeerNetworkService(private val timerViewModel: FocusTimerViewModel) {
    companion object {
        private const val MULTICAST_GROUP = "239.255.42.42"
        private const val PORT = 9877
        private const val BROADCAST_INTERVAL_MS = 30_000L
    }

    private val prefs = Preferences.userNodeForPackage(PeerNetworkService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var sendJob: Job? = null
    private var receiveJob: Job? = null
    private var socket: MulticastSocket? = null

    val selfId: String = prefs.get("peer_id", java.util.UUID.randomUUID().toString().also {
        prefs.put("peer_id", it)
    })

    var displayName: String
        get() = prefs.get("display_name", System.getProperty("user.name") ?: "Focus User")
        set(value) { prefs.put("display_name", value) }

    private val peerListeners = mutableListOf<(List<Peer>) -> Unit>()
    private val peers = mutableMapOf<String, Peer>()

    fun addPeerListener(listener: (List<Peer>) -> Unit) { peerListeners.add(listener) }

    fun start() {
        try {
            socket = MulticastSocket(PORT).apply {
                val group = InetSocketAddress(InetAddress.getByName(MULTICAST_GROUP), PORT)
                val ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost())
                joinGroup(group, ni)
                soTimeout = 5000
            }
            startReceiving()
            startBroadcasting()
        } catch (_: Exception) {}
    }

    fun stop() {
        sendJob?.cancel()
        receiveJob?.cancel()
        try { socket?.close() } catch (_: Exception) {}
        socket = null
    }

    private fun buildMessage(): String {
        val status = when {
            timerViewModel.timerState == TimerState.Running && timerViewModel.currentPhase == TimerPhase.Focus -> "FOCUSING"
            timerViewModel.timerState == TimerState.Running && timerViewModel.currentPhase == TimerPhase.Break -> "ON_BREAK"
            else -> "IDLE"
        }
        val elapsed = (timerViewModel.totalSeconds - timerViewModel.remainingSeconds) / 60
        return """{"id":"$selfId","name":"${displayName.replace("\"", "")}","status":"$status","minutes":$elapsed}"""
    }

    private fun parseMessage(json: String): Peer? {
        return try {
            val id = json.substringAfter("\"id\":\"").substringBefore("\"")
            val name = json.substringAfter("\"name\":\"").substringBefore("\"")
            val statusStr = json.substringAfter("\"status\":\"").substringBefore("\"")
            val minutes = json.substringAfter("\"minutes\":").substringBefore("}").trim().toIntOrNull() ?: 0
            val status = when (statusStr) {
                "FOCUSING" -> PeerStatus.FOCUSING
                "ON_BREAK" -> PeerStatus.ON_BREAK
                else -> PeerStatus.IDLE
            }
            if (id == selfId) null else Peer(id, name, status, minutes)
        } catch (_: Exception) { null }
    }

    private fun startBroadcasting() {
        sendJob = scope.launch {
            while (isActive) {
                try {
                    val msg = buildMessage().toByteArray()
                    val packet = DatagramPacket(msg, msg.size, InetAddress.getByName(MULTICAST_GROUP), PORT)
                    socket?.send(packet)
                } catch (_: Exception) {}
                delay(BROADCAST_INTERVAL_MS)
            }
        }
    }

    private fun startReceiving() {
        receiveJob = scope.launch {
            val buf = ByteArray(1024)
            while (isActive) {
                try {
                    val packet = DatagramPacket(buf, buf.size)
                    socket?.receive(packet)
                    val msg = String(packet.data, 0, packet.length)
                    parseMessage(msg)?.let { peer ->
                        peers[peer.id] = peer
                        cleanStalePeers()
                        val current = peers.values.filter { !it.isStale }.toList()
                        withContext(Dispatchers.Main) { peerListeners.forEach { it(current) } }
                    }
                } catch (_: SocketTimeoutException) {
                    cleanStalePeers()
                } catch (_: Exception) {}
            }
        }
    }

    private fun cleanStalePeers() {
        peers.entries.removeIf { it.value.isStale }
    }
}
