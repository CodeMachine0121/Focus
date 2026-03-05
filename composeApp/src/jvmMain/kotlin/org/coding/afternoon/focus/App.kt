package org.coding.afternoon.focus

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun App(viewModel: FocusTimerViewModel) {
    MaterialTheme {
        FocusScreen(viewModel)
    }
}