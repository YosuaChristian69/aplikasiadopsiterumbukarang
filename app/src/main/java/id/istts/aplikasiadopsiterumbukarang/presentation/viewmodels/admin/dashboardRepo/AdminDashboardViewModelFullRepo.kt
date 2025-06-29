package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboardRepo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard.AdminDashboardUiState
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.Event
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class AdminDashboardViewModelFullRepo(private val repository: RepostioryCorral, private val coralRepository: CoralRepository, private val sessionManager: SessionManager, val fileUtils: FileUtils?=null, val context: Context?=null):
    ViewModel() {

    private val _uiState = MutableLiveData(AdminDashboardUiState())
    val uiState: LiveData<AdminDashboardUiState> = _uiState

    // Navigation flags
    private val _shouldNavigateToLogin = MutableLiveData<Event<Boolean>>()
    val shouldNavigateToLogin: LiveData<Event<Boolean>> = _shouldNavigateToLogin

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
            _shouldNavigateToLogin.value = Event(true)
        } else {
            loadUserInfo()
            loadCoralDataRepo()
        }
    }

    private fun loadUserInfo() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        _uiState.value = _uiState.value?.copy(
            userName = userName,
            welcomeMessage = "Hi, $userName"
        )
    }
    fun loadCoralDataRepo() {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Authentication token not found"
            )
            _shouldNavigateToLogin.value = Event(true)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val tokenfix = authToken.replace("Bearer ", "", ignoreCase = true)
                val result = repository.getAllTerumbuKarangHybridly(tokenfix,fileUtils,context)

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
    fun loadCoralData() {
        val token = sessionManager.fetchAuthToken()

        if (token.isNullOrEmpty()) {
            _uiState.value = _uiState.value?.copy(isLoading = false, error = "Authentication token not found")
            _shouldNavigateToLogin.value = Event(true)
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
                        _shouldNavigateToLogin.postValue(Event(true))
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
            deleteCorralRepo(coralToDelete)
            _showDeleteDialog.value = null
        }
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    fun deleteCorralRepo(coral: Coral){
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _showMessage.value = "Authentication token not found"
            _shouldNavigateToLogin.value = Event(true)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val result = repository.deleteHybridly(coral.id_tk, token)
                if(result == "remote delete success"){
                    _showMessage.value = "Coral '${coral.tk_name}' deleted successfully"
                }else{
                    _showMessage.value = "Coral '${coral.tk_name}' deleted locally"
                }
                loadCoralDataRepo() // Refresh data after deletion
            } catch (e: Exception) {
                _showMessage.value = "Network error: ${e.message}"
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    private fun deleteCoral(coral: Coral) {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _showMessage.value = "Authentication token not found"
            _shouldNavigateToLogin.value = Event(true)
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val result = coralRepository.deleteCoral(coral.id_tk, token)
                if (result.isSuccess) {
                    _showMessage.postValue("Coral '${coral.tk_name}' deleted successfully")
                    loadCoralDataRepo() // Refresh data
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Failed to delete coral"
                    if (errorMessage.contains("Invalid or Expired Token", true) || errorMessage.contains("401")) {
                        _showMessage.postValue("Session expired. Please login again.")
                        _shouldNavigateToLogin.postValue(Event(true))
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
        _shouldNavigateToLogin.value = Event(true)
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
        _shouldNavigateToLogin.value = Event(false)
        _shouldNavigateToAddCoral.value = false
        _shouldNavigateToPlace.value = false
        _shouldNavigateToWorker.value = false
    }

    fun onMessageShown() {
        _showMessage.value = null
    }

    fun refreshData() {
        loadCoralDataRepo()
    }
}