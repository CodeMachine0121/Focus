package org.coding.afternoon.focus

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ProductivityReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalSessions: Int,
    val totalMinutes: Int,
    val avgSessionMinutes: Double,
    val bestDay: String,
    val bestDayMinutes: Int,
    val dailyBreakdown: Map<String, Int>
) {
    val totalHours: Double get() = totalMinutes / 60.0
    val formattedText: String get() = buildString {
        val fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        appendLine("═══════════════════════════════════════")
        appendLine("       FOCUS PRODUCTIVITY REPORT       ")
        appendLine("═══════════════════════════════════════")
        appendLine("Period: ${startDate.format(fmt)} → ${endDate.format(fmt)}")
        appendLine("───────────────────────────────────────")
        appendLine("Total Sessions  : $totalSessions")
        appendLine("Total Focus Time: ${"%.1f".format(totalHours)} hours ($totalMinutes min)")
        appendLine("Avg Session     : ${"%.0f".format(avgSessionMinutes)} min")
        appendLine("Best Day        : $bestDay ($bestDayMinutes min)")
        appendLine("───────────────────────────────────────")
        appendLine("DAILY BREAKDOWN:")
        dailyBreakdown.entries.sortedBy { it.key }.forEach { (date, mins) ->
            val bar = "█".repeat((mins / 10).coerceAtMost(20))
            appendLine("  $date  $bar ${mins}m")
        }
        appendLine("═══════════════════════════════════════")
    }
}

class ReportGenerator(private val repository: SessionRepository) {
    fun generate(startDate: LocalDate, endDate: LocalDate): ProductivityReport {
        val filtered = repository.sessions.toList().filter {
            val date = try { LocalDate.parse(it.date) } catch (_: Exception) { null } ?: return@filter false
            !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        val dailyBreakdown = filtered.groupBy { it.date }
            .mapValues { (_, sessions) -> sessions.sumOf { it.durationMinutes } }

        val bestEntry = dailyBreakdown.maxByOrNull { it.value }

        return ProductivityReport(
            startDate = startDate,
            endDate = endDate,
            totalSessions = filtered.size,
            totalMinutes = filtered.sumOf { it.durationMinutes },
            avgSessionMinutes = if (filtered.isEmpty()) 0.0
                else filtered.sumOf { it.durationMinutes }.toDouble() / filtered.size,
            bestDay = bestEntry?.key ?: "N/A",
            bestDayMinutes = bestEntry?.value ?: 0,
            dailyBreakdown = dailyBreakdown
        )
    }

    fun toCsv(startDate: LocalDate, endDate: LocalDate): String = buildString {
        appendLine("date,duration_minutes")
        repository.sessions.toList()
            .filter {
                val date = try { LocalDate.parse(it.date) } catch (_: Exception) { null } ?: return@filter false
                !date.isBefore(startDate) && !date.isAfter(endDate)
            }
            .sortedBy { it.date }
            .forEach { session ->
                appendLine("${session.date},${session.durationMinutes}")
            }
    }
}
