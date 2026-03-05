package org.coding.afternoon.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun BreakCoachScreen(viewModel: BreakCoachViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val filteredActivities = viewModel.filteredActivities
    val completedIds = viewModel.todayCompletedIds
    val selectedCategory = viewModel.selectedCategory
    val shuffleHighlightIndex = viewModel.shuffleHighlightIndex
    val pendingBreakSuggestion = viewModel.pendingBreakSuggestion

    // Scroll to shuffled item
    LaunchedEffect(shuffleHighlightIndex) {
        if (shuffleHighlightIndex >= 0) {
            listState.animateScrollToItem(shuffleHighlightIndex)
        }
    }

    // Show break-phase snackbar suggestion
    LaunchedEffect(pendingBreakSuggestion) {
        val suggestion = pendingBreakSuggestion ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Break time! Try: ${suggestion.title} ${suggestion.emoji}",
            actionLabel = "Go",
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            val idx = filteredActivities.indexOf(suggestion)
            if (idx >= 0) {
                coroutineScope.launch { listState.animateScrollToItem(idx) }
            }
        }
        viewModel.clearBreakSuggestion()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.shuffle() },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Text("🔀", fontSize = 20.sp)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── Daily progress header ─────────────────────────────────────────
            DailyProgressHeader(
                completed = viewModel.completedCount,
                total = viewModel.totalCount,
            )

            Spacer(Modifier.height(4.dp))

            // ── Category filter chips ─────────────────────────────────────────
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectCategory(it) },
            )

            Spacer(Modifier.height(4.dp))

            // ── Activity list ─────────────────────────────────────────────────
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(
                    items = filteredActivities,
                    key = { _, activity -> activity.id },
                ) { index, activity ->
                    val isDone = completedIds.contains(activity.id)
                    val isHighlighted = index == shuffleHighlightIndex

                    ActivityCard(
                        activity = activity,
                        isDone = isDone,
                        isHighlighted = isHighlighted,
                        onDone = { viewModel.markDone(activity.id) },
                    )
                }

                // Extra bottom space so last card clears the FAB
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

// ── Daily Progress Header ─────────────────────────────────────────────────────

@Composable
private fun DailyProgressHeader(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Today's Break Activities",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$completed of $total done",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

// ── Category Filter Row ───────────────────────────────────────────────────────

@Composable
private fun CategoryFilterRow(
    selectedCategory: BreakCategory?,
    onSelectCategory: (BreakCategory?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("All") },
            )
        }
        items(BreakCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(category.displayName) },
            )
        }
    }
}

// ── Activity Card ─────────────────────────────────────────────────────────────

@Composable
private fun ActivityCard(
    activity: BreakActivity,
    isDone: Boolean,
    isHighlighted: Boolean,
    onDone: () -> Unit,
) {
    val highlightColor by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 600),
        label = "highlightBorder",
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlighted) Modifier.border(2.dp, highlightColor, RoundedCornerShape(8.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isDone) 0.dp else 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isDone)
                MaterialTheme.colorScheme.surfaceContainerLowest
            else
                MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Emoji icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .alpha(if (isDone) 0.45f else 1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxSize(),
                ) {}
                Text(
                    text = activity.emoji,
                    fontSize = 22.sp,
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    DurationBadge(durationSeconds = activity.durationSeconds, muted = isDone)
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isDone) 0.5f else 1f,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(8.dp))

                if (isDone) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("✓", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onDone,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text("Done", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

// ── Duration Badge ────────────────────────────────────────────────────────────

@Composable
private fun DurationBadge(durationSeconds: Int, muted: Boolean) {
    val label = when {
        durationSeconds < 60 -> "${durationSeconds}s"
        durationSeconds % 60 == 0 -> "${durationSeconds / 60}m"
        else -> "${durationSeconds / 60}m ${durationSeconds % 60}s"
    }
    val alpha = if (muted) 0.45f else 1f
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = alpha),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = alpha),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
