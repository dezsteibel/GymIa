package com.gymia.ui.workout

import androidx.lifecycle.ViewModel
import com.gymia.domain.usecase.LogSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val logSessionUseCase: LogSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
}
