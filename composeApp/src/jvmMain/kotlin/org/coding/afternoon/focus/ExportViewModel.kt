package org.coding.afternoon.focus

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.time.LocalDate

enum class ReportPeriod(val label: String) {
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_90_DAYS("Last 90 days"),
    ALL_TIME("All time")
}

class ExportViewModel(private val generator: ReportGenerator) : ViewModel() {
    var selectedPeriod by mutableStateOf(ReportPeriod.LAST_30_DAYS)
    var report by mutableStateOf<ProductivityReport?>(null)
    var statusMessage by mutableStateOf("")

    fun generateReport() {
        val (start, end) = periodDates()
        val generated = generator.generate(start, end)
        report = if (generated.totalSessions > 0) generated else null
        statusMessage = ""
    }

    fun exportCsv() {
        val (start, end) = periodDates()
        val csv = generator.toCsv(start, end)
        try {
            val dialog = FileDialog(null as Frame?, "Save CSV Report", FileDialog.SAVE)
            dialog.file = "focus-report-${start}_${end}.csv"
            dialog.isVisible = true
            val dir = dialog.directory ?: return
            val file = dialog.file ?: return
            File(dir, file).writeText(csv)
            statusMessage = "✅ CSV exported to $file"
        } catch (e: Exception) {
            statusMessage = "❌ Export failed: ${e.message}"
        }
    }

    fun copyReport() {
        val text = report?.formattedText ?: return
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
            statusMessage = "✅ Report copied to clipboard!"
        } catch (_: Exception) {
            statusMessage = "❌ Could not copy to clipboard"
        }
    }

    fun selectPeriod(period: ReportPeriod) {
        selectedPeriod = period
        generateReport()
    }

    private fun periodDates(): Pair<LocalDate, LocalDate> {
        val end = LocalDate.now()
        val start = when (selectedPeriod) {
            ReportPeriod.LAST_7_DAYS -> end.minusDays(7)
            ReportPeriod.LAST_30_DAYS -> end.minusDays(30)
            ReportPeriod.LAST_90_DAYS -> end.minusDays(90)
            ReportPeriod.ALL_TIME -> LocalDate.of(2020, 1, 1)
        }
        return start to end
    }
}
