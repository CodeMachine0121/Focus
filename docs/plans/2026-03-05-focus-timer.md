# Focus Timer Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the template App.kt with a working Pomodoro-style focus timer that counts down, shows a circular progress ring, and forces the window to the foreground with an AlertDialog when time is up.

**Architecture:** `FocusTimerViewModel` holds all timer state and drives countdown via coroutines; `FocusScreen` is the pure UI composable; `main.kt` wires the window reference into the ViewModel so it can call `window.toFront()` on completion.

**Tech Stack:** Kotlin, Compose for Desktop (JVM), kotlinx-coroutines-swing, androidx.lifecycle ViewModel, Material3

---

### Task 1: Create FocusTimerViewModel

**Files:**
- Create: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/FocusTimerViewModel.kt`

**Step 1: Create the file with state and logic**

```kotlin
package org.coding.afternoon.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class TimerState { Idle, Running, Paused, Completed }

class FocusTimerViewModel : ViewModel() {
    var totalSeconds by mutableStateOf(25 * 60)
        private set
    var remainingSeconds by mutableStateOf(25 * 60)
        private set
    var timerState by mutableStateOf(TimerState.Idle)
        private set

    private var countdownJob: Job? = null
    var onComplete: (() -> Unit)? = null

    val progress: Float
        get() = if (totalSeconds == 0) 1f else remainingSeconds.toFloat() / totalSeconds.toFloat()

    fun setDuration(minutes: Int) {
        if (timerState != TimerState.Idle) return
        val seconds = minutes * 60
        totalSeconds = seconds
        remainingSeconds = seconds
    }

    fun start() {
        if (timerState == TimerState.Running) return
        timerState = TimerState.Running
        countdownJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds--
            }
            timerState = TimerState.Completed
            onComplete?.invoke()
        }
    }

    fun pause() {
        if (timerState != TimerState.Running) return
        countdownJob?.cancel()
        timerState = TimerState.Paused
    }

    fun reset() {
        countdownJob?.cancel()
        remainingSeconds = totalSeconds
        timerState = TimerState.Idle
    }

    fun dismiss() {
        timerState = TimerState.Idle
    }
}
```

**Step 2: Verify the file compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL (no errors)

**Step 3: Commit**

```bash
git add composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/FocusTimerViewModel.kt
git commit -m "feat: add FocusTimerViewModel with countdown logic"
```

---

### Task 2: Update main.kt to wire window reference

**Files:**
- Modify: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/main.kt`

The `ComposeWindow` is accessible via the `window` property inside the `Window { }` lambda. We pass it to the ViewModel's `onComplete` callback.

**Step 1: Replace main.kt**

```kotlin
package org.coding.afternoon.focus

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

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
```

**Step 2: Verify compile**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/main.kt
git commit -m "feat: wire ComposeWindow reference for foreground-on-complete"
```

---

### Task 3: Create FocusScreen composable

**Files:**
- Create: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/FocusScreen.kt`

**Step 1: Create FocusScreen.kt**

```kotlin
package org.coding.afternoon.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FocusScreen(viewModel: FocusTimerViewModel) {
    val state = viewModel.timerState
    val remaining = viewModel.remainingSeconds
    val progress = viewModel.progress

    val minutes = remaining / 60
    val seconds = remaining % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    // Custom duration input state
    var customInput by remember { mutableStateOf("") }

    if (state == TimerState.Completed) {
        AlertDialog(
            onDismissRequest = { viewModel.dismiss() },
            title = { Text("Time's up!") },
            text = { Text("Your focus session is complete.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismiss() }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Circular progress ring + time display
        Box(contentAlignment = Alignment.Center) {
            val ringColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.size(220.dp)) {
                val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                val inset = 8.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                val topLeft = Offset(inset, inset)
                // Track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                // Progress
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
            Text(
                text = timeText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Preset buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(25, 15, 5).forEach { minutes ->
                OutlinedButton(
                    onClick = { viewModel.setDuration(minutes) },
                    enabled = state == TimerState.Idle,
                ) {
                    Text("${minutes}m")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Custom duration input
        OutlinedTextField(
            value = customInput,
            onValueChange = { value ->
                val filtered = value.filter { it.isDigit() }.take(3)
                customInput = filtered
                filtered.toIntOrNull()?.let { viewModel.setDuration(it) }
            },
            label = { Text("Custom (min)") },
            enabled = state == TimerState.Idle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(160.dp),
        )

        Spacer(Modifier.height(32.dp))

        // Control buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state) {
                TimerState.Idle, TimerState.Completed -> {
                    Button(onClick = { viewModel.start() }) { Text("Start") }
                }
                TimerState.Running -> {
                    Button(onClick = { viewModel.pause() }) { Text("Pause") }
                }
                TimerState.Paused -> {
                    Button(onClick = { viewModel.start() }) { Text("Resume") }
                }
            }
            OutlinedButton(
                onClick = { viewModel.reset() },
                enabled = state != TimerState.Idle,
            ) {
                Text("Reset")
            }
        }
    }
}
```

**Step 2: Verify compile**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/FocusScreen.kt
git commit -m "feat: add FocusScreen with circular progress ring and controls"
```

---

### Task 4: Update App.kt to use FocusScreen

**Files:**
- Modify: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/App.kt`

**Step 1: Replace App.kt**

```kotlin
package org.coding.afternoon.focus

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun App(viewModel: FocusTimerViewModel) {
    MaterialTheme {
        FocusScreen(viewModel)
    }
}
```

**Step 2: Verify compile**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Run the app and manually test**

Run: `./gradlew :composeApp:run`

Verify:
- Window opens showing "25:00" in a circle
- Preset buttons (25m, 15m, 5m) change the displayed time
- Custom input field accepts numbers and updates the timer
- Start begins countdown
- Pause halts countdown
- Resume continues
- Reset returns to Idle
- When timer reaches 0:00, window comes to foreground and AlertDialog appears
- Clicking OK dismisses the dialog and returns to Idle state

**Step 4: Commit**

```bash
git add composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/App.kt
git commit -m "feat: wire FocusScreen into App - focus timer complete"
```

---

### Task 5: Cleanup unused files

**Files:**
- Delete or empty: `composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/Greeting.kt`

`Greeting.kt` was part of the template and is no longer used.

**Step 1: Delete Greeting.kt**

```bash
rm composeApp/src/jvmMain/kotlin/org/coding/afternoon/focus/Greeting.kt
```

**Step 2: Verify compile still passes**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add -u
git commit -m "chore: remove unused Greeting template file"
```
