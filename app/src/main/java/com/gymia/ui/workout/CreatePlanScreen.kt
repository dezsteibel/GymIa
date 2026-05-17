package com.gymia.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.data.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    viewModel: CreatePlanViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedPlanId) {
        if (uiState.savedPlanId != null) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.planName,
                    onValueChange = viewModel::onPlanNameChange,
                    label = { Text("Plan Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Text("Training Days", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(uiState.days) { dayIndex, day ->
                DayCard(
                    dayIndex = dayIndex,
                    day = day,
                    onLabelChange = { viewModel.onDayLabelChange(dayIndex, it) },
                    onRemoveDay = { viewModel.removeDay(dayIndex) },
                    onAddExercise = { viewModel.openExercisePicker(dayIndex) },
                    onRemoveExercise = { exIndex -> viewModel.removeExerciseFromDay(dayIndex, exIndex) }
                )
                Spacer(Modifier.height(8.dp))
            }
            item {
                TextButton(onClick = viewModel::addDay, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Day")
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = viewModel::savePlan,
                    enabled = uiState.planName.isNotBlank() && !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save Plan")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    uiState.pickerTargetDayIndex?.let { dayIndex ->
        ExercisePickerDialog(
            exercises = uiState.availableExercises,
            searchQuery = uiState.exerciseSearchQuery,
            onSearchChange = viewModel::onExerciseSearchChange,
            onSelect = { exercise -> viewModel.addExerciseToDay(dayIndex, exercise) },
            onDismiss = viewModel::closeExercisePicker
        )
    }
}

@Composable
internal fun DayCard(
    dayIndex: Int,
    day: DraftDay,
    onLabelChange: (String) -> Unit,
    onRemoveDay: () -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = day.label,
                    onValueChange = onLabelChange,
                    label = { Text("Day ${dayIndex + 1} label") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onRemoveDay) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Day")
                }
            }
            if (day.exercises.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                day.exercises.forEachIndexed { exIndex, de ->
                    DayExerciseRow(
                        name = de.exercise.name,
                        setsTarget = de.setsTarget,
                        onRemove = { onRemoveExercise(exIndex) }
                    )
                }
            }
            TextButton(onClick = onAddExercise) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Exercise")
            }
        }
    }
}

@Composable
internal fun DayExerciseRow(name: String, setsTarget: Int, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$name  ·  $setsTarget sets", style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Remove")
        }
    }
    HorizontalDivider()
}

@Composable
internal fun ExercisePickerDialog(
    exercises: List<Exercise>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    items(exercises, key = { it.id }) { exercise ->
                        TextButton(
                            onClick = { onSelect(exercise); onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(exercise.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${exercise.muscleGroup} · ${exercise.equipmentType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
