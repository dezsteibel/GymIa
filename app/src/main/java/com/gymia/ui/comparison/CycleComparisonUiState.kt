package com.gymia.ui.comparison

import com.gymia.domain.model.CycleComparison

sealed class CycleComparisonUiState {
    object Loading : CycleComparisonUiState()
    object Empty : CycleComparisonUiState()
    data class Success(val comparison: CycleComparison) : CycleComparisonUiState()
    data class Error(val message: String) : CycleComparisonUiState()
}
