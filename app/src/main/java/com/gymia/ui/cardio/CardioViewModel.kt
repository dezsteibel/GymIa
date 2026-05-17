package com.gymia.ui.cardio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.DomainCardioRecord
import com.gymia.domain.usecase.DeleteCardioUseCase
import com.gymia.domain.usecase.GetCardioHistoryUseCase
import com.gymia.domain.usecase.SaveCardioUseCase
import com.gymia.domain.usecase.UpdateCardioUseCase
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
class CardioViewModel @Inject constructor(
    private val saveCardioUseCase: SaveCardioUseCase,
    private val updateCardioUseCase: UpdateCardioUseCase,
    private val deleteCardioUseCase: DeleteCardioUseCase,
    private val getCardioHistoryUseCase: GetCardioHistoryUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Ready(
            val history: List<DomainCardioRecord>,
            val showForm: Boolean = false,
            val editingRecord: DomainCardioRecord? = null,
            val recordToDelete: DomainCardioRecord? = null,
            val activityType: String = ActivityType.RUN,
            val durationInput: String = "",
            val distanceInput: String = "",
            val notesInput: String = "",
            val isSaving: Boolean = false
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    object ActivityType {
        const val RUN = "run"
        const val BIKE = "bike"
        const val ROW = "row"
        const val OTHER = "other"
        val all = listOf(RUN, BIKE, ROW, OTHER)
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        getCardioHistoryUseCase()
            .onEach { records ->
                val current = _uiState.value
                _uiState.value = when (current) {
                    is UiState.Ready -> current.copy(history = records)
                    else -> UiState.Ready(history = records)
                }
            }
            .catch { e -> _uiState.value = UiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }

    fun showForm() {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(showForm = true, editingRecord = null)
    }

    fun showEditForm(record: DomainCardioRecord) {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(
            showForm = true,
            editingRecord = record,
            activityType = record.activityType,
            durationInput = record.durationMinutes.toString(),
            distanceInput = record.distanceKm?.toString() ?: "",
            notesInput = record.notes ?: ""
        )
    }

    fun hideForm() {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(
            showForm = false,
            editingRecord = null,
            activityType = ActivityType.RUN,
            durationInput = "",
            distanceInput = "",
            notesInput = ""
        )
    }

    fun updateActivityType(type: String) {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(activityType = type)
    }

    fun updateDuration(value: String) {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(durationInput = value)
    }

    fun updateDistance(value: String) {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(distanceInput = value)
    }

    fun updateNotes(value: String) {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(notesInput = value)
    }

    fun saveRecord() {
        val current = _uiState.value as? UiState.Ready ?: return
        val duration = current.durationInput.toIntOrNull() ?: return
        val record = buildRecordFromForm(current, duration)
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            try {
                if (current.editingRecord != null) updateCardioUseCase(record)
                else saveCardioUseCase(record)
                clearFormOnSuccess()
            } catch (e: Exception) {
                (_uiState.value as? UiState.Ready)?.let { _uiState.value = it.copy(isSaving = false) }
            }
        }
    }

    fun requestDeleteRecord(record: DomainCardioRecord) {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(recordToDelete = record)
    }

    fun cancelDelete() {
        val current = _uiState.value as? UiState.Ready ?: return
        _uiState.value = current.copy(recordToDelete = null)
    }

    fun confirmDelete() {
        val current = _uiState.value as? UiState.Ready ?: return
        val record = current.recordToDelete ?: return
        viewModelScope.launch {
            try {
                deleteCardioUseCase(record)
            } finally {
                (_uiState.value as? UiState.Ready)?.let { _uiState.value = it.copy(recordToDelete = null) }
            }
        }
    }

    private fun buildRecordFromForm(current: UiState.Ready, duration: Int): DomainCardioRecord =
        DomainCardioRecord(
            id = current.editingRecord?.id ?: 0,
            date = current.editingRecord?.date ?: System.currentTimeMillis(),
            activityType = current.activityType,
            durationMinutes = duration,
            distanceKm = current.distanceInput.toFloatOrNull(),
            notes = current.notesInput.ifBlank { null }
        )

    private fun clearFormOnSuccess() {
        (_uiState.value as? UiState.Ready)?.let { ready ->
            _uiState.value = ready.copy(
                isSaving = false,
                showForm = false,
                editingRecord = null,
                activityType = ActivityType.RUN,
                durationInput = "",
                distanceInput = "",
                notesInput = ""
            )
        }
    }
}
