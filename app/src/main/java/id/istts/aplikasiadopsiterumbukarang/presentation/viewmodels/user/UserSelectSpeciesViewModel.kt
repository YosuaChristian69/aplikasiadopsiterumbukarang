package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. A simple UI State data class, just for this screen's needs.
data class UserSelectSpeciesUiState(
    val availableCorals: List<Coral> = emptyList(),
    val selectedCoral: Coral? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// 2. The ViewModel, with only the dependencies it needs.
class UserSelectSpeciesViewModel(
    private val coralRepository: CoralRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSelectSpeciesUiState())
    val uiState: StateFlow<UserSelectSpeciesUiState> = _uiState.asStateFlow()

    init {
        loadAvailableCorals()
    }

    fun loadAvailableCorals() {
        val token = sessionManager.fetchAuthToken()

        // Same robust token check as your Admin VM
        if (token.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "Authentication token not found. Please login.") }
            // Here you might want a navigation event to the login screen as well
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Reusing the exact same repository call
            val result = coralRepository.getCoralList(token)

            result.onSuccess { allCorals ->
                // *** The key difference: Filter the list for users ***
                val availableCorals = allCorals.filter { it.stok_tersedia > 0 }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableCorals = availableCorals
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load corals: ${exception.message}"
                    )
                }
            }
        }
    }

    // Logic to handle when a user taps on a coral in the RecyclerView
    fun onCoralSelected(coral: Coral) {
        _uiState.update { currentState ->
            // If the user taps the same coral again, deselect it. Otherwise, select the new one.
            val newSelection = if (currentState.selectedCoral?.id_tk == coral.id_tk) null else coral
            currentState.copy(selectedCoral = newSelection)
        }
    }

    // Used to clear the error message after it has been shown in a Toast
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}