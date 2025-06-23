package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.repositories.LocationRepository // Use your repository interface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

// UI State for this screen
data class LocationSelectionUiState(
    val availableLocations: List<Lokasi> = emptyList(),
    val selectedLocation: Lokasi? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LocationSelectionViewModel(
    private val coralId: Int,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationSelectionUiState())
    val uiState: StateFlow<LocationSelectionUiState> = _uiState.asStateFlow()

    init {
        fetchAvailableLocations()
    }

    private fun fetchAvailableLocations() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // This would call your backend API: GET /.../?id_tk={coralId}
                val locations = locationRepository.getLocationsForCoral(coralId)
                _uiState.update { it.copy(isLoading = false, availableLocations = locations) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onLocationSelected(location: Lokasi) {
        _uiState.update { it.copy(selectedLocation = location) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}



class LocationSelectionViewModelFactory(
    private val coralId: Int,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationSelectionViewModel(coralId, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}