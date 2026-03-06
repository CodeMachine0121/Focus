package org.coding.afternoon.focus

data class DeepFocusSettings(
    val isDndEnabled: Boolean = true,
    val isDimEnabled: Boolean = true,
    val dimLevel: Float = 0.3f,
    val blockedApps: List<String> = listOf("Safari", "Chrome", "Slack", "Discord")
)
