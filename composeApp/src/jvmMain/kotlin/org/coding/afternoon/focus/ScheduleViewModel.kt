package org.coding.afternoon.focus

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {

    var sessions by mutableStateOf<List<ScheduledSession>>(emptyList())
        private set
    var labelInput by mutableStateOf("")
    var hourInput by mutableStateOf(9)
    var minuteInput by mutableStateOf(0)
    var durationInput by mutableStateOf(25)
    var recurringType by mutableStateOf(RecurringType.ONCE)

    fun load() {
        sessions = repository.loadAll()
            .sortedWith(compareBy({ it.scheduledHour }, { it.scheduledMinute }))
    }

    fun updateLabel(v: String) { labelInput = v }
    fun updateHour(v: Int) { hourInput = v.coerceIn(0, 23) }
    fun updateMinute(v: Int) { minuteInput = v.coerceIn(0, 59) }
    fun updateDuration(v: Int) { durationInput = v.coerceIn(5, 120) }
    fun updateRecurring(v: RecurringType) { recurringType = v }

    fun addSession() {
        if (labelInput.isBlank()) return
        repository.save(
            ScheduledSession(
                label = labelInput.trim(),
                scheduledHour = hourInput,
                scheduledMinute = minuteInput,
                durationMinutes = durationInput,
                recurringType = recurringType
            )
        )
        labelInput = ""
        load()
    }

    fun deleteSession(id: String) {
        repository.delete(id)
        load()
    }
}
