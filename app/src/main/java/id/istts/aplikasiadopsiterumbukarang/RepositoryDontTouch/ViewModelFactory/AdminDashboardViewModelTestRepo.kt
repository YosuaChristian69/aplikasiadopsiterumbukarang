package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard.AdminDashboardUiState
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModelTestRepo(val repository: RepostioryCorral,private val sessionManager: SessionManager): ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    init {
        validateUserAccess()
    }
    fun validateUserAccess() {
        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "admin") {
            _shouldNavigateToLogin.value = true
        } else {
//            loadUserInfo()
            loadCoralData()
        }
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
                val result = repository.getAllTerumbuKarangHybridly(authToken)
                val totalCorals = result.size
                val lowStockCount = result.count { it.stok_tersedia < 10 }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    coralList = result,
                    collectionTitle = "MY CORAL'S SEEDS COLLECTION ($totalCorals)",
                    totalCorals = totalCorals,
                    lowStockCount = lowStockCount,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

}