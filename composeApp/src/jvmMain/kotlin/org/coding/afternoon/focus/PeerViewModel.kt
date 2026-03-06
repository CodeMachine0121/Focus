package org.coding.afternoon.focus

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class PeerViewModel(private val networkService: PeerNetworkService) : ViewModel() {
    var peers by mutableStateOf<List<Peer>>(emptyList())
        private set
    var isBroadcasting by mutableStateOf(false)
        private set
    var displayName by mutableStateOf(networkService.displayName)
    var nameInput by mutableStateOf(networkService.displayName)

    init {
        networkService.addPeerListener { updatedPeers ->
            peers = updatedPeers
        }
    }

    fun startBroadcasting() {
        if (isBroadcasting) return
        networkService.displayName = nameInput.trim().ifBlank { "Focus User" }
        displayName = networkService.displayName
        networkService.start()
        isBroadcasting = true
    }

    fun stopBroadcasting() {
        networkService.stop()
        isBroadcasting = false
        peers = emptyList()
    }

    fun updateNameInput(v: String) { nameInput = v }
}
