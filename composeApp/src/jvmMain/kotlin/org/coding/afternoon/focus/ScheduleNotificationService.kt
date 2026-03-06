package org.coding.afternoon.focus

import kotlinx.coroutines.*
import java.awt.SystemTray
import java.awt.TrayIcon
import java.time.LocalTime

/**
 * Background service that polls every minute and fires an AWT tray notification
 * when a [ScheduledSession]'s scheduled time matches the current wall-clock minute.
 *
 * ONCE sessions are deleted after firing. DAILY and WEEKDAYS sessions are kept active.
 * All notification errors are swallowed so a missing SystemTray never crashes the app.
 */
class ScheduleNotificationService(private val repository: ScheduleRepository) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    fun start() {
        job = scope.launch {
            while (isActive) {
                checkAndNotify()
                delay(60_000L)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    private fun checkAndNotify() {
        val now = LocalTime.now()
        repository.loadAll()
            .filter { it.isActive }
            .forEach { session ->
                if (session.scheduledHour == now.hour && session.scheduledMinute == now.minute) {
                    sendNotification(session)
                    if (session.recurringType == RecurringType.ONCE) {
                        repository.delete(session.id)
                    }
                }
            }
    }

    private fun sendNotification(session: ScheduledSession) {
        try {
            if (SystemTray.isSupported()) {
                val trayIcons = SystemTray.getSystemTray().trayIcons
                if (trayIcons.isNotEmpty()) {
                    trayIcons[0].displayMessage(
                        "⏱ Focus Time!",
                        "Time to start: ${session.label} (${session.durationMinutes} min)",
                        TrayIcon.MessageType.INFO
                    )
                }
            }
        } catch (_: Exception) {
            // Silently skip if tray is unavailable
        }
    }
}
