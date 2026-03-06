package org.coding.afternoon.focus

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*

class DeepFocusViewModel(private val manager: DeepFocusManager) : ViewModel() {
    var settings by mutableStateOf(DeepFocusSettings())
        private set
    var isActive by mutableStateOf(false)
        private set
    var newBlockedApp by mutableStateOf("")

    fun load() {
        settings = manager.loadSettings()
        isActive = manager.isCurrentlyActive()
    }

    fun toggleActive() {
        if (isActive) {
            manager.deactivate(settings)
            isActive = false
        } else {
            manager.activate(settings)
            isActive = true
        }
    }

    fun updateDndEnabled(v: Boolean) {
        if (!isActive) { settings = settings.copy(isDndEnabled = v); manager.saveSettings(settings) }
    }

    fun updateDimEnabled(v: Boolean) {
        if (!isActive) { settings = settings.copy(isDimEnabled = v); manager.saveSettings(settings) }
    }

    fun updateDimLevel(v: Float) {
        if (!isActive) { settings = settings.copy(dimLevel = v); manager.saveSettings(settings) }
    }

    fun addBlockedApp() {
        if (newBlockedApp.isBlank() || isActive) return
        settings = settings.copy(blockedApps = settings.blockedApps + newBlockedApp.trim())
        manager.saveSettings(settings)
        newBlockedApp = ""
    }

    fun removeBlockedApp(app: String) {
        if (isActive) return
        settings = settings.copy(blockedApps = settings.blockedApps - app)
        manager.saveSettings(settings)
    }

    fun updateNewBlockedApp(v: String) { newBlockedApp = v }
}
