package org.coding.afternoon.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Composable screen for the Ambient Sound Engine.
 *
 * Displays:
 * - A pulsing status indicator showing whether audio is currently playing.
 * - A sound type selector (four radio-button rows).
 * - A volume slider with a live percentage label.
 * - A Play/Stop toggle button.
 * - An optional error message when the audio device is unavailable.
 *
 * All state is sourced from [AmbientSoundViewModel]; this composable is stateless.
 * Playback continues even when this screen leaves the composition — the ViewModel
 * outlives the Composable.
 */
@Composable
fun AmbientSoundScreen(viewModel: AmbientSoundViewModel) {
    val isPlaying = viewModel.isPlaying
    val selectedSound = viewModel.selectedSound
    val volume = viewModel.volume
    val errorMessage = viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // ---- Section heading ----
        Text(
            text = "Ambient Sound",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(16.dp))

        // ---- Status indicator row ----
        StatusIndicatorRow(isPlaying = isPlaying, errorMessage = errorMessage)

        Spacer(Modifier.height(20.dp))

        // ---- Sound type selector ----
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = "Sound",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(4.dp))

            AmbientSoundType.entries.forEach { type ->
                SoundTypeRow(
                    type = type,
                    selected = selectedSound == type,
                    onSelect = { viewModel.selectSound(type) },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- Volume slider ----
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Volume",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Slider(
                value = volume,
                onValueChange = { viewModel.setVolume(it) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))

        // ---- Play / Stop button ----
        Button(
            onClick = { viewModel.togglePlayback() },
            modifier = Modifier.fillMaxWidth(),
            colors = if (isPlaying) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
        ) {
            Text(if (isPlaying) "Stop" else "Play")
        }

        // ---- Error message ----
        AnimatedVisibility(visible = errorMessage != null) {
            Column {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Sub-composables
// -----------------------------------------------------------------------------------------

@Composable
private fun StatusIndicatorRow(isPlaying: Boolean, errorMessage: String?) {
    val dotColor = when {
        errorMessage != null -> MaterialTheme.colorScheme.error
        isPlaying -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val statusText = when {
        errorMessage != null -> "Audio unavailable"
        isPlaying -> "Playing"
        else -> "Stopped"
    }

    val statusColor = when {
        errorMessage != null -> MaterialTheme.colorScheme.error
        isPlaying -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Alpha animation for the dot when playing
    val dotAlpha: Float = if (isPlaying && errorMessage == null) {
        val infiniteTransition = rememberInfiniteTransition(label = "ambient-pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dot-alpha",
        )
        alpha
    } else {
        1.0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            drawCircle(
                color = dotColor.copy(alpha = dotAlpha),
                radius = 6.dp.toPx(),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
        )
    }
}

@Composable
private fun SoundTypeRow(
    type: AmbientSoundType,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
