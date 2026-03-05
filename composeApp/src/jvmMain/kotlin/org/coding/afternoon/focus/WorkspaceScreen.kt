package org.coding.afternoon.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Workspace screen root
// ---------------------------------------------------------------------------

/**
 * Root composable for the Workspace tab.
 *
 * Displays a 2-column [LazyVerticalGrid] of [WorkspaceTimerCard]s,
 * an "Add Timer" button, and conditionally shows [AddTimerDialog].
 *
 * This composable is entirely self-contained and does not share state with
 * [FocusTimerViewModel] or [FocusScreen].
 *
 * Integration: add to App.kt as:
 *   2 -> WorkspaceScreen(workspaceViewModel)
 */
@Composable
fun WorkspaceScreen(viewModel: WorkspaceViewModel) {
    val timers = viewModel.timers
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (timers.isEmpty()) {
                // Empty-state placeholder
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No timers yet",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Add up to 6 independent countdown timers",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(timers, key = { it.id }) { timer ->
                        WorkspaceTimerCard(
                            timer = timer,
                            onStart  = { viewModel.startTimer(timer.id) },
                            onPause  = { viewModel.pauseTimer(timer.id) },
                            onReset  = { viewModel.resetTimer(timer.id) },
                            onRemove = { viewModel.removeTimer(timer.id) }
                        )
                    }
                }
            }

            // "Add Timer" button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val atLimit = timers.size >= WorkspaceViewModel.MAX_TIMERS
                Button(
                    onClick = { showAddDialog = true },
                    enabled = !atLimit
                ) {
                    Text(
                        text = if (atLimit) "Maximum 6 timers reached" else "+ Add Timer"
                    )
                }
            }
        }

        if (showAddDialog) {
            AddTimerDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { label, minutes ->
                    viewModel.addTimer(label, minutes)
                    showAddDialog = false
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Individual timer card
// ---------------------------------------------------------------------------

/**
 * Displays a single [WorkspaceTimer] as a card with:
 * - Label text
 * - Circular progress arc + MM:SS countdown
 * - "Done" completion badge (visible only when Completed)
 * - Start / Pause / Resume, Reset, and Remove controls
 */
@Composable
fun WorkspaceTimerCard(
    timer: WorkspaceTimer,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onRemove: () -> Unit
) {
    val isCompleted = timer.state == WorkspaceTimerState.Completed
    val cardBackground = if (isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label
            Text(
                text = timer.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Progress arc + countdown
            TimerArc(timer = timer)

            Spacer(Modifier.height(6.dp))

            // Completion badge
            if (isCompleted) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = "Done",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary action button (Start / Pause / Resume) — hidden when Completed
                when (timer.state) {
                    WorkspaceTimerState.Idle -> {
                        Button(onClick = onStart, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Start", fontSize = 12.sp)
                        }
                    }
                    WorkspaceTimerState.Running -> {
                        Button(onClick = onPause, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Pause", fontSize = 12.sp)
                        }
                    }
                    WorkspaceTimerState.Paused -> {
                        Button(onClick = onStart, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Resume", fontSize = 12.sp)
                        }
                    }
                    WorkspaceTimerState.Completed -> {
                        // No primary action for completed timers — only Reset and Remove
                    }
                }

                // Reset button
                OutlinedButton(
                    onClick = onReset,
                    enabled = timer.state != WorkspaceTimerState.Idle,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Reset", fontSize = 12.sp)
                }

                // Remove button
                OutlinedButton(
                    onClick = onRemove,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("X", fontSize = 12.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Progress arc with centred countdown
// ---------------------------------------------------------------------------

/**
 * Draws a circular progress arc on a [Canvas] with the MM:SS time centered inside.
 * Arc color changes based on [WorkspaceTimer.state].
 */
@Composable
fun TimerArc(timer: WorkspaceTimer) {
    val progressColor = when (timer.state) {
        WorkspaceTimerState.Idle      -> MaterialTheme.colorScheme.primary
        WorkspaceTimerState.Running   -> MaterialTheme.colorScheme.primary
        WorkspaceTimerState.Paused    -> MaterialTheme.colorScheme.secondary
        WorkspaceTimerState.Completed -> Color(0xFF4CAF50)
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val minutes = timer.remainingSeconds / 60
    val seconds = timer.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val strokeWidth = 8.dp.toPx()
            val inset = strokeWidth / 2f
            val arcTopLeft = Offset(inset, inset)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            // Track (full circle)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = stroke
            )

            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * timer.progress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = stroke
            )
        }

        Text(
            text = timeText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------------------------------------------------------------------------
// Add Timer Dialog
// ---------------------------------------------------------------------------

/**
 * Modal dialog for creating a new workspace timer.
 *
 * Validates that the duration is a positive integer before enabling the
 * "Add" confirmation button. Label is optional and defaults to "Timer N"
 * inside [WorkspaceViewModel.addTimer].
 *
 * @param onDismiss Called when the user cancels or closes without adding.
 * @param onAdd     Called with the (label, minutes) pair when the user confirms.
 */
@Composable
fun AddTimerDialog(
    onDismiss: () -> Unit,
    onAdd: (label: String, minutes: Int) -> Unit
) {
    var labelInput by remember { mutableStateOf("") }
    var durationInput by remember { mutableStateOf("") }

    val durationMinutes = durationInput.toIntOrNull() ?: 0
    val canAdd = durationMinutes > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Timer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = labelInput,
                    onValueChange = { labelInput = it.take(60) },
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationInput,
                    onValueChange = { value ->
                        durationInput = value.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = durationInput.isNotEmpty() && !canAdd
                )
                if (durationInput.isNotEmpty() && !canAdd) {
                    Text(
                        text = "Duration must be at least 1 minute",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(labelInput.trim(), durationMinutes) },
                enabled = canAdd
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
