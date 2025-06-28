package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.Worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepositoryWorker
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch
import kotlin.collections.contains

class AdminWorkerDashboardViewModelRepo(private val apiService: ApiService?=null,private val repository: RepositoryWorker, private val context: Context?=null): ViewModel() {
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