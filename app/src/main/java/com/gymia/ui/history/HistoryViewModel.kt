package com.gymia.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.SessionSummary
import com.gymia.domain.usecase.DeleteSessionUseCase
import com.gymia.domain.usecase.GetSessionHistoryUseCase
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
class HistoryViewModel @Inject constructor(
    private val getSessionHistoryUseCase: GetSessionHistoryUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        object Empty : UiState()
        data class Success(val sessions: List<SessionSummary>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        getSessionHistoryUseCase()
            .onEach { sessions ->
                _uiState.value = if (sessions.isEmpty()) UiState.Empty else UiState.Success(sessions)
            }
            .catch { e -> _uiState.value = UiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                deleteSessionUseCase(sessionId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete session")
            }
        }
    }
}
