package com.gymia.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.data.model.Exercise
import com.gymia.data.model.WorkoutPlan
import com.gymia.domain.model.DayExerciseInput
import com.gymia.domain.model.DayInput
import com.gymia.domain.usecase.GetExercisesUseCase
import com.gymia.domain.usecase.SaveWorkoutPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DraftDayExercise(val exercise: Exercise, val setsTarget: Int = 3, val dayExerciseId: Long = 0)

data class DraftDay(val label: String = "", val exercises: List<DraftDayExercise> = emptyList())

data class CreatePlanUiState(
    val planName: String = "",
    val days: List<DraftDay> = listOf(DraftDay()),
    val availableExercises: List<Exercise> = emptyList(),
    val exerciseSearchQuery: String = "",
    val pickerTargetDayIndex: Int? = null,
    val isSaving: Boolean = false,
    val savedPlanId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class CreatePlanViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val saveWorkoutPlanUseCase: SaveWorkoutPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlanUiState())
    val uiState: StateFlow<CreatePlanUiState> = _uiState.asStateFlow()

    private val exerciseSearchQuery = MutableStateFlow("")

    init { observeExercises() }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeExercises() {
        exerciseSearchQuery
            .debounce(300)
            .flatMapLatest { query -> getExercisesUseCase(query) }
            .onEach { exercises -> _uiState.value = _uiState.value.copy(availableExercises = exercises) }
            .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
            .launchIn(viewModelScope)
    }

    fun onPlanNameChange(name: String) {
        _uiState.value = _uiState.value.copy(planName = name)
    }

    fun addDay() {
        _uiState.value = _uiState.value.copy(days = _uiState.value.days + DraftDay())
    }

    fun removeDay(index: Int) {
        val days = _uiState.value.days.toMutableList().also { it.removeAt(index) }
        _uiState.value = _uiState.value.copy(days = days)
    }

    fun onDayLabelChange(index: Int, label: String) {
        val days = _uiState.value.days.toMutableList()
        days[index] = days[index].copy(label = label)
        _uiState.value = _uiState.value.copy(days = days)
    }

    fun openExercisePicker(dayIndex: Int) {
        exerciseSearchQuery.value = ""
        _uiState.value = _uiState.value.copy(pickerTargetDayIndex = dayIndex, exerciseSearchQuery = "")
    }

    fun closeExercisePicker() {
        _uiState.value = _uiState.value.copy(pickerTargetDayIndex = null)
    }

    fun onExerciseSearchChange(query: String) {
        exerciseSearchQuery.value = query
        _uiState.value = _uiState.value.copy(exerciseSearchQuery = query)
    }

    fun addExerciseToDay(dayIndex: Int, exercise: Exercise) {
        val days = _uiState.value.days.toMutableList()
        val day = days[dayIndex]
        if (day.exercises.none { it.exercise.id == exercise.id }) {
            days[dayIndex] = day.copy(exercises = day.exercises + DraftDayExercise(exercise))
        }
        _uiState.value = _uiState.value.copy(days = days)
    }

    fun removeExerciseFromDay(dayIndex: Int, exerciseIndex: Int) {
        val days = _uiState.value.days.toMutableList()
        val exercises = days[dayIndex].exercises.toMutableList().also { it.removeAt(exerciseIndex) }
        days[dayIndex] = days[dayIndex].copy(exercises = exercises)
        _uiState.value = _uiState.value.copy(days = days)
    }

    fun savePlan() {
        val state = _uiState.value
        if (state.planName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            try {
                val plan = WorkoutPlan(name = state.planName.trim())
                val dayInputs = state.days.mapIndexed { index, draft ->
                    DayInput(
                        label = draft.label.ifBlank { "Day ${index + 1}" },
                        exercises = draft.exercises.mapIndexed { exIndex, de ->
                            DayExerciseInput(exerciseId = de.exercise.id, setsTarget = de.setsTarget, order = exIndex)
                        }
                    )
                }
                val planId = saveWorkoutPlanUseCase(plan, dayInputs)
                _uiState.value = _uiState.value.copy(isSaving = false, savedPlanId = planId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
