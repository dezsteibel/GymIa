package com.gymia.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymia.domain.model.UserProfile
import com.gymia.domain.usecase.GetUserProfileUseCase
import com.gymia.domain.usecase.SaveUserProfileUseCase
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
class ProfileViewModel @Inject constructor(
    private val getProfile: GetUserProfileUseCase,
    private val saveProfile: SaveUserProfileUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Loaded(val profile: UserProfile, val showSaved: Boolean = false) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var pendingSaveAck = false

    init { observeProfile() }

    private fun observeProfile() {
        getProfile()
            .onEach { profile ->
                val showSaved = pendingSaveAck.also { pendingSaveAck = false }
                _uiState.value = UiState.Loaded(profile, showSaved)
            }
            .catch { e -> _uiState.value = UiState.Error(e.message ?: "Unknown error") }
            .launchIn(viewModelScope)
    }

    fun save(profile: UserProfile) {
        viewModelScope.launch {
            pendingSaveAck = true
            runCatching { saveProfile(profile) }
                .onFailure { e ->
                    pendingSaveAck = false
                    _uiState.value = UiState.Error(e.message ?: "Save failed")
                }
        }
    }

    fun clearSavedAck() {
        val current = _uiState.value as? UiState.Loaded ?: return
        _uiState.value = current.copy(showSaved = false)
    }
}
