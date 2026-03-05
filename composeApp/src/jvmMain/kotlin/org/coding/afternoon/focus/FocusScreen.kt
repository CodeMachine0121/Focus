package org.coding.afternoon.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FocusScreen(viewModel: FocusTimerViewModel) {
    val state = viewModel.timerState
    val remaining = viewModel.remainingSeconds
    val progress = viewModel.progress
    val phase = viewModel.currentPhase
    val cycleCount = viewModel.cycleCount
    val autoCycleEnabled = viewModel.autoCycleEnabled

    val minutes = remaining / 60
    val seconds = remaining % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    var customInput by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state == TimerState.Idle) customInput = ""
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            val ringColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.size(220.dp)) {
                val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                val inset = 8.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
            Text(
                text = timeText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Phase label and cycle counter (shown when auto-cycle is active)
        if (autoCycleEnabled || (state != TimerState.Idle && cycleCount > 1)) {
            val phaseLabel = if (phase == TimerPhase.Focus) "Focus" else "Break"
            val cycleLabel = "Cycle $cycleCount"
            Text(
                text = "$cycleLabel \u2014 $phaseLabel",
                fontSize = 14.sp,
                color = if (phase == TimerPhase.Break)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Auto-Cycle toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Auto-Cycle", fontSize = 14.sp)
            Switch(
                checked = autoCycleEnabled,
                onCheckedChange = { viewModel.autoCycleEnabled = it },
                enabled = state != TimerState.Completed,
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(25, 15, 5).forEach { mins ->
                OutlinedButton(
                    onClick = { viewModel.setDuration(mins) },
                    enabled = state == TimerState.Idle,
                ) {
                    Text("${mins}m")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = customInput,
            onValueChange = { value ->
                val filtered = value.filter { it.isDigit() }.take(3)
                customInput = filtered
                filtered.toIntOrNull()?.let { viewModel.setDuration(it) }
            },
            label = { Text("Custom (min)") },
            enabled = state == TimerState.Idle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(160.dp),
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state) {
                TimerState.Idle, TimerState.Completed -> {
                    Button(onClick = { viewModel.start() }) { Text("Start") }
                }
                TimerState.Running -> {
                    Button(onClick = { viewModel.pause() }) { Text("Pause") }
                }
                TimerState.Paused -> {
                    Button(onClick = { viewModel.start() }) { Text("Resume") }
                }
            }
            OutlinedButton(
                onClick = { viewModel.reset() },
                enabled = state != TimerState.Idle,
            ) {
                Text("Reset")
            }
        }
    }

    if (state == TimerState.Completed) {
        AlertDialog(
            onDismissRequest = { viewModel.dismiss() },
            title = { Text("Time's up!") },
            text = { Text("Your focus session is complete.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismiss() }) { Text("OK") }
            }
        )
    }
    }
}
