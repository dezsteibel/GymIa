package com.gymia.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutSession
import com.gymia.domain.usecase.GetExercisesForDayUseCase
import com.gymia.domain.usecase.GetLastLoadForExerciseUseCase
import com.gymia.domain.usecase.GetWorkoutDayUseCase
import com.gymia.domain.usecase.LogSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getExercisesForDayUseCase: GetExercisesForDayUseCase,
    private val getLastLoadForExerciseUseCase: GetLastLoadForExerciseUseCase,
    private val getWorkoutDayUseCase: GetWorkoutDayUseCase,
    private val logSessionUseCase: LogSessionUseCase
) : ViewModel() {

    private val dayId: Long = checkNotNull(savedStateHandle["dayId"])
    private val sessionStartTime = System.currentTimeMillis()
    private var currentPlanId: Long? = null
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    init {
        loadDayInfo()
        loadExercises()
    }

    private fun loadDayInfo() {
        viewModelScope.launch {
            try {
                val day = getWorkoutDayUseCase(dayId)
                currentPlanId = day?.planId
                _uiState.value = _uiState.value.copy(dayLabel = day?.label ?: "Workout")
            } catch (_: Exception) {}
        }
    }

    private fun loadExercises() {
        getExercisesForDayUseCase(dayId)
            .onEach { exercises ->
                val states = exercises.map { efd ->
                    val lastLoad = runCatching { getLastLoadForExerciseUseCase(efd.exercise.id) }.getOrNull()
                    ExerciseSessionState(
                        exercise = efd.exercise,
                        setsTarget = efd.setsTarget,
                        loggedSets = listOf(SetEntry(setNumber = 1, loadKg = lastLoad?.toString() ?: "")),
                        lastBestLoad = lastLoad
                    )
                }
                _uiState.value = _uiState.value.copy(isLoading = false, exercises = states)
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            .launchIn(viewModelScope)
    }

    fun onRepsChange(exerciseIndex: Int, setIndex: Int, value: String) {
        updateSet(exerciseIndex, setIndex) { it.copy(reps = value) }
    }

    fun onLoadChange(exerciseIndex: Int, setIndex: Int, value: String) {
        updateSet(exerciseIndex, setIndex) { it.copy(loadKg = value) }
    }

    fun confirmSet(exerciseIndex: Int, setIndex: Int) {
        updateSet(exerciseIndex, setIndex) { it.copy(isConfirmed = true) }
        startRestTimer()
    }

    fun addSet(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val exercise = exercises[exerciseIndex]
        val lastLoad = exercise.loggedSets.lastOrNull()?.loadKg ?: exercise.lastBestLoad?.toString() ?: ""
        val newSet = SetEntry(setNumber = exercise.loggedSets.size + 1, loadKg = lastLoad)
        exercises[exerciseIndex] = exercise.copy(loggedSets = exercise.loggedSets + newSet)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun startRestTimer(durationSeconds: Int = _uiState.value.selectedRestDuration) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = true, restTimerSeconds = durationSeconds)
        timerJob = viewModelScope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(restTimerSeconds = remaining)
            }
            _uiState.value = _uiState.value.copy(isTimerRunning = false, restTimerSeconds = 0)
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = false, restTimerSeconds = 0)
    }

    fun onRestDurationChange(seconds: Int) {
        _uiState.value = _uiState.value.copy(selectedRestDuration = seconds)
    }

    fun finishSession() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            try {
                val durationMinutes = ((System.currentTimeMillis() - sessionStartTime) / 60000).toInt()
                val session = WorkoutSession(planId = currentPlanId, dayId = dayId, durationMinutes = durationMinutes)
                val sets = buildSetRecords(state)
                logSessionUseCase(session, sets)
                _uiState.value = _uiState.value.copy(isSaving = false, sessionFinished = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    private fun buildSetRecords(state: ActiveSessionUiState): List<SetRecord> =
        state.exercises.flatMap { exerciseState ->
            exerciseState.loggedSets
                .filter { it.isConfirmed }
                .mapNotNull { entry ->
                    val reps = entry.reps.toIntOrNull() ?: return@mapNotNull null
                    val load = entry.loadKg.toFloatOrNull() ?: return@mapNotNull null
                    SetRecord(sessionId = 0, exerciseId = exerciseState.exercise.id, setNumber = entry.setNumber, reps = reps, loadKg = load)
                }
        }

    private fun updateSet(exerciseIndex: Int, setIndex: Int, transform: (SetEntry) -> SetEntry) {
        val exercises = _uiState.value.exercises.toMutableList()
        val sets = exercises[exerciseIndex].loggedSets.toMutableList()
        sets[setIndex] = transform(sets[setIndex])
        exercises[exerciseIndex] = exercises[exerciseIndex].copy(loggedSets = sets)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }
}
