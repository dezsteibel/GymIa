package com.gymia.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.WorkoutCycle
import com.gymia.domain.usecase.GenerateCycleUseCase
import com.gymia.domain.usecase.SaveAiCycleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiCycleViewModel @Inject constructor(
    private val generateCycleUseCase: GenerateCycleUseCase,
    private val saveAiCycleUseCase: SaveAiCycleUseCase
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val cycle: WorkoutCycle) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigateToWorkout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToWorkout: SharedFlow<Unit> = _navigateToWorkout.asSharedFlow()

    fun generateCycle() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            generateCycleUseCase()
                .onSuccess { cycle -> _uiState.value = UiState.Success(cycle) }
                .onFailure { error -> _uiState.value = UiState.Error(error.message ?: "Unknown error") }
        }
    }

    fun acceptCycle(cycle: WorkoutCycle) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching { saveAiCycleUseCase(cycle) }
                .onSuccess {
                    _uiState.value = UiState.Idle
                    _navigateToWorkout.emit(Unit)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to save cycle")
                }
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }
}
