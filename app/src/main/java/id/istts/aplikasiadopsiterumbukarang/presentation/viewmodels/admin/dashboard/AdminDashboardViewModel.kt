package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val coralRepository: CoralRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableLiveData(AdminDashboardUiState())
    val uiState: LiveData<AdminDashboardUiState> = _uiState

    // Navigation flags
    private val _shouldNavigateToLogin = MutableLiveData<Boolean>()
    val shouldNavigateToLogin: LiveData<Boolean> = _shouldNavigateToLogin

    private val _shouldNavigateToAddCoral = MutableLiveData<Boolean>()
    val shouldNavigateToAddCoral: LiveData<Boolean> = _shouldNavigateToAddCoral

    private val _shouldNavigateToPlace = MutableLiveData<Boolean>()
    val shouldNavigateToPlace: LiveData<Boolean> = _shouldNavigateToPlace

    private val _shouldNavigateToWorker = MutableLiveData<Boolean>()
    val shouldNavigateToWorker: LiveData<Boolean> = _shouldNavigateToWorker

    // Event untuk menampilkan dialog/toast
    private val _showDeleteDialog = MutableLiveData<Coral?>()
    val showDeleteDialog: LiveData<Coral?> = _showDeleteDialog

    private val _showMessage = MutableLiveData<String?>()
    val showMessage: LiveData<String?> = _showMessage

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
        _uiState.value = _uiState.value?.copy(
            userName = userName,
            welcomeMessage = "Hi, $userName"
        )
    }

    fun loadCoralData() {
        val token = sessionManager.fetchAuthToken()

        if (token.isNullOrEmpty()) {
            _uiState.value = _uiState.value?.copy(isLoading = false, error = "Authentication token not found")
            _shouldNavigateToLogin.value = true
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val result = coralRepository.getCoralList(authToken)

                if (result.isSuccess) {
                    val coralList = result.getOrNull() ?: emptyList()
                    val totalCorals = coralList.size
                    val lowStockCount = coralList.count { it.stok_tersedia < 10 }

                    _uiState.postValue(_uiState.value?.copy(
                        isLoading = false,
                        coralList = coralList,
                        collectionTitle = "MY CORAL'S SEEDS COLLECTION ($totalCorals)",
                        totalCorals = totalCorals,
                        lowStockCount = lowStockCount,
                        error = null
                    ))
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Unknown error occurred"

                    if (errorMessage.contains("Invalid or Expired Token", true) || errorMessage.contains("401")) {
                        _uiState.postValue(_uiState.value?.copy(isLoading = false, error = "Session expired. Please login again."))
                        _shouldNavigateToLogin.postValue(true)
                    } else {
                        _uiState.postValue(_uiState.value?.copy(isLoading = false, error = "Failed to load coral data: $errorMessage"))
                    }
                }
            } catch (e: Exception) {
                _uiState.postValue(_uiState.value?.copy(isLoading = false, error = "Network error: ${e.message}"))
            }
        }
    }

    // Fungsi untuk interaksi UI
    fun onCoralItemClick(coral: Coral) {
        // Navigasi ke detail atau tampilkan dialog. Contoh:
        // _shouldNavigateToCoralDetail.value = coral
    }

    fun onCoralEditClick(coral: Coral) {
        // Navigasi ke halaman edit
        // _shouldNavigateToEditCoral.value = coral
    }

    fun onCoralDeleteClick(coral: Coral) {
        _showDeleteDialog.value = coral
    }

    fun confirmDeleteCoral() {
        val coralToDelete = _showDeleteDialog.value
        if (coralToDelete != null) {
            deleteCoral(coralToDelete)
            _showDeleteDialog.value = null
        }
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    private fun deleteCoral(coral: Coral) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _showMessage.value = "Authentication token not found"
            _shouldNavigateToLogin.value = true
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val result = coralRepository.deleteCoral(coral.id_tk, token)
                if (result.isSuccess) {
                    _showMessage.postValue("Coral '${coral.tk_name}' deleted successfully")
                    loadCoralData() // Refresh data
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Failed to delete coral"
                    if (errorMessage.contains("Invalid or Expired Token", true) || errorMessage.contains("401")) {
                        _showMessage.postValue("Session expired. Please login again.")
                        _shouldNavigateToLogin.postValue(true)
                    } else {
                        _showMessage.postValue("Failed to delete coral: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                _showMessage.postValue("Network error: ${e.message}")
            } finally {
                _uiState.postValue(_uiState.value?.copy(isLoading = false))
            }
        }
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

    fun onNavigated() {
        _shouldNavigateToLogin.value = false
        _shouldNavigateToAddCoral.value = false
        _shouldNavigateToPlace.value = false
        _shouldNavigateToWorker.value = false
    }

    fun onMessageShown() {
        _showMessage.value = null
    }

    fun refreshData() {
        loadCoralData()
    }
}