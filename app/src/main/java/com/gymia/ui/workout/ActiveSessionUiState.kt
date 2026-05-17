package com.gymia.ui.workout

import com.gymia.domain.model.DomainExercise
import com.gymia.domain.model.LoadSuggestion

data class ActiveSessionUiState(
    val isLoading: Boolean = true,
    val dayLabel: String = "",
    val exercises: List<ExerciseSessionState> = emptyList(),
    val restTimerSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val selectedRestDuration: Int = 90,
    val isSaving: Boolean = false,
    val sessionFinished: Boolean = false,
    val error: String? = null,
    val elapsedSeconds: Int = 0,
    val notesDialogExerciseIndex: Int? = null,
    val notesDialogSetIndex: Int? = null,
    val notesDialogText: String = ""
)

data class ExerciseSessionState(
    val exercise: DomainExercise,
    val setsTarget: Int,
    val loggedSets: List<SetEntry>,
    val lastBestLoad: Float? = null,
    val loadSuggestion: LoadSuggestion? = null
)

data class SetEntry(
    val setNumber: Int,
    val reps: String = "",
    val loadKg: String = "",
    val isConfirmed: Boolean = false,
    val notes: String = ""
)
