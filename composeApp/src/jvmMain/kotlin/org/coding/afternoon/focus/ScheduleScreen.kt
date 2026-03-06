package org.coding.afternoon.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TealPrimary = Color(0xFF00796B)
private val TealLight = Color(0xFFB2DFDB)
private val TealDark = Color(0xFF004D40)

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            "📅 Smart Focus Scheduling",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TealDark
        )
        Spacer(Modifier.height(16.dp))

        AddScheduleCard(viewModel)

        Spacer(Modifier.height(20.dp))

        Text(
            "Upcoming Sessions",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TealDark
        )
        Spacer(Modifier.height(8.dp))

        if (viewModel.sessions.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.sessions, key = { it.id }) { session ->
                    SessionCard(session = session, onDelete = { viewModel.deleteSession(session.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleCard(viewModel: ScheduleViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TealLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add Schedule", fontWeight = FontWeight.SemiBold, color = TealDark)

            OutlinedTextField(
                value = viewModel.labelInput,
                onValueChange = { viewModel.updateLabel(it.take(60)) },
                label = { Text("Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    focusedLabelColor = TealPrimary,
                    cursorColor = TealPrimary
                )
            )

            // Time row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Time:", color = TealDark)
                StepperButton("-") { viewModel.updateHour(viewModel.hourInput - 1) }
                Text(
                    "%02d".format(viewModel.hourInput),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TealDark
                )
                Text(":", fontWeight = FontWeight.Bold, color = TealDark)
                Text(
                    "%02d".format(viewModel.minuteInput),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TealDark
                )
                StepperButton("+") { viewModel.updateHour(viewModel.hourInput + 1) }
                Spacer(Modifier.width(8.dp))
                StepperButton("-") { viewModel.updateMinute(viewModel.minuteInput - 1) }
                Text("min", fontSize = 12.sp, color = TealDark)
                StepperButton("+") { viewModel.updateMinute(viewModel.minuteInput + 1) }
            }

            // Duration row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Duration:", color = TealDark)
                StepperButton("-") { viewModel.updateDuration(viewModel.durationInput - 5) }
                Text("${viewModel.durationInput} min", fontWeight = FontWeight.Bold, color = TealDark)
                StepperButton("+") { viewModel.updateDuration(viewModel.durationInput + 5) }
            }

            // Recurring dropdown
            var expanded by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repeat:", color = TealDark)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.recurringType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().width(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            focusedLabelColor = TealPrimary
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        RecurringType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.updateRecurring(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.addSession() },
                enabled = viewModel.labelInput.isNotBlank(),
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Add Schedule")
            }
        }
    }
}

@Composable
private fun SessionCard(session: ScheduledSession, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time badge
            Surface(
                color = TealPrimary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    session.displayTime,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(session.label, fontWeight = FontWeight.SemiBold, color = TealDark)
                Text(
                    "${session.durationMinutes} min · ${session.recurringLabel}",
                    fontSize = 12.sp,
                    color = TealPrimary
                )
            }

            IconButton(onClick = onDelete) {
                Text("🗑", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📅", fontSize = 48.sp)
            Text("No scheduled sessions yet.", fontSize = 16.sp, color = Color.Gray)
            Text("Add one above to get started!", fontSize = 13.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}
