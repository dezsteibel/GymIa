package com.gymia.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.GlobalStats
import com.gymia.domain.usecase.GetGlobalStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(val stats: GlobalStats) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getGlobalStatsUseCase: GetGlobalStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = StatsUiState.Loading
            try {
                _uiState.value = StatsUiState.Success(getGlobalStatsUseCase())
            } catch (e: Exception) {
                _uiState.value = StatsUiState.Error(e.message ?: "Failed to load stats")
            }
        }
    }
}
