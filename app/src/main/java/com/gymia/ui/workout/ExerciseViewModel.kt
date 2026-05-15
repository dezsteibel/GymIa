package com.gymia.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.data.model.Exercise
import com.gymia.domain.usecase.DeleteExerciseUseCase
import com.gymia.domain.usecase.GetExercisesUseCase
import com.gymia.domain.usecase.SaveExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseUiState(
    val isLoading: Boolean = true,
    val exercises: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val saveExerciseUseCase: SaveExerciseUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        observeExercises()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeExercises() {
        searchQuery
            .debounce(300)
            .flatMapLatest { query -> getExercisesUseCase(query) }
            .onEach { exercises ->
                _uiState.value = _uiState.value.copy(isLoading = false, exercises = exercises, error = null)
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun saveExercise(name: String, muscleGroup: String, equipmentType: String, id: Long = 0) {
        viewModelScope.launch {
            try {
                saveExerciseUseCase(Exercise(id = id, name = name, muscleGroup = muscleGroup, equipmentType = equipmentType))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            try {
                deleteExerciseUseCase(exercise)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
