package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlanting
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetail
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerPlantingUiState
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * ViewModel for managing worker planting operations
 */
class WorkerPlantingViewModel(
    private val repository: WorkerPlantingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI State
    private val _uiState = MutableLiveData(WorkerPlantingUiState())
    val uiState: LiveData<WorkerPlantingUiState> = _uiState

    // Navigation state
    private val _shouldNavigateToLogin = MutableLiveData<Boolean>()
    val shouldNavigateToLogin: LiveData<Boolean> = _shouldNavigateToLogin

    // Data states
    private val _pendingPlantings = MutableLiveData<List<PendingPlanting>?>()
    val pendingPlantings: LiveData<List<PendingPlanting>?> = _pendingPlantings

    private val _plantingDetail = MutableLiveData<PlantingDetail?>()
    val plantingDetail: LiveData<PlantingDetail?> = _plantingDetail

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        validateUserAccess()
    }

    /**
     * Validates user access and redirects to login if necessary
     */
    fun validateUserAccess() {
        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "worker") {
            _shouldNavigateToLogin.value = true
        } else {
            loadUserInfo()
        }
    }

    private fun loadUserInfo() {
        val userName = sessionManager.fetchUserName() ?: "Worker"
        _uiState.value = _uiState.value?.copy(
            userName = userName,
            welcomeMessage = "Hi, $userName"
        )
    }

    fun loadPendingPlantings() {
        _loading.value = true
        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: ""
                val result = repository.getPendingPlantings(token)

                result.onSuccess { response ->
                    _pendingPlantings.value = response.data
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load plantings"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load plantings"
            } finally {
                _loading.value = false
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    fun loadPlantingDetails(id: Int) {
        _loading.value = true
        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: ""
                val result = repository.getPlantingDetails(token, id)

                result.onSuccess { response ->
                    _plantingDetail.value = response.data
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load planting details"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load planting details"
            } finally {
                _loading.value = false
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    fun finishPlanting(id: Int, workerId: Int) {
        _loading.value = true
        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()?: ""
                val result = repository.finishPlanting(id,workerId,token)

                result.onSuccess { response ->
                    // On success, refresh the list of pending plantings
                    loadPendingPlantings()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to finish planting"
                    _loading.value = false
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to finish planting"
                _loading.value = false
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _error.value = null
        _uiState.value = _uiState.value?.copy(errorMessage = null)
    }

    fun onNavigatedToLogin() {
        _shouldNavigateToLogin.value = false
    }
}