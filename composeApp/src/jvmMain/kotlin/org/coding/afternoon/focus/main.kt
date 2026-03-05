package org.coding.afternoon.focus

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Focus",
    ) {
        val scope = rememberCoroutineScope()
        val composeWindow = window
        val viewModel = viewModel { FocusTimerViewModel() }

        viewModel.onComplete = {
            scope.launch {
                composeWindow.isAlwaysOnTop = true
                composeWindow.toFront()
                composeWindow.requestFocus()
                delay(500)
                composeWindow.isAlwaysOnTop = false
            }
        }

        App(viewModel)
    }
}