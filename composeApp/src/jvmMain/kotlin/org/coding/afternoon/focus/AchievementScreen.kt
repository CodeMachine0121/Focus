package org.coding.afternoon.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFFFD700)
private val LevelPurple = Color(0xFF6750A4)

@Composable
fun AchievementScreen(viewModel: AchievementViewModel) {
    LaunchedEffect(Unit) { viewModel.refresh() }

    val level = viewModel.userLevel
    val achievements = viewModel.achievements
    val challenges = viewModel.challenges
    val newlyUnlocked = viewModel.newlyUnlocked

    if (newlyUnlocked != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUnlocked() },
            title = { Text("🎉 Achievement Unlocked!") },
            text = {
                Text("${newlyUnlocked.emoji} ${newlyUnlocked.title}\n${newlyUnlocked.description}")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissUnlocked() }) { Text("Awesome!") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { LevelCard(level) }

        item {
            Text("📋 Today's Challenges", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        items(challenges) { challenge ->
            ChallengeCard(challenge)
        }

        item {
            Text("🏆 Achievements", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(600.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(achievements) { ach ->
                    AchievementCard(ach)
                }
            }
        }
    }
}

@Composable
private fun LevelCard(level: UserLevel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LevelPurple)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(level.emoji, fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Level ${level.level}", color = Color.White, fontSize = 12.sp)
                    Text(
                        level.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        "${level.totalMinutes} min total",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { level.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Gold,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${level.currentLevelMinutes} / ${level.nextLevelThreshold} min to next level",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(challenge.title, fontWeight = FontWeight.SemiBold)
                if (challenge.isCompleted) Text("✅", fontSize = 16.sp)
            }
            Text(challenge.description, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = {
                    if (challenge.targetCount > 0)
                        challenge.currentCount.toFloat() / challenge.targetCount
                    else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${challenge.currentCount} / ${challenge.targetCount}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val bg = if (achievement.isUnlocked) Gold.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (achievement.isUnlocked) achievement.emoji else "🔒", fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(achievement.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(
                achievement.description,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
}
