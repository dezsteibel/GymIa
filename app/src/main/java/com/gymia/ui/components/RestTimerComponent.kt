package com.gymia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val REST_PRESETS = listOf(60, 90, 120, 180)

@Composable
fun RestTimerBar(
    isRunning: Boolean,
    remainingSeconds: Int,
    selectedDuration: Int,
    onStop: () -> Unit,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TimerRow(isRunning = isRunning, remainingSeconds = remainingSeconds, onStop = onStop)
            PresetRow(selected = selectedDuration, onSelect = onDurationChange)
        }
    }
}

@Composable
private fun TimerRow(isRunning: Boolean, remainingSeconds: Int, onStop: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isRunning) formatSeconds(remainingSeconds) else "Rest Timer",
            style = MaterialTheme.typography.titleMedium
        )
        if (isRunning) {
            IconButton(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = "Stop Timer")
            }
        }
    }
}

@Composable
private fun PresetRow(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        REST_PRESETS.forEach { seconds ->
            FilterChip(
                selected = selected == seconds,
                onClick = { onSelect(seconds) },
                label = { Text("${seconds}s") }
            )
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
