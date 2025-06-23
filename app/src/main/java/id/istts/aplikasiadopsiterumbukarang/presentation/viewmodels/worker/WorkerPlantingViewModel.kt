package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlanting
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetail
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerPlantingUiState
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * ViewModel for managing worker planting operations
 */
class WorkerPlantingViewModel(
    private val repository: WorkerPlantingRepository<Any?>,
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
            loadCoralData()
        }
    }

    private fun loadUserInfo() {
        val userName = sessionManager.fetchUserName() ?: "Worker"
        _uiState.value?.let { currentState ->
            _uiState.value = currentState.copy(
                userName = userName,
                welcomeMessage = "Hi, $userName"
            )
        }
    }

    private fun loadCoralData() {
        loadPendingPlantings()
    }

    fun loadPendingPlantings() {
        _loading.value = true
        _uiState.value?.let { currentState ->
            _uiState.value = currentState.copy(isLoading = true)
        }

        viewModelScope.launch {
            repository.getPendingPlantings()
                .onSuccess { result ->
                    val plantings = result as? List<PendingPlanting>
                    _pendingPlantings.value = plantings
                    _loading.value = false
                    _uiState.value?.let { currentState ->
                        _uiState.value = currentState.copy(isLoading = false)
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _loading.value = false
                    _uiState.value?.let { currentState ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }

    fun loadPlantingDetails(id: Int) {
        _loading.value = true
        _uiState.value?.let { currentState ->
            _uiState.value = currentState.copy(isLoading = true)
        }

        viewModelScope.launch {
            repository.getPlantingDetails(id)
                .onSuccess { result ->
                    val detail = result as? PlantingDetail
                    _plantingDetail.value = detail
                    _loading.value = false
                    _uiState.value?.let { currentState ->
                        _uiState.value = currentState.copy(isLoading = false)
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _loading.value = false
                    _uiState.value?.let { currentState ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }

    fun finishPlanting(id: Int, workerId: Int? = null) {
        _loading.value = true
        _uiState.value?.let { currentState ->
            _uiState.value = currentState.copy(isLoading = true)
        }

        viewModelScope.launch {
            repository.finishPlanting(id, workerId)
                .onSuccess {
                    _loading.value = false
                    _uiState.value?.let { currentState ->
                        _uiState.value = currentState.copy(isLoading = false)
                    }
                    loadPendingPlantings()
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _loading.value = false
                    _uiState.value?.let { currentState ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }

    fun clearError() {
        _error.value = null
        _uiState.value?.let { currentState ->
            _uiState.value = currentState.copy(errorMessage = null)
        }
    }

    fun onNavigatedToLogin() {
        _shouldNavigateToLogin.value = false
    }
}