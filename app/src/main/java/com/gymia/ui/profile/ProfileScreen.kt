package com.gymia.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymia.domain.model.ExperienceLevel
import com.gymia.domain.model.Goal
import com.gymia.domain.model.UserProfile
import kotlin.math.roundToInt

private const val TRAINING_DAYS_MIN = 2f
private const val TRAINING_DAYS_MAX = 6f
private const val TRAINING_DAYS_STEPS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val state = uiState as? ProfileViewModel.UiState.Loaded ?: return@LaunchedEffect
        if (state.showSaved) {
            snackbarHostState.showSnackbar("Profile saved")
            viewModel.clearSavedAck()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProfileViewModel.UiState.Loading -> LoadingIndicator(innerPadding)
            is ProfileViewModel.UiState.Loaded -> ProfileForm(
                profile = state.profile,
                onSave = { viewModel.save(it) },
                modifier = Modifier.padding(innerPadding)
            )
            is ProfileViewModel.UiState.Error -> ErrorMessage(state.message, innerPadding)
        }
    }
}

@Composable
private fun LoadingIndicator(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String, padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileForm(
    profile: UserProfile,
    onSave: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var formInitialized by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf(Goal.MIXED) }
    var experienceLevel by remember { mutableStateOf(ExperienceLevel.INTERMEDIATE) }
    var trainingDays by remember { mutableFloatStateOf(4f) }
    var bodyWeight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    if (!formInitialized) {
        name = profile.name
        goal = profile.goal
        experienceLevel = profile.experienceLevel
        trainingDays = profile.trainingDaysPerWeek.toFloat()
        bodyWeight = profile.bodyWeightKg?.toString() ?: ""
        notes = profile.notes ?: ""
        formInitialized = true
    }

    val currentProfile = UserProfile(
        name = name,
        goal = goal,
        experienceLevel = experienceLevel,
        trainingDaysPerWeek = trainingDays.roundToInt(),
        bodyWeightKg = bodyWeight.toFloatOrNull(),
        notes = notes.takeIf { it.isNotBlank() }
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { NameField(name, onValueChange = { name = it }) }
        item { GoalSelector(goal, onGoalSelected = { goal = it }) }
        item { ExperienceLevelSelector(experienceLevel, onLevelSelected = { experienceLevel = it }) }
        item { TrainingDaysSlider(trainingDays, onValueChange = { trainingDays = it }) }
        item { BodyWeightField(bodyWeight, onValueChange = { bodyWeight = it }) }
        item { NotesField(notes, onValueChange = { notes = it }) }
        item { SaveButton(onClick = { onSave(currentProfile) }) }
    }
}

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalSelector(selected: Goal, onGoalSelected: (Goal) -> Unit) {
    Text("Goal")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        Goal.entries.forEachIndexed { index, goal ->
            SegmentedButton(
                selected = selected == goal,
                onClick = { onGoalSelected(goal) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = Goal.entries.size),
                label = { Text(goalLabel(goal)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceLevelSelector(
    selected: ExperienceLevel,
    onLevelSelected: (ExperienceLevel) -> Unit
) {
    Text("Experience Level")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ExperienceLevel.entries.forEachIndexed { index, level ->
            SegmentedButton(
                selected = selected == level,
                onClick = { onLevelSelected(level) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ExperienceLevel.entries.size),
                label = { Text(goalLabel(level)) }
            )
        }
    }
}

@Composable
private fun TrainingDaysSlider(value: Float, onValueChange: (Float) -> Unit) {
    Text("Training days per week: ${value.roundToInt()}")
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = TRAINING_DAYS_MIN..TRAINING_DAYS_MAX,
        steps = TRAINING_DAYS_STEPS,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BodyWeightField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Body weight (kg) — optional") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}

@Composable
private fun NotesField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Notes") },
        placeholder = { Text("Injuries, preferences, equipment...") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4
    )
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Save")
    }
}

private fun goalLabel(value: Any): String =
    value.toString().lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
