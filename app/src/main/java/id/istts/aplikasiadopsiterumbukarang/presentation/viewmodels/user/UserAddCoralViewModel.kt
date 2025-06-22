package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl // Or your repository interface
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI State for this specific screen
data class AddCoralUiState(
    val coral: Coral? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Add SessionManager to the constructor
class UserAddCoralViewModel(
    private val coralRepository: CoralRepositoryImpl,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddCoralUiState())
    val uiState: StateFlow<AddCoralUiState> = _uiState.asStateFlow()

    fun loadCoralDetails(coralId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Get the token from SessionManager
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Session expired. Please login again.") }
                return@launch
            }

            // Use the correct function name: getSingleCoral
            val result = coralRepository.getSingleCoral(coralId, token)

            result.onSuccess { coral ->
                // The API response has a 'corral' property, so we use that.
                _uiState.update { it.copy(isLoading = false, coral = coral) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }
}