package org.coding.afternoon.focus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AchievementViewModel(
    private val sessionRepository: SessionRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    var userLevel by mutableStateOf(UserLevel(1, 0, 0, 60))
        private set
    var achievements by mutableStateOf<List<Achievement>>(emptyList())
        private set
    var challenges by mutableStateOf<List<Challenge>>(emptyList())
        private set
    var newlyUnlocked by mutableStateOf<Achievement?>(null)
        private set

    fun refresh() {
        val sessions = sessionRepository.sessions.toList()
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val totalSessions = sessions.size
        val streakDays = StreakCalculator.calculateCurrentStreak(sessions)

        val today = SessionRecord.today()
        val todaySessions = sessions.filter { it.date == today }
        val todayMinutes = todaySessions.sumOf { it.durationMinutes }

        val prev = achievements
        val updated = achievementRepository.getAchievements(totalSessions, totalMinutes, streakDays)

        // Only surface a newly-unlocked achievement if it wasn't unlocked in previous state
        if (newlyUnlocked == null) {
            newlyUnlocked = updated.firstOrNull { a ->
                a.isUnlocked && prev.none { it.id == a.id && it.isUnlocked }
            }
        }

        userLevel = achievementRepository.getUserLevel(totalMinutes)
        achievements = updated
        challenges = achievementRepository.getDailyChallenges(todaySessions.size, todayMinutes)
    }

    fun dismissUnlocked() {
        newlyUnlocked = null
    }
}
