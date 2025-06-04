package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import CoralRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        validateUserAccess()
    }

    fun validateUserAccess() {
        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "admin") {
            _navigationEvent.value = NavigationEvent.NavigateToLogin
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
            _navigationEvent.value = NavigationEvent.NavigateToLogin
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
                        _navigationEvent.value = NavigationEvent.NavigateToLogin
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


    fun onCoralItemClick(coral: Coral) {
        _navigationEvent.value = NavigationEvent.ShowCoralDetail(coral)
    }

    fun onLogoutClick() {
        sessionManager.clearSession()
        _navigationEvent.value = NavigationEvent.NavigateToLogin
    }

    fun onAddCoralClick() {
        _navigationEvent.value = NavigationEvent.NavigateToAddCoral
    }

    fun onPlaceNavClick() {
        _navigationEvent.value = NavigationEvent.NavigateToAdminPlace
    }

    fun onWorkerNavClick() {
        _navigationEvent.value = NavigationEvent.NavigateToAdminWorker
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
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
sealed class NavigationEvent {
    object NavigateToLogin : NavigationEvent()
    object NavigateToAddCoral : NavigationEvent()
    object NavigateToAdminPlace : NavigationEvent()
    object NavigateToAdminWorker : NavigationEvent()
    data class ShowCoralDetail(val coral: Coral) : NavigationEvent()
}

class AdminDashboardViewModelFactory(
    private val coralRepository: CoralRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
            return AdminDashboardViewModel(coralRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}