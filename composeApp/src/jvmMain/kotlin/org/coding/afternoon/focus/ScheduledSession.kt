package org.coding.afternoon.focus

import java.util.UUID

enum class RecurringType { ONCE, DAILY, WEEKDAYS }

data class ScheduledSession(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val durationMinutes: Int = 25,
    val recurringType: RecurringType = RecurringType.ONCE,
    val isActive: Boolean = true
) {
    val displayTime: String get() = "%02d:%02d".format(scheduledHour, scheduledMinute)
    val recurringLabel: String get() = when (recurringType) {
        RecurringType.ONCE -> "Once"
        RecurringType.DAILY -> "Daily"
        RecurringType.WEEKDAYS -> "Weekdays"
    }
}
