package org.coding.afternoon.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PeerScreen(viewModel: PeerViewModel) {
    val peers = viewModel.peers
    val isActive = viewModel.isBroadcasting

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Your profile card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👤 Your Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = viewModel.nameInput,
                        onValueChange = { viewModel.updateNameInput(it) },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isActive,
                        singleLine = true
                    )
                    Button(
                        onClick = { if (isActive) viewModel.stopBroadcasting() else viewModel.startBroadcasting() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFFE53935) else Color(0xFF43A047)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isActive) "🔴 Stop Broadcasting" else "🟢 Start Broadcasting")
                    }
                    if (isActive) {
                        Text(
                            "Broadcasting as: ${viewModel.displayName}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Peers section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🌐 Peers on Network (${peers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Empty state
        if (peers.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (isActive) "Searching for peers..." else "Start broadcasting to see peers",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Peer cards
        items(peers) { peer ->
            val statusColor = when (peer.status) {
                PeerStatus.FOCUSING -> Color(0xFF4CAF50)
                PeerStatus.ON_BREAK -> Color(0xFF2196F3)
                PeerStatus.IDLE -> Color.Gray
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(peer.statusEmoji, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(peer.displayName, fontWeight = FontWeight.SemiBold)
                            Text(peer.statusLabel, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Surface(color = statusColor, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            peer.status.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Info footer
        item {
            Text(
                "ℹ️ Peers are discovered automatically on your local network. No internet connection required.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
