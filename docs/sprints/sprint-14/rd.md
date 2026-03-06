# Sprint 14 — RD Technical Spec

## Architecture
- Pattern: `ReportGenerator.kt` (domain) + `ExportViewModel.kt` (state) + `ExportScreen.kt` (UI)
- No new Gradle dependencies — pure Kotlin stdlib + java.* APIs

## Files Created
| File | Purpose |
|------|---------|
| `ReportGenerator.kt` | Pure domain: CSV generation + text report generation |
| `ExportViewModel.kt` | Compose state holder: period selection, export actions |
| `ExportScreen.kt` | Compose UI: period chips, preview card, action buttons |

## API Notes
- `SessionRepository.sessions` (SnapshotStateList) used via `.toList()` — no `loadAll()` method exists
- `SessionRecord` fields: `date: String`, `startTime: String`, `durationMinutes: Int` — NO label field
- CSV format: `date,duration_minutes` (no label column since field doesn't exist)

## CSV Format
```
date,duration_minutes
2024-01-01,25
2024-01-01,50
2024-01-02,25
```

## Report Format
ASCII art box using `═`, `─`, `█` characters; rendered in monospace terminal block in UI.

## File Save
- `java.awt.FileDialog(null as Frame?, "Save CSV Report", FileDialog.SAVE)`
- Pre-filled filename: `focus-report-{start}_{end}.csv`

## Clipboard
- `Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)`

## Date Filtering
- `LocalDate.parse(it.date)` — records already stored as `yyyy-MM-dd`
- Filter: `!date.isBefore(start) && !date.isAfter(end)`
