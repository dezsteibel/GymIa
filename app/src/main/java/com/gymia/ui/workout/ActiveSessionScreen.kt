package com.gymia.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.ui.components.RestTimerBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    viewModel: ActiveSessionViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sessionFinished) {
        if (uiState.sessionFinished) onFinished()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.dayLabel.ifBlank { "Active Workout" }) },
                actions = {
                    TextButton(onClick = viewModel::finishSession, enabled = !uiState.isSaving) {
                        Text(if (uiState.isSaving) "Saving..." else "Finish")
                    }
                }
            )
        },
        bottomBar = {
            RestTimerBar(
                isRunning = uiState.isTimerRunning,
                remainingSeconds = uiState.restTimerSeconds,
                selectedDuration = uiState.selectedRestDuration,
                onStop = viewModel::stopRestTimer,
                onDurationChange = viewModel::onRestDurationChange,
                modifier = Modifier.padding(8.dp)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.exercises.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No exercises in this day.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::finishSession) { Text("Close") }
                }
            }
            else -> ExerciseList(
                exercises = uiState.exercises,
                onRepsChange = viewModel::onRepsChange,
                onLoadChange = viewModel::onLoadChange,
                onConfirmSet = viewModel::confirmSet,
                onAddSet = viewModel::addSet,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ExerciseList(
    exercises: List<ExerciseSessionState>,
    onRepsChange: (Int, Int, String) -> Unit,
    onLoadChange: (Int, Int, String) -> Unit,
    onConfirmSet: (Int, Int) -> Unit,
    onAddSet: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(8.dp)) }
        itemsIndexed(exercises) { exerciseIndex, exerciseState ->
            ExerciseCard(
                exerciseIndex = exerciseIndex,
                exerciseState = exerciseState,
                onRepsChange = { setIndex, value -> onRepsChange(exerciseIndex, setIndex, value) },
                onLoadChange = { setIndex, value -> onLoadChange(exerciseIndex, setIndex, value) },
                onConfirmSet = { setIndex -> onConfirmSet(exerciseIndex, setIndex) },
                onAddSet = { onAddSet(exerciseIndex) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ExerciseCard(
    exerciseIndex: Int,
    exerciseState: ExerciseSessionState,
    onRepsChange: (Int, String) -> Unit,
    onLoadChange: (Int, String) -> Unit,
    onConfirmSet: (Int) -> Unit,
    onAddSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExerciseCardHeader(
                name = exerciseState.exercise.name,
                lastBestLoad = exerciseState.lastBestLoad,
                setsTarget = exerciseState.setsTarget
            )
            Spacer(Modifier.height(8.dp))
            exerciseState.loggedSets.forEachIndexed { setIndex, setEntry ->
                SetRow(
                    setEntry = setEntry,
                    onRepsChange = { onRepsChange(setIndex, it) },
                    onLoadChange = { onLoadChange(setIndex, it) },
                    onConfirm = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirmSet(setIndex)
                    }
                )
                if (setIndex < exerciseState.loggedSets.lastIndex) HorizontalDivider()
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAddSet, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Set")
            }
        }
    }
}

@Composable
private fun ExerciseCardHeader(name: String, lastBestLoad: Float?, setsTarget: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = buildString {
                append("$setsTarget sets")
                if (lastBestLoad != null) append("  ·  prev ${lastBestLoad}kg")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SetRow(
    setEntry: SetEntry,
    onRepsChange: (String) -> Unit,
    onLoadChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val rowBackground = when {
        setEntry.isConfirmed -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground, MaterialTheme.shapes.small)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${setEntry.setNumber}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(24.dp)
        )
        OutlinedTextField(
            value = setEntry.reps,
            onValueChange = onRepsChange,
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            enabled = !setEntry.isConfirmed,
            singleLine = true
        )
        OutlinedTextField(
            value = setEntry.loadKg,
            onValueChange = onLoadChange,
            label = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            enabled = !setEntry.isConfirmed,
            singleLine = true
        )
        if (setEntry.isConfirmed) {
            Icon(Icons.Default.Check, contentDescription = "Confirmed", tint = MaterialTheme.colorScheme.primary)
        } else {
            FilledTonalIconButton(onClick = onConfirm) {
                Icon(Icons.Default.Check, contentDescription = "Confirm Set")
            }
        }
    }
}
