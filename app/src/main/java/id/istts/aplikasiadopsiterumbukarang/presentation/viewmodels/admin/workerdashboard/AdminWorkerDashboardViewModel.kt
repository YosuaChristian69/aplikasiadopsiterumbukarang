package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.workerdashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

// UBAH CONSTRUCTOR: Terima ApiService sebagai parameter
class AdminWorkerDashboardViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // LiveData untuk daftar worker
    private val _workers = MutableLiveData<List<Worker>>()
    val workers: LiveData<List<Worker>> = _workers

    // Simpan daftar asli untuk me-reset pencarian
    private var originalWorkersList: List<Worker> = emptyList()

    // LiveData untuk state UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData untuk statistik
    private val _totalWorkersCount = MutableLiveData<Int>()
    val totalWorkersCount: LiveData<Int> = _totalWorkersCount

    private val _activeWorkersCount = MutableLiveData<Int>()
    val activeWorkersCount: LiveData<Int> = _activeWorkersCount

    // --- PERUBAHAN UNTUK DATABINDING ---

    // Two-way binding untuk search query
    val searchQuery = MutableLiveData<String>()

    // LiveData untuk event navigasi
    private val _navigateToAddWorker = MutableLiveData<Boolean?>()
    val navigateToAddWorker: LiveData<Boolean?> = _navigateToAddWorker

    private val _navigateToLogout = MutableLiveData<Boolean?>()
    val navigateToLogout: LiveData<Boolean?> = _navigateToLogout

    // --- AKHIR PERUBAHAN ---

    fun loadWorkers(sessionManager: SessionManager) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "No authentication token"
                    handleData(emptyList())
                    return@launch
                }

                val response = apiService.fetchAllUsers(token)
                if (response.isSuccessful && response.body() != null) {
                    val allUsers = response.body()?.users ?: emptyList()
                    val workersList = allUsers.filter { user ->
                        val status = user.status?.lowercase()
                        val userStatus = user.user_status?.lowercase()
                        status != "admin" && userStatus != "admin" &&
                                status != "user" && userStatus != "user"
                    }
                    originalWorkersList = workersList
                    handleData(workersList)
                    _error.value = null
                    Log.d("AdminWorkerViewModel", "Filtered workers: ${workersList.size}")
                } else {
                    val errorMessage = "Failed to fetch workers: ${response.code()} - ${response.message()}"
                    _error.value = errorMessage
                    handleData(emptyList())
                    Log.e("AdminWorkerViewModel", errorMessage)
                }
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
        _totalWorkersCount.value = workers.size
        _activeWorkersCount.value = workers.count { worker ->
            worker.user_status?.lowercase() in listOf("active", "aktif") ||
                    worker.status?.lowercase() in listOf("active", "aktif")
        }
    }

    // Method untuk search/filter workers
    // Method untuk search/filter workers
    fun searchWorkers(query: String) {
        val filteredList = if (query.isBlank()) {
            originalWorkersList
        } else {
            originalWorkersList.filter { worker ->
                worker.full_name.contains(query, ignoreCase = true) ||
                        worker.email.contains(query, ignoreCase = true) ||
                        worker.id_user.contains(query, ignoreCase = true)
            }
        }

        handleData(filteredList)
    }
    fun onAddWorkerClicked() {
        _navigateToAddWorker.value = true
    }

    fun onLogoutClicked() {
        _navigateToLogout.value = true
    }

    fun doneNavigatingToAddWorker() {
        _navigateToAddWorker.value = null
    }

    fun doneNavigatingToLogout() {
        _navigateToLogout.value = null
    }
}