package org.coding.afternoon.focus

import java.util.prefs.Preferences

class AchievementRepository {
    private val prefs = Preferences.userNodeForPackage(AchievementRepository::class.java)

    private val levelThresholds = listOf(0, 60, 180, 360, 720, 1200, 2000, 3000, 5000, 8000)

    fun getUserLevel(totalMinutes: Int): UserLevel {
        var level = 1
        for (i in levelThresholds.indices.reversed()) {
            if (totalMinutes >= levelThresholds[i]) {
                level = i + 1
                break
            }
        }
        level = level.coerceAtMost(10)
        val currentThreshold = levelThresholds.getOrElse(level - 1) { 0 }
        val nextThreshold = levelThresholds.getOrElse(level) { levelThresholds.last() }
        return UserLevel(
            level = level,
            totalMinutes = totalMinutes,
            currentLevelMinutes = totalMinutes - currentThreshold,
            nextLevelThreshold = nextThreshold - currentThreshold
        )
    }

    fun getAchievements(totalSessions: Int, totalMinutes: Int, streakDays: Int): List<Achievement> {
        return listOf(
            Achievement("first_session", "First Steps", "Complete your first focus session", "🎯",
                totalSessions >= 1),
            Achievement("session_5", "Getting Started", "Complete 5 focus sessions", "⭐",
                totalSessions >= 5),
            Achievement("session_25", "Committed", "Complete 25 focus sessions", "🔥",
                totalSessions >= 25),
            Achievement("session_100", "Century Club", "Complete 100 focus sessions", "💯",
                totalSessions >= 100),
            Achievement("hour_1", "One Hour Club", "Focus for 60 minutes total", "⏰",
                totalMinutes >= 60),
            Achievement("hour_10", "Time Investor", "Focus for 10 hours total", "📈",
                totalMinutes >= 600),
            Achievement("hour_50", "Dedicated", "Focus for 50 hours total", "🏅",
                totalMinutes >= 3000),
            Achievement("hour_100", "Century Focuser", "Focus for 100 hours total", "🏆",
                totalMinutes >= 6000),
            Achievement("streak_3", "3-Day Streak", "Focus 3 days in a row", "🔥",
                streakDays >= 3),
            Achievement("streak_7", "Week Warrior", "Focus 7 days in a row", "📅",
                streakDays >= 7),
            Achievement("streak_30", "Monthly Master", "Focus 30 days in a row", "🌙",
                streakDays >= 30),
            Achievement("early_bird", "Early Bird", "Complete a session before 9 AM", "🌅",
                prefs.getBoolean("ach_early_bird", false)),
            Achievement("night_owl", "Night Owl", "Complete a session after 10 PM", "🦉",
                prefs.getBoolean("ach_night_owl", false))
        )
    }

    fun getDailyChallenges(completedTodaySessions: Int, completedTodayMinutes: Int): List<Challenge> {
        return listOf(
            Challenge("daily_3", "Triple Focus", "Complete 3 focus sessions today", 3,
                minOf(completedTodaySessions, 3), completedTodaySessions >= 3),
            Challenge("daily_5", "Power Day", "Complete 5 focus sessions today", 5,
                minOf(completedTodaySessions, 5), completedTodaySessions >= 5),
            Challenge("daily_60min", "Hour of Power", "Focus for 60 minutes today", 60,
                minOf(completedTodayMinutes, 60), completedTodayMinutes >= 60),
            Challenge("daily_120min", "Two Hour Club", "Focus for 2 hours today", 120,
                minOf(completedTodayMinutes, 120), completedTodayMinutes >= 120)
        )
    }

    fun markEarlyBird() = prefs.putBoolean("ach_early_bird", true)
    fun markNightOwl() = prefs.putBoolean("ach_night_owl", true)
}
