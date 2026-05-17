package com.gymia.ui.workout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    viewModel: EditPlanViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

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
                    Text(if (uiState.isSaving) "Saving..." else "Save Changes")
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
