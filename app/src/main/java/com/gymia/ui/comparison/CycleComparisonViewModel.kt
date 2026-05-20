package com.gymia.ui.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.usecase.GetCycleComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleComparisonViewModel @Inject constructor(
    private val getCycleComparisonUseCase: GetCycleComparisonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CycleComparisonUiState>(CycleComparisonUiState.Loading)
    val uiState: StateFlow<CycleComparisonUiState> = _uiState.asStateFlow()

    init {
        loadComparison()
    }

    private fun loadComparison() {
        viewModelScope.launch {
            runCatching { getCycleComparisonUseCase() }
                .onSuccess { comparison ->
                    _uiState.value = if (comparison?.previousCycle != null) {
                        CycleComparisonUiState.Success(comparison)
                    } else {
                        CycleComparisonUiState.Empty
                    }
                }
                .onFailure { error ->
                    _uiState.value = CycleComparisonUiState.Error(
                        error.message ?: "Failed to load comparison"
                    )
                }
        }
    }
}
