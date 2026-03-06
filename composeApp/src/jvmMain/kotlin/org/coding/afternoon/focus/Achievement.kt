package org.coding.afternoon.focus

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false
)

data class UserLevel(
    val level: Int,
    val totalMinutes: Int,
    val currentLevelMinutes: Int,
    val nextLevelThreshold: Int
) {
    val title: String get() = when (level) {
        1 -> "Rookie Focuser"
        2 -> "Apprentice"
        3 -> "Focused Mind"
        4 -> "Flow Seeker"
        5 -> "Focus Warrior"
        6 -> "Deep Worker"
        7 -> "Flow Master"
        8 -> "Productivity Sage"
        9 -> "Focus Legend"
        else -> "Transcendent"
    }

    val emoji: String get() = when (level) {
        1 -> "🌱"; 2 -> "🌿"; 3 -> "🌳"; 4 -> "⚡"; 5 -> "🔥"
        6 -> "💎"; 7 -> "🚀"; 8 -> "🌟"; 9 -> "🏆"; else -> "👑"
    }

    val progress: Float get() = if (nextLevelThreshold == 0) 1f
        else currentLevelMinutes.toFloat() / nextLevelThreshold.toFloat()
}
