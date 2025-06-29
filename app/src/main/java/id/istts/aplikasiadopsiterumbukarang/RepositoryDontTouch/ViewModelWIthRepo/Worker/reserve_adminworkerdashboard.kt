package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.Worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepositoryWorker
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard.AdminDashboardUiState
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.contains

class AdminWorkerDashboardViewModelRepo2(private val apiService: ApiService?=null,private val repository: RepositoryWorker, private val context: Context?=null): ViewModel() {
    // HAPUS BARIS INI: private val apiService: ApiService = RetrofitClient.instance

    private val _workers = MutableLiveData<List<Worker>>()
    val workers: LiveData<List<Worker>> = _workers

    // Simpan daftar asli untuk me-reset pencarian
    private var originalWorkersList: List<Worker> = emptyList()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _totalWorkersCount = MutableLiveData<Int>()
    val totalWorkersCount: LiveData<Int> = _totalWorkersCount

    private val _activeWorkersCount = MutableLiveData<Int>()
    val activeWorkersCount: LiveData<Int> = _activeWorkersCount

    fun loadWorkers(sessionManager: SessionManager) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "No authentication token"
                    handleData(emptyList()) // Gunakan satu fungsi untuk update
                    return@launch
                }
                val result = repository.getAllWorkerHybridly(token)
                originalWorkersList = result
                handleData(originalWorkersList)

