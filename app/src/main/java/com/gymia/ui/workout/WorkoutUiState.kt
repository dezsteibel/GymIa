package com.gymia.ui.workout

import com.gymia.data.model.WorkoutPlan

sealed class WorkoutUiState {
    object Loading : WorkoutUiState()
    data class Success(val plans: List<WorkoutPlan>) : WorkoutUiState()
    data class Error(val message: String) : WorkoutUiState()
}
