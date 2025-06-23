package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
// Import the SelectionMode enum
import id.istts.aplikasiadopsiterumbukarang.presentation.SelectionMode
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CHANGE 1: The UI state now holds a LIST of selected corals to support multi-selection.
data class UserSelectSpeciesUiState(
    val availableCorals: List<Coral> = emptyList(),
    val selectedCorals: List<Coral> = emptyList(), // Changed from selectedCoral: Coral?
    val isLoading: Boolean = false,
    val error: String? = null
)

// CHANGE 2: The ViewModel now accepts a 'selectionMode' in its constructor.
class UserSelectSpeciesViewModel(
    private val coralRepository: CoralRepository,
    private val sessionManager: SessionManager,
    private val selectionMode: SelectionMode // The mode is passed in from the Fragment's factory
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSelectSpeciesUiState())
    val uiState: StateFlow<UserSelectSpeciesUiState> = _uiState.asStateFlow()

    init {
        // Renamed the function for clarity
        loadCoralsBasedOnMode()
    }

    // CHANGE 3: The data loading logic now depends on the mode.
    fun loadCoralsBasedOnMode() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "Authentication token not found. Please login.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = coralRepository.getCoralList(token)

            result.onSuccess { allCorals ->
                // Use a 'when' block to decide how to process the fetched corals
                val coralsToShow = when (selectionMode) {
                    // For users, only show corals that are in stock.
                    SelectionMode.USER_SINGLE_SELECTION -> allCorals.filter { it.stok_tersedia > 0 }
                    // For admins, show ALL corals, regardless of stock.
                    SelectionMode.ADMIN_MULTI_SELECTION -> allCorals
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableCorals = coralsToShow
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

    // CHANGE 4: The selection logic is now 'toggleCoralSelection' and handles both modes.
    fun toggleCoralSelection(tappedCoral: Coral) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedCorals.toMutableList()

            when (selectionMode) {
                // USER LOGIC: Only one selection is allowed.
                SelectionMode.USER_SINGLE_SELECTION -> {
                    // If the tapped coral is already selected, deselect it (clear list).
                    // Otherwise, create a new list containing only the tapped coral.
                    val newSelection = if (currentSelected.contains(tappedCoral)) {
                        emptyList()
                    } else {
                        listOf(tappedCoral)
                    }
                    currentState.copy(selectedCorals = newSelection)
                }
                // ADMIN LOGIC: Multiple selections are allowed.
                SelectionMode.ADMIN_MULTI_SELECTION -> {
                    // If the tapped coral is already in the list, remove it.
                    if (currentSelected.contains(tappedCoral)) {
                        currentSelected.remove(tappedCoral)
                    }
                    // Otherwise, add it to the list.
                    else {
                        currentSelected.add(tappedCoral)
                    }
                    // Update the state with the modified list.
                    currentState.copy(selectedCorals = currentSelected)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}