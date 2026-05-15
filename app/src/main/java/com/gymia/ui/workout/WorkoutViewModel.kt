package com.gymia.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.usecase.GetWorkoutPlansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getWorkoutPlansUseCase: GetWorkoutPlansUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    private fun loadPlans() {
        getWorkoutPlansUseCase()
            .onEach { plans -> _uiState.value = WorkoutUiState.Success(plans) }
            .catch { e -> _uiState.value = WorkoutUiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }
}
