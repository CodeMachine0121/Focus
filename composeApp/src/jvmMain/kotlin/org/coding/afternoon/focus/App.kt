package org.coding.afternoon.focus

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

@Composable
fun App(viewModel: FocusTimerViewModel) {
    val repository = remember { SessionRepository() }

    SideEffectOnce {
        viewModel.onSessionDismissed = { durationMinutes ->
            repository.record(durationMinutes)
        }
    }

    MaterialTheme {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Timer", "History")

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }
            when (selectedTab) {
                0 -> FocusScreen(viewModel)
                1 -> HistoryScreen(repository)
            }
        }
    }
}

/**
 * Runs [block] only once per composition lifecycle (on first composition).
 * Used to wire ViewModel callbacks without repeated re-assignment on every recomposition.
 */
@Composable
private fun SideEffectOnce(block: () -> Unit) {
    var ran by remember { mutableStateOf(false) }
    if (!ran) {
        block()
        ran = true
    }
}
