# Sprint 14 — Product Spec: Session Data Export & Visual Reports

## User Stories

**US-1**: As a user, I want to export my focus sessions as CSV so I can analyze them in Excel/Sheets.

**US-2**: As a user, I want to see an in-app productivity report so I can understand my focus habits at a glance.

**US-3**: As a user, I want to filter the report by time period (7/30/90 days, all time) so I can compare different windows.

**US-4**: As a user, I want to copy the report text to clipboard so I can share it with my manager or coach.

## Data Models

```kotlin
data class ProductivityReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalSessions: Int,
    val totalMinutes: Int,
    val avgSessionMinutes: Double,
    val bestDay: String,
    val bestDayMinutes: Int,
    val dailyBreakdown: Map<String, Int>   // date -> total minutes
)

enum class ReportPeriod { LAST_7_DAYS, LAST_30_DAYS, LAST_90_DAYS, ALL_TIME }
```

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC-1 | Selecting a period updates the report preview without restart |
| AC-2 | CSV has header row: `date,duration_minutes` |
| AC-3 | CSV rows are sorted by date ascending |
| AC-4 | Save dialog uses native OS file dialog (FileDialog) |
| AC-5 | Report preview shows: sessions, hours, avg, best day |
| AC-6 | Daily breakdown renders mini bar chart (ASCII █ blocks) |
| AC-7 | Copy button copies formatted text; status message confirms |
| AC-8 | Empty period shows "No sessions found" gracefully |
| AC-9 | Compiles cleanly with no external library additions |
