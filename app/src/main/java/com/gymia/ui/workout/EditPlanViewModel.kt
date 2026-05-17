package com.gymia.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.data.model.Exercise
import com.gymia.domain.model.DayExerciseInput
import com.gymia.domain.model.DayInput
import com.gymia.domain.model.ExerciseOrderUpdate
import com.gymia.domain.usecase.GetExercisesUseCase
import com.gymia.domain.usecase.UpdateExerciseOrderUseCase
import com.gymia.domain.usecase.UpdateWorkoutPlanUseCase
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

data class EditPlanUiState(
    val planId: Long = 0L,
    val planName: String = "",
    val days: List<DraftDay> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val exerciseSearchQuery: String = "",
    val pickerTargetDayIndex: Int? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditPlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getExercisesUseCase: GetExercisesUseCase,
    private val updateWorkoutPlanUseCase: UpdateWorkoutPlanUseCase,
    private val updateExerciseOrderUseCase: UpdateExerciseOrderUseCase,
    private val workoutRepository: com.gymia.data.repository.WorkoutRepository
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _uiState = MutableStateFlow(EditPlanUiState(planId = planId))
    val uiState: StateFlow<EditPlanUiState> = _uiState.asStateFlow()

    private val exerciseSearchQuery = MutableStateFlow("")

    init {
        loadPlan()
        observeExercises()
    }

    private fun loadPlan() {
        viewModelScope.launch {
            try {
                val planWithDays = workoutRepository.getPlanWithDaysOnce(planId) ?: return@launch
                val draftDays = planWithDays.days.map { day ->
                    val exercisesForDay = workoutRepository.getDayExercisesOnce(day.id)
                    DraftDay(
                        label = day.label,
                        exercises = exercisesForDay.map { ef ->
                            DraftDayExercise(
                                exercise = Exercise(
                                    id = ef.exercise.id,
                                    name = ef.exercise.name,
                                    muscleGroup = ef.exercise.muscleGroup,
                                    equipmentType = ef.exercise.equipmentType
                                ),
                                setsTarget = ef.setsTarget,
                                dayExerciseId = ef.dayExerciseId
                            )
                        }
                    )
                }
                _uiState.value = _uiState.value.copy(
                    planName = planWithDays.plan.name,
                    days = draftDays,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

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

    fun reorderExercises(dayIndex: Int, from: Int, to: Int) {
        val days = _uiState.value.days.toMutableList()
        val exercises = days[dayIndex].exercises.toMutableList()
        val moved = exercises.removeAt(from)
        exercises.add(to, moved)
        days[dayIndex] = days[dayIndex].copy(exercises = exercises)
        _uiState.value = _uiState.value.copy(days = days)
        persistExerciseOrder(dayIndex, exercises)
    }

    private fun persistExerciseOrder(dayIndex: Int, exercises: List<DraftDayExercise>) {
        if (exercises.any { it.dayExerciseId == 0L }) return
        viewModelScope.launch {
            try {
                val updates = exercises.mapIndexed { index, de ->
                    ExerciseOrderUpdate(id = de.dayExerciseId, order = index)
                }
                updateExerciseOrderUseCase(updates)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun savePlan() {
        val state = _uiState.value
        if (state.planName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            try {
                val dayInputs = state.days.mapIndexed { index, draft ->
                    DayInput(
                        label = draft.label.ifBlank { "Day ${index + 1}" },
                        exercises = draft.exercises.mapIndexed { exIndex, de ->
                            DayExerciseInput(exerciseId = de.exercise.id, setsTarget = de.setsTarget, order = exIndex)
                        }
                    )
                }
                updateWorkoutPlanUseCase(planId, state.planName.trim(), dayInputs)
                _uiState.value = _uiState.value.copy(isSaving = false, savedSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