//                val response = apiService.fetchAllUsers(token)
//                if (response.isSuccessful && response.body() != null) {
//                    val allUsers = response.body()?.users ?: emptyList()
//
//                    // Filter hanya worker (bukan admin atau user biasa)
//                    val workersList = allUsers.filter { user ->
//                        val status = user.status?.lowercase()
//                        val userStatus = user.user_status?.lowercase()
//                        status != "admin" && userStatus != "admin" &&
//                                status != "user" && userStatus != "user"
//                    }
//
//                    // Simpan daftar asli dan perbarui UI
//                    originalWorkersList = workersList
//                    handleData(workersList)
//                    _error.value = null
//                    Log.d("AdminWorkerViewModel", "Filtered workers: ${workersList.size}")
//
//                } else {
//                    val errorMessage = "Failed to fetch workers: ${response.code()} - ${response.message()}"
//                    _error.value = errorMessage
//                    handleData(emptyList())
//                    Log.e("AdminWorkerViewModel", errorMessage)
//                }
            } catch (e: Exception) {
                val errorMessage = "Exception: ${e.message}"
                _error.value = errorMessage
                handleData(emptyList())
                Log.e("AdminWorkerViewModel", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleData(workers: List<Worker>) {
        _workers.value = workers
        updateStats(workers)
    }

    private fun updateStats(workers: List<Worker>) {
        val totalCount = workers.size
        val activeCount = workers.count { worker ->
            worker.user_status?.lowercase() in listOf("active", "aktif") ||
                    worker.status?.lowercase() in listOf("active", "aktif")
        }

        _totalWorkersCount.value = totalCount
        _activeWorkersCount.value = activeCount
    }

    // Method untuk search/filter workers
    fun searchWorkers(query: String) {
        if (query.isBlank()) {
            // Jika search kosong, kembalikan ke daftar asli
            handleData(originalWorkersList)
        } else {
            // Filter dari daftar asli berdasarkan nama, email, atau ID
            val filteredWorkers = originalWorkersList.filter { worker ->
                worker.full_name.contains(query, ignoreCase = true) ||
                        worker.email.contains(query, ignoreCase = true) ||
                        worker.id_user.contains(query, ignoreCase = true)
            }
            handleData(filteredWorkers)
        }
    }
}







class AdminDashboardViewModelFullRepo2(private val repository: RepostioryCorral,private val coralRepository: CoralRepository, private val sessionManager: SessionManager, val fileUtils: FileUtils?=null,val context: Context?=null):
    ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    // Navigation flags
    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    private val _shouldNavigateToAddCoral = MutableStateFlow(false)
    val shouldNavigateToAddCoral: StateFlow<Boolean> = _shouldNavigateToAddCoral.asStateFlow()

    private val _shouldNavigateToPlace = MutableStateFlow(false)
    val shouldNavigateToPlace: StateFlow<Boolean> = _shouldNavigateToPlace.asStateFlow()

    private val _shouldNavigateToWorker = MutableStateFlow(false)
    val shouldNavigateToWorker: StateFlow<Boolean> = _shouldNavigateToWorker.asStateFlow()

    private val _shouldNavigateToEditCoral = MutableStateFlow<Coral?>(null)
    val shouldNavigateToEditCoral: StateFlow<Coral?> = _shouldNavigateToEditCoral.asStateFlow()

    private val _shouldNavigateToCoralDetail = MutableStateFlow<Coral?>(null)
    val shouldNavigateToCoralDetail: StateFlow<Coral?> = _shouldNavigateToCoralDetail.asStateFlow()

    private val _selectedCoral = MutableStateFlow<Coral?>(null)
    val selectedCoral: StateFlow<Coral?> = _selectedCoral.asStateFlow()

    // Delete confirmation dialog
    private val _showDeleteDialog = MutableStateFlow<Coral?>(null)
    val showDeleteDialog: StateFlow<Coral?> = _showDeleteDialog.asStateFlow()

    // Success/Error messages
    private val _showMessage = MutableStateFlow<String?>(null)
    val showMessage: StateFlow<String?> = _showMessage.asStateFlow()

    init {
        validateUserAccess()
    }

    fun validateUserAccess() {
        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "admin") {
            _shouldNavigateToLogin.value = true
        } else {
            loadUserInfo()
//            loadCoralData()
            loadCoralDataRepo()
        }
    }

    private fun loadUserInfo() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        _uiState.value = _uiState.value.copy(
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
            _shouldNavigateToLogin.value = true
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

    // Coral interaction functions
    fun onCoralItemClick(coral: Coral) {
        _selectedCoral.value = coral
        _shouldNavigateToCoralDetail.value = coral
    }

    fun onCoralEditClick(coral: Coral) {
        _shouldNavigateToEditCoral.value = coral
    }

    fun onCoralDeleteClick(coral: Coral, position: Int) {
        _showDeleteDialog.value = coral
    }

    fun confirmDeleteCoral() {
        val coralToDelete = _showDeleteDialog.value
        if (coralToDelete != null) {
//            deleteCoral(coralToDelete)
            deleteCorralRepo(coralToDelete)
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

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val result = coralRepository.deleteCoral(coral.id_tk, token)

                if (result.isSuccess) {
                    val successMessage = result.getOrNull() ?: "Coral deleted successfully"
                    _showMessage.value = "Coral '${coral.tk_name}' deleted successfully"
                    loadCoralDataRepo() // Refresh data after deletion
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Failed to delete coral"

                    if (errorMessage.contains("Invalid or Expired Token") ||
                        errorMessage.contains("401") ||
                        errorMessage.contains("expired token", ignoreCase = true)) {
                        _showMessage.value = "Session expired. Please login again."
                        _shouldNavigateToLogin.value = true
                    } else {
                        _showMessage.value = "Failed to delete coral: $errorMessage"
                    }
                }
            } catch (e: Exception) {
                _showMessage.value = "Network error: ${e.message}"
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteCorralRepo(coral: Coral){
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _showMessage.value = "Authentication token not found"
            _shouldNavigateToLogin.value = true
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

    fun clearNavigationFlags() {
        _shouldNavigateToLogin.value = false
        _shouldNavigateToAddCoral.value = false
        _shouldNavigateToPlace.value = false
        _shouldNavigateToWorker.value = false
        _shouldNavigateToEditCoral.value = null
        _shouldNavigateToCoralDetail.value = null
        _selectedCoral.value = null
    }

    fun clearMessage() {
        _showMessage.value = null
    }

    fun refreshData() {
        loadCoralDataRepo()
    }
}