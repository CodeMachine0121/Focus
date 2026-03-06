package org.coding.afternoon.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeepFocusScreen(viewModel: DeepFocusViewModel) {
    LaunchedEffect(Unit) { viewModel.load() }

    val isActive = viewModel.isActive
    val settings = viewModel.settings
    val activeColor = Color(0xFFE53935)
    val inactiveColor = Color(0xFF546E7A)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isActive) activeColor else inactiveColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔕", fontSize = 48.sp)
                    Text(
                        "Deep Focus Mode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Color(0xFF69F0AE) else Color.White.copy(alpha = 0.5f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isActive) "ACTIVE" else "INACTIVE",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.toggleActive() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = if (isActive) activeColor else inactiveColor
                        )
                    ) {
                        Text(
                            if (isActive) "Deactivate" else "Activate Deep Focus",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Info Banner
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Text(
                    "⚡ When active: enables macOS Do Not Disturb, dims your display, " +
                        "and warns you about distracting apps.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        // Settings header
        item {
            Text(
                "⚙️ Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isActive) Color.Gray else Color.Unspecified
            )
        }

        // Settings card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Do Not Disturb", fontWeight = FontWeight.SemiBold)
                            Text("Silences macOS notifications", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = settings.isDndEnabled,
                            onCheckedChange = { viewModel.updateDndEnabled(it) },
                            enabled = !isActive
                        )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dim Display", fontWeight = FontWeight.SemiBold)
                            Text("Reduces screen brightness", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = settings.isDimEnabled,
                            onCheckedChange = { viewModel.updateDimEnabled(it) },
                            enabled = !isActive
                        )
                    }
                    if (settings.isDimEnabled) {
                        Text("Dim Level: ${(settings.dimLevel * 100).toInt()}%", fontSize = 13.sp)
                        Slider(
                            value = settings.dimLevel,
                            onValueChange = { viewModel.updateDimLevel(it) },
                            valueRange = 0.1f..0.8f,
                            enabled = !isActive,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Blocked apps section
        item {
            Text("🚫 Distracting Apps", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.newBlockedApp,
                    onValueChange = { viewModel.updateNewBlockedApp(it) },
                    label = { Text("App name") },
                    modifier = Modifier.weight(1f),
                    enabled = !isActive,
                    singleLine = true
                )
                Button(
                    onClick = { viewModel.addBlockedApp() },
                    enabled = !isActive
                ) {
                    Text("Add")
                }
            }
        }
        items(settings.blockedApps) { app ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("• $app")
                TextButton(
                    onClick = { viewModel.removeBlockedApp(app) },
                    enabled = !isActive
                ) {
                    Text("Remove", color = Color.Red)
                }
            }
        }
    }
}
