package com.gymia.ui.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.usecase.ExportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class ExportUiState {
    object Idle : ExportUiState()
    object Exporting : ExportUiState()
    data class Success(val filePath: String, val file: File) : ExportUiState()
    data class Error(val message: String) : ExportUiState()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun exportData(context: Context) {
        viewModelScope.launch {
            _uiState.value = ExportUiState.Exporting
            try {
                val jsonString = exportDataUseCase()
                val file = withContext(Dispatchers.IO) {
                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val dir = context.getExternalFilesDir(null) ?: context.filesDir
                    val outFile = File(dir, "gymia_backup_$dateStr.json")
                    outFile.writeText(jsonString)
                    outFile
                }
                _uiState.value = ExportUiState.Success(file.absolutePath, file)
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = ExportUiState.Idle
    }
}
