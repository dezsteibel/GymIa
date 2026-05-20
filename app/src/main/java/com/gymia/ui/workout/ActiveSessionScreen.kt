package com.gymia.ui.workout

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.gymia.domain.model.LoadSuggestion
import com.gymia.domain.model.Trend
import com.gymia.ui.components.RestTimerBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    viewModel: ActiveSessionViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current

    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NotificationPermissionRequest()
    }

    // Timer flash animation state
    var timerFlashRed by remember { mutableStateOf(false) }
    val timerFlashColor by animateColorAsState(
        targetValue = if (timerFlashRed) androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.3f)
                      else androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        finishedListener = { if (timerFlashRed) timerFlashRed = false },
        label = "timerFlash"
    )

    // React to timer completing
    LaunchedEffect(uiState.restTimerCompleted) {
        if (uiState.restTimerCompleted) {
            timerFlashRed = true
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            viewModel.clearTimerCompleted()
        }
    }

    LaunchedEffect(uiState.sessionFinished) {
        if (uiState.sessionFinished) onFinished()
    }

    if (uiState.notesDialogExerciseIndex != null) {
        NotesDialog(
            text = uiState.notesDialogText,
            onTextChange = viewModel::onNotesDialogTextChange,
            onConfirm = viewModel::saveNotes,
            onDismiss = viewModel::dismissNotesDialog
        )
    }

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeSessionContent(uiState = uiState, viewModel = viewModel)
    } else {
        PortraitSessionContent(uiState = uiState, viewModel = viewModel, timerFlashColor = timerFlashColor)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionRequest() {
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitSessionContent(
    uiState: ActiveSessionUiState,
    viewModel: ActiveSessionViewModel,
    timerFlashColor: Color = Color.Transparent
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.dayLabel.ifBlank { "Active Workout" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatElapsed(uiState.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::finishSession, enabled = !uiState.isSaving) {
                        Text(if (uiState.isSaving) "Saving..." else "Finish")
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.background(timerFlashColor)) {
                RestTimerBar(
                    isRunning = uiState.isTimerRunning,
                    remainingSeconds = uiState.restTimerSeconds,
                    selectedDuration = uiState.selectedRestDuration,
                    onStop = viewModel::stopRestTimer,
                    onDurationChange = viewModel::onRestDurationChange,
                    modifier = Modifier.padding(8.dp)
                )
            }
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
                onSuggestionTap = viewModel::onSuggestionTap,
                onNotesClick = viewModel::openNotesDialog,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeSessionContent(
    uiState: ActiveSessionUiState,
    viewModel: ActiveSessionViewModel
) {
    var selectedExerciseIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.dayLabel.ifBlank { "Active Workout" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = formatElapsed(uiState.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::finishSession, enabled = !uiState.isSaving) {
                        Text(if (uiState.isSaving) "Saving..." else "Finish")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val leftWidth = maxWidth * 0.40f
            val rightWidth = maxWidth * 0.60f
            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .width(leftWidth)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(uiState.exercises) { index, exerciseState ->
                        LandscapeExerciseItem(
                            name = exerciseState.exercise.name,
                            isSelected = index == selectedExerciseIndex,
                            onSelect = { selectedExerciseIndex = index }
                        )
                        HorizontalDivider()
                    }
                }
                Column(
                    modifier = Modifier
                        .width(rightWidth)
                        .fillMaxHeight()
                ) {
                    val exerciseState = uiState.exercises.getOrNull(selectedExerciseIndex)
                    if (exerciseState != null) {
                        ExerciseCard(
                            exerciseState = exerciseState,
                            onRepsChange = { setIndex, value -> viewModel.onRepsChange(selectedExerciseIndex, setIndex, value) },
                            onLoadChange = { setIndex, value -> viewModel.onLoadChange(selectedExerciseIndex, setIndex, value) },
                            onConfirmSet = { setIndex -> viewModel.confirmSet(selectedExerciseIndex, setIndex) },
                            onAddSet = { viewModel.addSet(selectedExerciseIndex) },
                            onSuggestionTap = { setIndex -> viewModel.onSuggestionTap(selectedExerciseIndex, setIndex) },
                            onNotesClick = { setIndex -> viewModel.openNotesDialog(selectedExerciseIndex, setIndex) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    RestTimerBar(
                        isRunning = uiState.isTimerRunning,
                        remainingSeconds = uiState.restTimerSeconds,
                        selectedDuration = uiState.selectedRestDuration,
                        onStop = viewModel::stopRestTimer,
                        onDurationChange = viewModel::onRestDurationChange,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeExerciseItem(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                     else MaterialTheme.colorScheme.surface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        color = background
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun NotesDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Notes") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note...") },
                singleLine = false,
                maxLines = 4
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ExerciseList(
    exercises: List<ExerciseSessionState>,
    onRepsChange: (Int, Int, String) -> Unit,
    onLoadChange: (Int, Int, String) -> Unit,
    onConfirmSet: (Int, Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onSuggestionTap: (Int, Int) -> Unit,
    onNotesClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(8.dp)) }
        itemsIndexed(exercises) { exerciseIndex, exerciseState ->
            ExerciseCard(
                exerciseState = exerciseState,
                onRepsChange = { setIndex, value -> onRepsChange(exerciseIndex, setIndex, value) },
                onLoadChange = { setIndex, value -> onLoadChange(exerciseIndex, setIndex, value) },
                onConfirmSet = { setIndex -> onConfirmSet(exerciseIndex, setIndex) },
                onAddSet = { onAddSet(exerciseIndex) },
                onSuggestionTap = { setIndex -> onSuggestionTap(exerciseIndex, setIndex) },
                onNotesClick = { setIndex -> onNotesClick(exerciseIndex, setIndex) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ExerciseCard(
    exerciseState: ExerciseSessionState,
    onRepsChange: (Int, String) -> Unit,
    onLoadChange: (Int, String) -> Unit,
    onConfirmSet: (Int) -> Unit,
    onAddSet: () -> Unit,
    onSuggestionTap: (Int) -> Unit,
    onNotesClick: (Int) -> Unit,
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
                setsTarget = exerciseState.setsTarget,
                lastBestLoad = exerciseState.lastBestLoad,
                loadSuggestion = exerciseState.loadSuggestion,
                onSuggestionTap = { onSuggestionTap(0) }
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
                    },
                    onNotesClick = { onNotesClick(setIndex) }
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
private fun ExerciseCardHeader(
    name: String,
    setsTarget: Int,
    lastBestLoad: Float?,
    loadSuggestion: LoadSuggestion?,
    onSuggestionTap: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "$setsTarget sets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (loadSuggestion != null) {
            val (trendIcon, chipColor) = when (loadSuggestion.trend) {
                Trend.PROGRESSING -> "↑" to MaterialTheme.colorScheme.primary
                Trend.STABLE -> "→" to MaterialTheme.colorScheme.tertiary
                Trend.REGRESSING -> "↓" to MaterialTheme.colorScheme.error
            }
            SuggestionChip(
                onClick = onSuggestionTap,
                label = {
                    Text(
                        text = "$trendIcon ${"%.1f".format(loadSuggestion.suggestedLoad)}kg",
                        fontWeight = FontWeight.Bold,
                        color = chipColor
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = chipColor.copy(alpha = 0.12f)
                )
            )
        } else if (lastBestLoad != null) {
            Text(
                text = "prev ${lastBestLoad}kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SetRow(
    setEntry: SetEntry,
    onRepsChange: (String) -> Unit,
    onLoadChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onNotesClick: () -> Unit
) {
    val rowBackground = when {
        setEntry.isConfirmed -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Column {
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
            IconButton(onClick = onNotesClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Notes",
                    tint = if (setEntry.notes.isNotBlank()) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (setEntry.notes.isNotBlank()) {
            Text(
                text = setEntry.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(start = 32.dp, bottom = 2.dp)
            )
        }
    }
}

private fun formatElapsed(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}
