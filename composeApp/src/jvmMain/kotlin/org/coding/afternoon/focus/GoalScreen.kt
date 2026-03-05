package org.coding.afternoon.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Top-level screen ----

@Composable
fun GoalScreen(viewModel: GoalViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!viewModel.atLimit) showAddDialog = true },
                containerColor = if (viewModel.atLimit)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    color = if (viewModel.atLimit)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Progress section
            DailyProgressSection(
                completedCount = viewModel.completedCount,
                totalCount = viewModel.totalCount,
                progress = viewModel.progress,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Limit banner
            if (viewModel.atLimit) {
                Text(
                    text = "Goal limit reached (${GoalRepository.MAX_GOALS}/${GoalRepository.MAX_GOALS})",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Goal list or empty state
            val goals = viewModel.goals
            if (goals.isEmpty()) {
                GoalEmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onComplete = { viewModel.markComplete(goal.id) },
                            onDelete = { viewModel.deleteGoal(goal.id) },
                        )
                    }
                    // Bottom padding so the last card isn't hidden by the FAB
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, pomodoros ->
                viewModel.addGoal(title, pomodoros)
                showAddDialog = false
            },
        )
    }
}

// ---- Progress bar section ----

@Composable
private fun DailyProgressSection(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today's Goals",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$completedCount / $totalCount",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// ---- Goal card ----

@Composable
private fun GoalCard(
    goal: DailyGoal,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardColors = if (goal.completed) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Title + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (goal.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (goal.completed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
                val pomodoroLabel = if (goal.estimatedPomodoros == 1) "1 pomodoro" else "${goal.estimatedPomodoros} pomodoros"
                Text(
                    text = pomodoroLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Complete button
            IconButton(onClick = { if (!goal.completed) onComplete() }) {
                if (goal.completed) {
                    Text("✅", fontSize = 20.sp)
                } else {
                    Text("⭕", fontSize = 20.sp)
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Text("🗑", fontSize = 18.sp)
            }
        }
    }
}

// ---- Empty state ----

@Composable
private fun GoalEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No goals yet.",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Tap + to plan your day.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---- Add Goal dialog ----

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, estimatedPomodoros: Int) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var pomodoros by remember { mutableStateOf(1) }

    val isConfirmEnabled = title.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 60) title = it },
                    label = { Text("Goal title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${title.length} / 60",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Estimated Pomodoros",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { if (pomodoros > 1) pomodoros-- },
                        enabled = pomodoros > 1,
                    ) {
                        Text(
                            text = "−",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "$pomodoros",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.widthIn(min = 24.dp),
                    )
                    IconButton(
                        onClick = { if (pomodoros < 10) pomodoros++ },
                        enabled = pomodoros < 10,
                    ) {
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isConfirmEnabled) onConfirm(title.trim(), pomodoros) },
                enabled = isConfirmEnabled,
            ) {
                Text("Add Goal")
            }
        },
    )
}
