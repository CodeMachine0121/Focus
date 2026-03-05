package org.coding.afternoon.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

// ---- Top-level entry point ---------------------------------------------------

/**
 * Dashboard tab screen. Expects a [DashboardViewModel] constructed with the
 * shared [SessionRepository].
 *
 * Integration: in App.kt add a "Dashboard" tab and render:
 *   `2 -> DashboardScreen(remember { DashboardViewModel(repository) })`
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val stats = viewModel.stats
    val isLoading = viewModel.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        when {
            isLoading || stats == null -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            stats.totalSessions == 0 -> EmptyState()
            else -> DashboardContent(stats)
        }
    }
}

// ---- Empty state -------------------------------------------------------------

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No focus sessions yet.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Complete your first Pomodoro to start\ntracking your productivity!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ---- Dashboard content -------------------------------------------------------

@Composable
private fun DashboardContent(stats: DashboardStats) {
    StatsCardsRow(stats)
    SectionTitle("This Week")
    WeeklyBarChart(stats.weeklyData)
    SectionTitle("Last 30 Days")
    HeatmapGrid(stats.heatmapData)
    SectionTitle("Personal Records")
    PersonalRecordsRow(stats.personalRecords)
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ---- Stats cards row ---------------------------------------------------------

@Composable
private fun StatsCardsRow(stats: DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Streak",
            value = "${stats.currentStreak}",
            subLabel = "days",
            highlighted = stats.currentStreak >= 3,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Sessions",
            value = "${stats.totalSessions}",
            subLabel = "all-time",
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Minutes",
            value = formatNumber(stats.totalMinutes),
            subLabel = "focused",
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subLabel: String,
    highlighted: Boolean = false,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlighted)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary,
            )
            Text(
                text = subLabel,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---- Weekly bar chart --------------------------------------------------------

@Composable
private fun WeeklyBarChart(weeklyData: List<WeeklyBarData>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            ) {
                drawWeeklyBars(
                    weeklyData = weeklyData,
                    primaryColor = primaryColor,
                    primaryContainerColor = primaryContainerColor,
                    labelColor = labelColor,
                    textMeasurer = textMeasurer,
                )
            }
            // Day labels below the canvas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                weeklyData.forEach { bar ->
                    Text(
                        text = bar.dayLabel.take(1),
                        fontSize = 10.sp,
                        color = if (bar.isToday)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawWeeklyBars(
    weeklyData: List<WeeklyBarData>,
    primaryColor: Color,
    primaryContainerColor: Color,
    labelColor: Color,
    textMeasurer: TextMeasurer,
) {
    val count = weeklyData.size
    if (count == 0) return

    val maxMinutes = weeklyData.maxOf { it.minutes }.coerceAtLeast(1)
    val canvasWidth = size.width
    val canvasHeight = size.height
    val columnWidth = canvasWidth / count
    val barWidth = columnWidth * 0.55f
    val topPadding = 20f
    val maxBarHeight = canvasHeight - topPadding
    val minBarHeight = 2f

    weeklyData.forEachIndexed { index, bar ->
        val barHeight = if (bar.minutes > 0)
            (bar.minutes.toFloat() / maxMinutes) * maxBarHeight
        else
            minBarHeight

        val left = index * columnWidth + (columnWidth - barWidth) / 2f
        val top = canvasHeight - barHeight
        val barColor = if (bar.isToday) primaryColor else primaryContainerColor

        drawRoundRect(
            color = barColor,
            topLeft = Offset(left, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )

        // Value label above bar
        if (bar.minutes > 0) {
            val label = "${bar.minutes}"
            val measured = textMeasurer.measure(
                text = label,
                style = TextStyle(fontSize = 9.sp, color = labelColor),
            )
            val textX = left + (barWidth - measured.size.width) / 2f
            val textY = top - measured.size.height - 2f
            if (textY >= 0f) {
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(textX, textY),
                )
            }
        }
    }
}

// ---- 30-day heatmap ----------------------------------------------------------

@Composable
private fun HeatmapGrid(heatmapData: List<HeatmapCell>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val today = remember { LocalDate.now() }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val availableWidthDp: Dp = maxWidth
                val columns = 6
                val rows = 5
                val gapDp = 4.dp
                val cellSizeDp = (availableWidthDp - gapDp * (columns - 1)) / columns
                val gridHeightDp = cellSizeDp * rows + gapDp * (rows - 1)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeightDp),
                ) {
                    val cellSize = cellSizeDp.toPx()
                    val gap = gapDp.toPx()
                    val cornerRadius = CornerRadius(4.dp.toPx())

                    heatmapData.forEachIndexed { index, cell ->
                        val col = index % columns
                        val row = index / columns
                        val left = col * (cellSize + gap)
                        val top = row * (cellSize + gap)

                        val fillColor = intensityColor(cell.sessionCount, primaryColor, surfaceVariantColor)
                        drawRoundRect(
                            color = fillColor,
                            topLeft = Offset(left, top),
                            size = Size(cellSize, cellSize),
                            cornerRadius = cornerRadius,
                        )

                        if (cell.date == today) {
                            drawRoundRect(
                                color = outlineColor,
                                topLeft = Offset(left, top),
                                size = Size(cellSize, cellSize),
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Less",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0, 1, 2, 3, 4).forEach { level ->
                    val cellColor = intensityColor(
                        level,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawRoundRect(
                            color = cellColor,
                            cornerRadius = CornerRadius(2.dp.toPx()),
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = "More",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun intensityColor(sessionCount: Int, primaryColor: Color, surfaceVariantColor: Color): Color {
    return when {
        sessionCount <= 0 -> surfaceVariantColor
        sessionCount == 1 -> primaryColor.copy(alpha = 0.25f)
        sessionCount == 2 -> primaryColor.copy(alpha = 0.50f)
        sessionCount == 3 -> primaryColor.copy(alpha = 0.75f)
        else -> primaryColor
    }
}

// ---- Personal records --------------------------------------------------------

@Composable
private fun PersonalRecordsRow(records: PersonalRecords) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecordCard(
            modifier = Modifier.weight(1f),
            label = "Longest Session",
            value = if (records.longestSessionMinutes > 0) "${records.longestSessionMinutes} min" else "--",
        )
        RecordCard(
            modifier = Modifier.weight(1f),
            label = "Best Day",
            value = if (records.bestDayMinutes > 0) "${records.bestDayMinutes} min" else "--",
            subValue = records.bestDayDate,
        )
    }
}

@Composable
private fun RecordCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subValue: String? = null,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ---- Utility -----------------------------------------------------------------

private fun formatNumber(value: Int): String {
    return if (value >= 1000) {
        val thousands = value / 1000
        val remainder = value % 1000
        "${thousands},${remainder.toString().padStart(3, '0')}"
    } else {
        value.toString()
    }
}
