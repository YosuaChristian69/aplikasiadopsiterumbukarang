package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import android.util.Log

class AdminWorkerDashboardViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    private val _workers = MutableLiveData<List<Worker>>()
    val workers: LiveData<List<Worker>> = _workers

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
                    _workers.value = emptyList()
                    updateStats(emptyList())
                    return@launch
                }

                val response = apiService.fetchAllUsers(token)
                if (response.isSuccessful && response.body() != null) {
                    val allUsers = response.body()?.users ?: emptyList()

                    // Filter hanya worker (bukan admin)
                    val workersList = allUsers.filter { user ->
                        user.user_status?.lowercase() != "admin" &&
                                user.status?.lowercase() != "admin"
                    }

                    _workers.value = workersList
                    updateStats(workersList)
                    _error.value = null

                    // Log untuk debugging
                    Log.d("AdminWorkerViewModel", "Total users: ${allUsers.size}")
                    Log.d("AdminWorkerViewModel", "Filtered workers: ${workersList.size}")

                } else {
                    val errorMessage = "Failed to fetch workers: ${response.code()} - ${response.message()}"
                    _error.value = errorMessage
                    _workers.value = emptyList()
                    updateStats(emptyList())
                    Log.e("AdminWorkerViewModel", errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Exception: ${e.message}"
                _error.value = errorMessage
                _workers.value = emptyList()
                updateStats(emptyList())
                Log.e("AdminWorkerViewModel", errorMessage, e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateStats(workers: List<Worker>) {
        val totalCount = workers.size

        // Periksa beberapa kemungkinan field untuk status aktif
        val activeCount = workers.count { worker ->
            when {
                // Cek user_status terlebih dahulu (sesuai dengan adapter)
                worker.user_status?.lowercase() in listOf("active", "aktif") -> true
                // Fallback ke status jika user_status null
                worker.status?.lowercase() in listOf("active", "aktif") -> true
                else -> false
            }
        }

        _totalWorkersCount.value = totalCount
        _activeWorkersCount.value = activeCount

        // Log untuk debugging
        Log.d("AdminWorkerViewModel", "Total workers: $totalCount")
        Log.d("AdminWorkerViewModel", "Active workers: $activeCount")

        // Log detail setiap worker untuk debugging
        workers.forEachIndexed { index, worker ->
            Log.d("AdminWorkerViewModel",
                "Worker $index: ${worker.full_name}, " +
                        "user_status: ${worker.user_status}, " +
                        "status: ${worker.status}"
            )
        }
    }

    fun refreshWorkers(sessionManager: SessionManager) {
        loadWorkers(sessionManager)
    }

    // Method untuk search/filter workers
    fun searchWorkers(query: String, sessionManager: SessionManager) {
        val currentWorkers = _workers.value ?: emptyList()

        if (query.isEmpty()) {
            // Jika search kosong, reload semua workers
            loadWorkers(sessionManager)
        } else {
            // Filter workers berdasarkan nama atau email
            val filteredWorkers = currentWorkers.filter { worker ->
                worker.full_name?.contains(query, ignoreCase = true) == true ||
                        worker.email?.contains(query, ignoreCase = true) == true ||
                        worker.id_user?.toString()?.contains(query, ignoreCase = true) == true
            }

            _workers.value = filteredWorkers
            // Update stats dengan filtered data
            updateStats(filteredWorkers)
        }
    }
}