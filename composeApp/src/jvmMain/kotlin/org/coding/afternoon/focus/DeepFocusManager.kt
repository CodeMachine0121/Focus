package org.coding.afternoon.focus

import java.util.prefs.Preferences

class DeepFocusManager {
    private val prefs = Preferences.userNodeForPackage(DeepFocusManager::class.java)
    private var originalBrightness: Float = 1.0f
    private var isActive = false

    fun loadSettings(): DeepFocusSettings {
        return DeepFocusSettings(
            isDndEnabled = prefs.getBoolean("dnd_enabled", true),
            isDimEnabled = prefs.getBoolean("dim_enabled", true),
            dimLevel = prefs.getFloat("dim_level", 0.3f),
            blockedApps = prefs.get("blocked_apps", "Safari,Chrome,Slack,Discord")
                .split(",").filter { it.isNotBlank() }
        )
    }

    fun saveSettings(settings: DeepFocusSettings) {
        prefs.putBoolean("dnd_enabled", settings.isDndEnabled)
        prefs.putBoolean("dim_enabled", settings.isDimEnabled)
        prefs.putFloat("dim_level", settings.dimLevel)
        prefs.put("blocked_apps", settings.blockedApps.joinToString(","))
    }

    fun activate(settings: DeepFocusSettings) {
        if (isActive) return
        isActive = true
        if (settings.isDndEnabled) enableDnd()
        if (settings.isDimEnabled) {
            originalBrightness = getBrightness()
            setBrightness(settings.dimLevel)
        }
        if (settings.blockedApps.isNotEmpty()) warnBlockedApps(settings.blockedApps)
    }

    fun deactivate(settings: DeepFocusSettings) {
        if (!isActive) return
        isActive = false
        if (settings.isDndEnabled) disableDnd()
        if (settings.isDimEnabled) setBrightness(originalBrightness)
    }

    fun isCurrentlyActive() = isActive

    private fun enableDnd() {
        runScript("""
            tell application "System Events"
                tell process "Control Center"
                end tell
            end tell
        """.trimIndent())
        runCommand(
            "defaults", "-currentHost", "write",
            System.getProperty("user.home") + "/Library/Preferences/ByHost/com.apple.notificationcenterui",
            "doNotDisturb", "-boolean", "true"
        )
    }

    private fun disableDnd() {
        runCommand(
            "defaults", "-currentHost", "write",
            System.getProperty("user.home") + "/Library/Preferences/ByHost/com.apple.notificationcenterui",
            "doNotDisturb", "-boolean", "false"
        )
    }

    private fun getBrightness(): Float {
        return try {
            val proc = Runtime.getRuntime().exec(
                arrayOf(
                    "osascript", "-e",
                    "tell application \"System Events\" to get brightness of " +
                        "(first display whose name is \"Built-in Retina Display\")"
                )
            )
            proc.inputStream.bufferedReader().readLine()?.trim()?.toFloatOrNull() ?: 1.0f
        } catch (_: Exception) { 1.0f }
    }

    private fun setBrightness(level: Float) {
        try {
            Runtime.getRuntime().exec(arrayOf("brightness", level.toString())).waitFor()
        } catch (_: Exception) {
            println("[DeepFocus] brightness CLI unavailable; skipping display dim")
        }
    }

    private fun warnBlockedApps(apps: List<String>) {
        apps.forEach { app ->
            try {
                val proc = Runtime.getRuntime().exec(arrayOf("pgrep", "-i", app))
                proc.waitFor()
                if (proc.exitValue() == 0) {
                    println("[DeepFocus] Warning: $app is running")
                }
            } catch (_: Exception) {}
        }
    }

    private fun runScript(script: String): String {
        return try {
            val proc = ProcessBuilder("osascript", "-e", script).start()
            proc.waitFor()
            proc.inputStream.bufferedReader().readText()
        } catch (_: Exception) { "" }
    }

    private fun runCommand(vararg args: String) {
        try { ProcessBuilder(*args).start().waitFor() } catch (_: Exception) {}
    }
}
