package org.coding.afternoon.focus

import java.awt.*
import java.awt.image.BufferedImage
import kotlinx.coroutines.*

/**
 * Manages a system tray icon for the Focus timer app.
 *
 * Responsibilities:
 *  - Register/unregister a tray icon with the OS.
 *  - Update the tooltip to reflect live timer state.
 *  - Show a native balloon notification on session completion.
 *  - Expose a right-click context menu: Start, Pause, Reset, Quit.
 *
 * Call [install] once after the app window is created.
 * Call [uninstall] when the app is about to exit.
 */
class SystemTrayManager(
    private val viewModel: FocusTimerViewModel,
    private val onQuit: () -> Unit,
) {
    private var trayIcon: TrayIcon? = null
    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /** Returns true if the OS supports SystemTray and installation succeeded. */
    fun install(): Boolean {
        if (!SystemTray.isSupported()) return false

        val image = createTrayImage()
        val popup = buildPopupMenu()

        val icon = TrayIcon(image, "Focus - Ready", popup).apply {
            isImageAutoSize = true
        }

        return try {
            SystemTray.getSystemTray().add(icon)
            trayIcon = icon
            startPolling()
            true
        } catch (e: AWTException) {
            false
        }
    }

    fun uninstall() {
        pollJob?.cancel()
        scope.cancel()
        trayIcon?.let { SystemTray.getSystemTray().remove(it) }
        trayIcon = null
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun buildPopupMenu(): PopupMenu {
        val menu = PopupMenu()

        val startItem = MenuItem("Start").apply {
            addActionListener {
                EventQueue.invokeLater { viewModel.start() }
            }
        }
        val pauseItem = MenuItem("Pause").apply {
            addActionListener {
                EventQueue.invokeLater { viewModel.pause() }
            }
        }
        val resetItem = MenuItem("Reset").apply {
            addActionListener {
                EventQueue.invokeLater { viewModel.reset() }
            }
        }
        val quitItem = MenuItem("Quit").apply {
            addActionListener { onQuit() }
        }

        menu.add(startItem)
        menu.add(pauseItem)
        menu.add(resetItem)
        menu.addSeparator()
        menu.add(quitItem)

        return menu
    }

    /** Poll the ViewModel every 500 ms and update the tooltip and icon accordingly. */
    private fun startPolling() {
        pollJob = scope.launch {
            var lastState: TimerState? = null
            var lastRemaining = -1

            while (isActive) {
                val state = viewModel.timerState
                val remaining = viewModel.remainingSeconds
                val phase = viewModel.currentPhase

                val changed = state != lastState || remaining != lastRemaining
                if (changed) {
                    val tooltip = buildTooltip(state, remaining)
                    val isActive = state == TimerState.Running || state == TimerState.Paused
                    val newIcon = createTrayImage(isActive = isActive, phase = phase)
                    EventQueue.invokeLater {
                        trayIcon?.toolTip = tooltip
                        trayIcon?.image = newIcon
                    }

                    if (state == TimerState.Completed && lastState != TimerState.Completed) {
                        showCompletionNotification()
                    }

                    lastState = state
                    lastRemaining = remaining
                }

                delay(500)
            }
        }
    }

    private fun buildTooltip(state: TimerState, remaining: Int): String {
        return when (state) {
            TimerState.Idle -> "Focus - Ready"
            TimerState.Running -> {
                val mm = remaining / 60
                val ss = remaining % 60
                "Focus - %02d:%02d remaining".format(mm, ss)
            }
            TimerState.Paused -> "Focus - Paused"
            TimerState.Completed -> "Focus - Complete!"
        }
    }

    private fun showCompletionNotification() {
        EventQueue.invokeLater {
            trayIcon?.displayMessage(
                "Focus Complete",
                "Your focus session is complete.",
                TrayIcon.MessageType.INFO,
            )
        }
    }

    // -------------------------------------------------------------------------
    // Icon generation
    // -------------------------------------------------------------------------

    /**
     * Creates a 64x64 icon whose color reflects timer state:
     *  - Idle: gray  (#787878)
     *  - Focusing/Paused: red  (#E53935)
     *  - Break: blue (#1E88E5)
     *
     * Uses a donut (ring) shape so the macOS menu bar shows it clearly.
     */
    private fun createTrayImage(isActive: Boolean = false, phase: TimerPhase = TimerPhase.Focus): Image {
        val size = 64
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Outer filled circle — color encodes state
        g.color = when {
            !isActive -> Color(0x78, 0x78, 0x78)          // gray — idle
            phase == TimerPhase.Break -> Color(0x1E, 0x88, 0xE5) // blue — break
            else -> Color(0xE5, 0x39, 0x35)                // red — focusing
        }
        g.fillOval(4, 4, size - 8, size - 8)

        // White inner circle (donut / ring effect)
        g.color = Color(255, 255, 255, 200)
        val innerInset = size / 4
        g.fillOval(innerInset, innerInset, size - innerInset * 2, size - innerInset * 2)

        g.dispose()
        return img
    }
}
