package org.coding.afternoon.focus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

private data class NavItem(val emoji: String, val label: String)

private val navItems = listOf(
    NavItem("⏱", "Timer"),
    NavItem("📋", "History"),
    NavItem("🎯", "Goals"),
    NavItem("🎵", "Sounds"),
    NavItem("📊", "Stats"),
    NavItem("🧘", "Break"),
    NavItem("💼", "Work"),
)

@Composable
fun App(viewModel: FocusTimerViewModel) {
    val repository = remember { SessionRepository() }
    val goalRepository = remember { GoalRepository() }

    LaunchedEffect(Unit) {
        viewModel.onSessionDismissed = { durationMinutes, label ->
            repository.record(durationMinutes, label)
        }
    }

    MaterialTheme {
        var selectedTab by remember { mutableStateOf(0) }

        val goalViewModel = remember { GoalViewModel(goalRepository) }
        val ambientSoundViewModel = remember { AmbientSoundViewModel() }
        val dashboardViewModel = remember { DashboardViewModel(repository) }
        val breakCoachViewModel = remember { BreakCoachViewModel(focusTimerViewModel = viewModel) }
        val workspaceViewModel = remember { WorkspaceViewModel() }

        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail {
                navItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(item.emoji, fontSize = 20.sp) },
                        label = { Text(item.label, fontSize = 10.sp) },
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (selectedTab) {
                    0 -> FocusScreen(viewModel)
                    1 -> HistoryScreen(repository)
                    2 -> GoalScreen(goalViewModel)
                    3 -> AmbientSoundScreen(ambientSoundViewModel)
                    4 -> {
                        LaunchedEffect(selectedTab) { dashboardViewModel.refresh() }
                        DashboardScreen(dashboardViewModel)
                    }
                    5 -> BreakCoachScreen(breakCoachViewModel)
                    6 -> WorkspaceScreen(workspaceViewModel)
                }
            }
        }
    }
}
