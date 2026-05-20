package com.gymia.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.data.local.DeloadDismissalDataSource
import com.gymia.domain.model.DeloadReason
import com.gymia.domain.model.DeloadSuggestion
import com.gymia.domain.model.ExerciseProgress
import com.gymia.domain.model.WeeklyVolumePoint
import com.gymia.domain.usecase.DetectDeloadNeedUseCase
import com.gymia.domain.usecase.GetProgressUseCase
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
class ProgressViewModel @Inject constructor(
    private val getProgressUseCase: GetProgressUseCase,
    private val detectDeloadNeedUseCase: DetectDeloadNeedUseCase,
    private val deloadDismissalDataSource: DeloadDismissalDataSource
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        object Empty : UiState()
        data class Success(
            val exerciseProgresses: List<ExerciseProgress>,
            val weeklyVolumes: List<WeeklyVolumePoint>,
            val cycleSessionCount: Int,
            val cycleTotalVolume: Float,
            val selectedExerciseIndex: Int = 0,
            val deloadSuggestion: DeloadSuggestion? = null,
            val isDeloadDismissed: Boolean = false
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadProgress()
        loadDeloadState()
    }

    private fun loadProgress() {
        getProgressUseCase()
            .onEach { summary ->
                _uiState.value = if (summary.exerciseProgresses.isEmpty()) UiState.Empty
                else UiState.Success(
                    exerciseProgresses = summary.exerciseProgresses,
                    weeklyVolumes = summary.weeklyVolumes,
                    cycleSessionCount = summary.cycleSessionCount,
                    cycleTotalVolume = summary.cycleTotalVolume
                )
            }
            .catch { e -> _uiState.value = UiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }

    private fun loadDeloadState() {
        viewModelScope.launch {
            val suggestion = runCatching { detectDeloadNeedUseCase() }
                .getOrDefault(DeloadSuggestion(false, DeloadReason.NONE, emptyList()))
            deloadDismissalDataSource.isDismissed()
                .onEach { dismissed ->
                    val current = _uiState.value as? UiState.Success ?: return@onEach
                    _uiState.value = current.copy(deloadSuggestion = suggestion, isDeloadDismissed = dismissed)
                }
                .launchIn(viewModelScope)
        }
    }

    fun selectExercise(index: Int) {
        val current = _uiState.value as? UiState.Success ?: return
        _uiState.value = current.copy(selectedExerciseIndex = index)
    }

    fun dismissDeload() {
        viewModelScope.launch { runCatching { deloadDismissalDataSource.dismissFor7Days() } }
    }
}
