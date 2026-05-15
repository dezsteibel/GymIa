package com.gymia.ui.workout

import com.gymia.domain.model.PlanWithDays

sealed class WorkoutUiState {
    object Loading : WorkoutUiState()
    data class Success(val plans: List<PlanWithDays>) : WorkoutUiState()
    data class Error(val message: String) : WorkoutUiState()
}
