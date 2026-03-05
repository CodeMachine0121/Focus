package org.coding.afternoon.focus

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Pure functions for computing productivity analytics from a list of [SessionRecord]s.
 *
 * All functions are stateless and depend only on their inputs, making them
 * straightforward to unit-test without any Compose or Android dependencies.
 */
object StreakCalculator {

    private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ---- Group helpers --------------------------------------------------------

    /**
     * Groups records by calendar date, returning a map of LocalDate -> DayData.
     * Days with no records are not included.
     */
    fun groupByDay(records: List<SessionRecord>): Map<LocalDate, DayData> {
        return records
            .groupBy { LocalDate.parse(it.date, DATE_FMT) }
            .mapValues { (date, sessions) ->
                DayData(
                    date = date,
                    totalMinutes = sessions.sumOf { it.durationMinutes },
                    sessionCount = sessions.size,
                )
            }
    }

    /**
     * Returns [WeeklyBarData] for each day of the ISO week (Mon–Sun) containing [today].
     * Days with no sessions get minutes = 0.
     */
    fun groupByWeek(records: List<SessionRecord>, today: LocalDate = LocalDate.now()): List<WeeklyBarData> {
        val byDay = groupByDay(records)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        return (0..6).map { offset ->
            val day = weekStart.plusDays(offset.toLong())
            WeeklyBarData(
                dayLabel = labels[offset],
                minutes = byDay[day]?.totalMinutes ?: 0,
                isToday = day == today,
            )
        }
    }

    /**
     * Returns exactly 30 [HeatmapCell]s covering the last 30 calendar days
     * ending with [today] (inclusive). Cells with no sessions have sessionCount = 0.
     */
    fun getLast30Days(records: List<SessionRecord>, today: LocalDate = LocalDate.now()): List<HeatmapCell> {
        val byDay = groupByDay(records)
        val start = today.minusDays(29)
        return (0..29).map { offset ->
            val date = start.plusDays(offset.toLong())
            HeatmapCell(
                date = date,
                sessionCount = byDay[date]?.sessionCount ?: 0,
            )
        }
    }

    // ---- Streak calculation ---------------------------------------------------

    /**
     * Calculates the current streak: the number of consecutive calendar days
     * ending on or including [today] that each have at least one session.
     *
     * If today has no sessions but yesterday does, the streak is still counted
     * (streak is "at risk" but not yet broken). This matches common streak
     * semantics (the user has until midnight to maintain it).
     */
    fun calculateCurrentStreak(records: List<SessionRecord>, today: LocalDate = LocalDate.now()): Int {
        if (records.isEmpty()) return 0

        val activeDays: Set<LocalDate> = records
            .map { LocalDate.parse(it.date, DATE_FMT) }
            .toSet()

        // Start from today; if today has no session, check if yesterday does
        // to handle the "streak at risk" grace period.
        val checkStart = when {
            activeDays.contains(today) -> today
            activeDays.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        var current = checkStart
        while (activeDays.contains(current)) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }

    /**
     * Calculates the all-time longest streak: the maximum number of consecutive
     * calendar days each with at least one session, over all recorded history.
     */
    fun calculateLongestStreak(records: List<SessionRecord>): Int {
        if (records.isEmpty()) return 0

        val sortedDays: List<LocalDate> = records
            .map { LocalDate.parse(it.date, DATE_FMT) }
            .toSortedSet()
            .toList()

        var longest = 1
        var current = 1

        for (i in 1 until sortedDays.size) {
            val gap = ChronoUnit.DAYS.between(sortedDays[i - 1], sortedDays[i])
            if (gap == 1L) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    // ---- Personal records ----------------------------------------------------

    /**
     * Computes [PersonalRecords] from the full session list.
     * Returns zeros/nulls when [records] is empty.
     */
    fun getPersonalRecords(records: List<SessionRecord>): PersonalRecords {
        if (records.isEmpty()) {
            return PersonalRecords(
                longestSessionMinutes = 0,
                bestDayMinutes = 0,
                bestDayDate = null,
            )
        }

        val longestSession = records.maxOf { it.durationMinutes }

        val byDay = groupByDay(records)
        val bestDay = byDay.values.maxByOrNull { it.totalMinutes }

        return PersonalRecords(
            longestSessionMinutes = longestSession,
            bestDayMinutes = bestDay?.totalMinutes ?: 0,
            bestDayDate = bestDay?.date?.format(DATE_FMT),
        )
    }

    // ---- Top-level stats assembly ---------------------------------------------

    /**
     * Computes a complete [DashboardStats] snapshot from the given [records].
     * This is the single entry point called by [DashboardViewModel].
     */
    fun compute(records: List<SessionRecord>, today: LocalDate = LocalDate.now()): DashboardStats {
        return DashboardStats(
            currentStreak = calculateCurrentStreak(records, today),
            longestStreak = calculateLongestStreak(records),
            totalSessions = records.size,
            totalMinutes = records.sumOf { it.durationMinutes },
            weeklyData = groupByWeek(records, today),
            heatmapData = getLast30Days(records, today),
            personalRecords = getPersonalRecords(records),
        )
    }
}
