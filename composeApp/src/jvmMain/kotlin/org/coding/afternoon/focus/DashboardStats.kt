package org.coding.afternoon.focus

import java.time.LocalDate

/**
 * Represents focus activity for a single calendar day.
 */
data class DayData(
    val date: LocalDate,
    val totalMinutes: Int,
    val sessionCount: Int,
)

/**
 * Data for a single bar in the weekly bar chart.
 */
data class WeeklyBarData(
    val dayLabel: String,
    val minutes: Int,
    val isToday: Boolean,
)

/**
 * Data for a single cell in the 30-day heatmap.
 */
data class HeatmapCell(
    val date: LocalDate,
    val sessionCount: Int,
)

/**
 * Personal achievement records derived from all-time session history.
 */
data class PersonalRecords(
    val longestSessionMinutes: Int,
    val bestDayMinutes: Int,
    val bestDayDate: String?,
)

/**
 * Aggregated analytics state powering the Dashboard screen.
 */
data class DashboardStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalSessions: Int,
    val totalMinutes: Int,
    val weeklyData: List<WeeklyBarData>,
    val heatmapData: List<HeatmapCell>,
    val personalRecords: PersonalRecords,
)
