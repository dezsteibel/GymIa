package com.gymia.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.ExerciseProgress
import com.gymia.domain.model.WeeklyVolumePoint
import com.gymia.domain.usecase.GetProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressUseCase: GetProgressUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        object Empty : UiState()
        data class Success(
            val exerciseProgresses: List<ExerciseProgress>,
            val weeklyVolumes: List<WeeklyVolumePoint>,
            val cycleSessionCount: Int,
            val cycleTotalVolume: Float,
            val selectedExerciseIndex: Int = 0
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        getProgressUseCase()
            .onEach { summary ->
                _uiState.value = if (summary.exerciseProgresses.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(
                        exerciseProgresses = summary.exerciseProgresses,
                        weeklyVolumes = summary.weeklyVolumes,
                        cycleSessionCount = summary.cycleSessionCount,
                        cycleTotalVolume = summary.cycleTotalVolume
                    )
                }
            }
            .catch { e -> _uiState.value = UiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }

    fun selectExercise(index: Int) {
        val current = _uiState.value as? UiState.Success ?: return
        _uiState.value = current.copy(selectedExerciseIndex = index)
    }
}
