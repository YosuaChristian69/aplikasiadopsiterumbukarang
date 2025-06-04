package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import CoralRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminDashboardViewModel(
    private val coralRepository: CoralRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    // Simple navigation flags
    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    private val _shouldNavigateToAddCoral = MutableStateFlow(false)
    val shouldNavigateToAddCoral: StateFlow<Boolean> = _shouldNavigateToAddCoral.asStateFlow()

    private val _shouldNavigateToPlace = MutableStateFlow(false)
    val shouldNavigateToPlace: StateFlow<Boolean> = _shouldNavigateToPlace.asStateFlow()

    private val _shouldNavigateToWorker = MutableStateFlow(false)
    val shouldNavigateToWorker: StateFlow<Boolean> = _shouldNavigateToWorker.asStateFlow()

    private val _selectedCoral = MutableStateFlow<Coral?>(null)
    val selectedCoral: StateFlow<Coral?> = _selectedCoral.asStateFlow()

    init {
        validateUserAccess()
    }

    fun validateUserAccess() {
        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "admin") {
            _shouldNavigateToLogin.value = true
        } else {
            loadUserInfo()
            loadCoralData()
        }
    }

    private fun loadUserInfo() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        _uiState.value = _uiState.value.copy(
            userName = userName,
            welcomeMessage = "Hi, $userName"
        )
    }

    fun loadCoralData() {
        val token = sessionManager.fetchAuthToken()

        if (token.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Authentication token not found"
            )
            _shouldNavigateToLogin.value = true
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val result = coralRepository.getCoralList(authToken)

                if (result.isSuccess) {
                    val coralList = result.getOrNull() ?: emptyList()
                    val totalCorals = coralList.size
                    val lowStockCount = coralList.count { it.stok_tersedia < 10 }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        coralList = coralList,
                        collectionTitle = "MY CORAL'S SEEDS COLLECTION ($totalCorals)",
                        totalCorals = totalCorals,
                        lowStockCount = lowStockCount,
                        error = null
                    )
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Unknown error occurred"

                    if (errorMessage.contains("Invalid or Expired Token") || errorMessage.contains("401")) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Session expired. Please login again."
                        )
                        _shouldNavigateToLogin.value = true
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load coral data: $errorMessage"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    // Simple navigation functions
    fun onCoralItemClick(coral: Coral) {
        _selectedCoral.value = coral
    }

    fun onLogoutClick() {
        sessionManager.clearSession()
        _shouldNavigateToLogin.value = true
    }

    fun onAddCoralClick() {
        _shouldNavigateToAddCoral.value = true
    }

    fun onPlaceNavClick() {
        _shouldNavigateToPlace.value = true
    }

    fun onWorkerNavClick() {
        _shouldNavigateToWorker.value = true
    }

    // Reset navigation flags after handling
    fun clearNavigationFlags() {
        _shouldNavigateToLogin.value = false
        _shouldNavigateToAddCoral.value = false
        _shouldNavigateToPlace.value = false
        _shouldNavigateToWorker.value = false
        _selectedCoral.value = null
    }

    fun refreshData() {
        loadCoralData()
    }
}

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val coralList: List<Coral> = emptyList(),
    val userName: String = "",
    val welcomeMessage: String = "",
    val collectionTitle: String = "MY CORAL'S SEEDS COLLECTION (0)",
    val totalCorals: Int = 0,
    val lowStockCount: Int = 0,
    val error: String? = null
)