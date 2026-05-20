package com.gymia.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.data.model.SetRecord
import com.gymia.data.model.WorkoutSession
import com.gymia.domain.SessionTimer
import com.gymia.domain.usecase.GetExercisesForDayUseCase
import com.gymia.domain.usecase.GetLastLoadForExerciseUseCase
import com.gymia.domain.usecase.GetLoadSuggestionUseCase
import com.gymia.domain.usecase.GetWorkoutDayUseCase
import com.gymia.domain.usecase.LogSessionUseCase
import com.gymia.ui.notification.NotificationHelper
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
    private val getLoadSuggestionUseCase: GetLoadSuggestionUseCase,
    private val getWorkoutDayUseCase: GetWorkoutDayUseCase,
    private val logSessionUseCase: LogSessionUseCase,
    private val sessionTimer: SessionTimer,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val dayId: Long = checkNotNull(savedStateHandle["dayId"])
    private var currentPlanId: Long? = null
    private var timerJob: Job? = null
    private var elapsedJob: Job? = null

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    init {
        sessionTimer.start()
        startElapsedTicker()
        loadDayInfo()
        loadExercises()
    }

    private fun startElapsedTicker() {
        elapsedJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(elapsedSeconds = sessionTimer.elapsed())
            }
        }
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
                states.forEachIndexed { index, state ->
                    fetchSuggestionForExercise(index, state.exercise.id)
                }
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            .launchIn(viewModelScope)
    }

    private fun fetchSuggestionForExercise(exerciseIndex: Int, exerciseId: Long) {
        viewModelScope.launch {
            val suggestion = runCatching { getLoadSuggestionUseCase(exerciseId) }.getOrNull()
            val exercises = _uiState.value.exercises.toMutableList()
            if (exerciseIndex < exercises.size) {
                exercises[exerciseIndex] = exercises[exerciseIndex].copy(loadSuggestion = suggestion)
                _uiState.value = _uiState.value.copy(exercises = exercises)
            }
        }
    }

    fun onSuggestionTap(exerciseIndex: Int, setIndex: Int) {
        val suggestion = _uiState.value.exercises.getOrNull(exerciseIndex)?.loadSuggestion ?: return
        updateSet(exerciseIndex, setIndex) { it.copy(loadKg = "%.1f".format(suggestion.suggestedLoad)) }
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
            notificationHelper.showRestCompleteNotification()
            _uiState.value = _uiState.value.copy(isTimerRunning = false, restTimerSeconds = 0, restTimerCompleted = true)
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = false, restTimerSeconds = 0)
    }

    fun onRestDurationChange(seconds: Int) {
        _uiState.value = _uiState.value.copy(selectedRestDuration = seconds)
    }

    fun clearTimerCompleted() {
        _uiState.value = _uiState.value.copy(restTimerCompleted = false)
    }

    fun openNotesDialog(exerciseIndex: Int, setIndex: Int) {
        val currentNotes = _uiState.value.exercises.getOrNull(exerciseIndex)
            ?.loggedSets?.getOrNull(setIndex)?.notes ?: ""
        _uiState.value = _uiState.value.copy(
            notesDialogExerciseIndex = exerciseIndex,
            notesDialogSetIndex = setIndex,
            notesDialogText = currentNotes
        )
    }

    fun onNotesDialogTextChange(text: String) {
        _uiState.value = _uiState.value.copy(notesDialogText = text)
    }

    fun saveNotes() {
        val state = _uiState.value
        val exerciseIndex = state.notesDialogExerciseIndex ?: return
        val setIndex = state.notesDialogSetIndex ?: return
        updateSet(exerciseIndex, setIndex) { it.copy(notes = state.notesDialogText) }
        _uiState.value = _uiState.value.copy(
            notesDialogExerciseIndex = null,
            notesDialogSetIndex = null,
            notesDialogText = ""
        )
    }

    fun dismissNotesDialog() {
        _uiState.value = _uiState.value.copy(
            notesDialogExerciseIndex = null,
            notesDialogSetIndex = null,
            notesDialogText = ""
        )
    }

    fun finishSession() {
        elapsedJob?.cancel()
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            try {
                val durationMinutes = (sessionTimer.stop() / 60).coerceAtLeast(0)
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
                    SetRecord(
                        sessionId = 0,
                        exerciseId = exerciseState.exercise.id,
                        setNumber = entry.setNumber,
                        reps = reps,
                        loadKg = load,
                        notes = entry.notes.ifBlank { null }
                    )
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
