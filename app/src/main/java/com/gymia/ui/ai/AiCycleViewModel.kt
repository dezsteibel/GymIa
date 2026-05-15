package com.gymia.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.usecase.GenerateCycleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiCycleViewModel @Inject constructor(
    private val generateCycleUseCase: GenerateCycleUseCase
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val cycleJson: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun generateCycle() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            generateCycleUseCase()
                .onSuccess { json -> _uiState.value = UiState.Success(json) }
                .onFailure { error -> _uiState.value = UiState.Error(error.message ?: "Unknown error") }
        }
    }
}
