package com.gymia.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.usecase.DeleteWorkoutPlanUseCase
import com.gymia.domain.usecase.DuplicatePlanUseCase
import com.gymia.domain.usecase.GetWorkoutPlansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getWorkoutPlansUseCase: GetWorkoutPlansUseCase,
    private val deleteWorkoutPlanUseCase: DeleteWorkoutPlanUseCase,
    private val duplicatePlanUseCase: DuplicatePlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadPlans()
    }

    private fun loadPlans() {
        getWorkoutPlansUseCase()
            .onEach { plans -> _uiState.value = WorkoutUiState.Success(plans) }
            .catch { e -> _uiState.value = WorkoutUiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            try {
                deleteWorkoutPlanUseCase(planId)
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.message ?: "Failed to delete plan")
            }
        }
    }

    fun duplicatePlan(planId: Long) {
        viewModelScope.launch {
            try {
                duplicatePlanUseCase(planId)
                _snackbarMessage.value = "Plan duplicated"
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to duplicate plan"
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
