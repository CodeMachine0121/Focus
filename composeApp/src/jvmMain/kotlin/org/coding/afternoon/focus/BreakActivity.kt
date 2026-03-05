package org.coding.afternoon.focus

enum class BreakCategory(val displayName: String) {
    Movement("Movement"),
    EyeRest("Eye Rest"),
    Mindfulness("Mindfulness"),
    Hydration("Hydration"),
    Creative("Creative"),
}

data class BreakActivity(
    val id: String,
    val title: String,
    val description: String,
    val category: BreakCategory,
    /** Expected time to complete the activity, in seconds. */
    val durationSeconds: Int,
    /** A single emoji used as the card icon. */
    val emoji: String,
)
