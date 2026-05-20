package com.gymia.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.WorkoutTemplate
import com.gymia.domain.usecase.CreatePlanFromTemplateUseCase
import com.gymia.domain.usecase.GetWorkoutTemplatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val getTemplates: GetWorkoutTemplatesUseCase,
    private val createPlan: CreatePlanFromTemplateUseCase
) : ViewModel() {

    sealed class UiState {
        data class Loaded(
            val templates: List<WorkoutTemplate>,
            val selectedTemplate: WorkoutTemplate? = null,
            val isCreating: Boolean = false
        ) : UiState()
        object PlanCreated : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loaded(getTemplates()))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun selectTemplate(template: WorkoutTemplate) {
        val current = _uiState.value as? UiState.Loaded ?: return
        _uiState.value = current.copy(selectedTemplate = template)
    }

    fun dismissDetail() {
        val current = _uiState.value as? UiState.Loaded ?: return
        _uiState.value = current.copy(selectedTemplate = null)
    }

    fun createPlanFromTemplate(template: WorkoutTemplate) {
        val current = _uiState.value as? UiState.Loaded ?: return
        _uiState.value = current.copy(isCreating = true, selectedTemplate = null)
        viewModelScope.launch {
            runCatching { createPlan(template) }
                .onSuccess { _uiState.value = UiState.PlanCreated }
                .onFailure { e -> _uiState.value = UiState.Error(e.message ?: "Failed to create plan") }
        }
    }
}
