package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

// The constructor remains the same
class UserAddCoralViewModel(
    private val coralRepository: CoralRepositoryImpl,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCoralUiState())
    val uiState: StateFlow<AddCoralUiState> = _uiState.asStateFlow()

    // NEW: LiveData to hold the selected location's ID and Name.
    private val _selectedLocationId = MutableLiveData<Int?>(null)
    val selectedLocationId: LiveData<Int?> = _selectedLocationId

    private val _selectedLocationName = MutableLiveData<String?>(null)
    val selectedLocationName: LiveData<String?> = _selectedLocationName

    // NEW: LiveData to control the 'Next' button's enabled state.
    private val _isNextButtonEnabled = MutableLiveData<Boolean>(false)
    val isNextButtonEnabled: LiveData<Boolean> = _isNextButtonEnabled


    fun loadCoralDetails(coralId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Session expired. Please login again.") }
                return@launch
            }

            // Using your repository to get the single coral details
            val result = coralRepository.getSingleCoral(coralId, token)

            result.onSuccess { coral ->
                _uiState.update { it.copy(isLoading = false, coral = coral) }
                // MODIFIED: After loading coral details, we must check if the button can be enabled.
                checkIfNextButtonShouldBeEnabled()
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }

    // NEW: This public function is called by the Fragment after the user selects a location.
    fun onLocationSelected(locationId: Int, locationName: String) {
        _selectedLocationId.value = locationId
        _selectedLocationName.value = locationName
        // After updating the location, re-run the validation check.
        checkIfNextButtonShouldBeEnabled()
    }

    // NEW: This private function centralizes the logic for enabling the 'Next' button.
    private fun checkIfNextButtonShouldBeEnabled() {
        val coralIsLoaded = _uiState.value.coral != null
        val locationIsSelected = _selectedLocationId.value != null

        // The button is only enabled if BOTH conditions are true.
        _isNextButtonEnabled.value = coralIsLoaded && locationIsSelected
    }
}