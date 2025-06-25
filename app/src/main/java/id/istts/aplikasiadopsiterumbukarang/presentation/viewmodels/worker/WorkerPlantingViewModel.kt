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

    // Success state for operations
    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    // New fields for enhanced functionality
    private val _totalPending = MutableLiveData<Int>()
    val totalPending: LiveData<Int> = _totalPending

    private val _assignedPlantings = MutableLiveData<List<PendingPlanting>>()
    val assignedPlantings: LiveData<List<PendingPlanting>> = _assignedPlantings

    private val _unassignedPlantings = MutableLiveData<List<PendingPlanting>>()
    val unassignedPlantings: LiveData<List<PendingPlanting>> = _unassignedPlantings

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
        _error.value = null
        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token not found"
                    _shouldNavigateToLogin.value = true
                    return@launch
                }

                val result = repository.getPendingPlantings(token)

                result.onSuccess { response ->
                    _pendingPlantings.value = response.data
                    _totalPending.value = response.total_pending ?: response.data.size

                    // Separate assigned and unassigned plantings
                    val assigned = response.data.filter { it.assignment_status != "unassigned" }
                    val unassigned = response.data.filter { it.assignment_status == "unassigned" }

                    _assignedPlantings.value = assigned
                    _unassignedPlantings.value = unassigned

                    _uiState.value = _uiState.value?.copy(
                        errorMessage = null,
                        pendingPlantings = response.data,
                        totalPending = response.total_pending ?: response.data.size,
                        assignedPlantings = assigned,
                        unassignedPlantings = unassigned
                    )
                }.onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("401") == true -> {
                            _shouldNavigateToLogin.value = true
                            "Session expired. Please login again."
                        }
                        exception.message?.contains("403") == true -> "Access denied. Worker permission required."
                        exception.message?.contains("500") == true -> "Server error. Please try again later."
                        exception.message?.contains("network") == true -> "Network error. Check your connection."
                        else -> exception.message ?: "Failed to load pending plantings"
                    }
                    _error.value = errorMessage
                    _uiState.value = _uiState.value?.copy(errorMessage = errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message}"
                _error.value = errorMessage
                _uiState.value = _uiState.value?.copy(errorMessage = errorMessage)
            } finally {
                _loading.value = false
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    fun loadPlantingDetails(id: Int) {
        _loading.value = true
        _error.value = null
        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token not found"
                    _shouldNavigateToLogin.value = true
                    return@launch
                }

                val result = repository.getPlantingDetails(token, id)

                result.onSuccess { response ->
                    _plantingDetail.value = response.data
                    _uiState.value = _uiState.value?.copy(
                        errorMessage = null,
                        selectedPlanting = response.data
                    )
                }.onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("401") == true -> {
                            _shouldNavigateToLogin.value = true
                            "Session expired. Please login again."
                        }
                        exception.message?.contains("404") == true -> "Planting details not found."
                        exception.message?.contains("500") == true -> "Server error. Please try again later."
                        else -> exception.message ?: "Failed to load planting details"
                    }
                    _error.value = errorMessage
                    _uiState.value = _uiState.value?.copy(errorMessage = errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message}"
                _error.value = errorMessage
                _uiState.value = _uiState.value?.copy(errorMessage = errorMessage)
            } finally {
                _loading.value = false
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    fun finishPlanting(id: Int, workerId: Int? = null) {
        _loading.value = true
        _error.value = null
        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token not found"
                    _shouldNavigateToLogin.value = true
                    return@launch
                }

                // Use provided workerId or fetch from session
                val actualWorkerId = workerId ?: sessionManager.fetchUserId()
                if (actualWorkerId <= 0) {
                    _error.value = "Worker ID not found"
                    return@launch
                }

                val result = repository.finishPlanting(id, actualWorkerId, token)

                result.onSuccess { response ->
                    _operationSuccess.value = "Planting finished successfully!"
                    // Refresh the list of pending plantings
                    loadPendingPlantings()
                }.onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("401") == true -> {
                            _shouldNavigateToLogin.value = true
                            "Session expired. Please login again."
                        }
                        exception.message?.contains("404") == true -> "Planting record not found."
                        exception.message?.contains("500") == true -> "Server error. Please try again later."
                        else -> exception.message ?: "Failed to finish planting"
                    }
                    _error.value = errorMessage
                    _uiState.value = _uiState.value?.copy(errorMessage = errorMessage)
                    _loading.value = false
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                }
            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message}"
                _error.value = errorMessage
                _uiState.value = _uiState.value?.copy(errorMessage = errorMessage)
                _loading.value = false
                _uiState.value = _uiState.value?.copy(isLoading = false)
            }
        }
    }

    // New filtering methods
    fun filterByAssignmentStatus(status: String?) {
        _uiState.value = _uiState.value?.copy(filterByAssignmentStatus = status)
    }

    fun getFilteredPlantings(): List<PendingPlanting> {
        val currentState = _uiState.value
        return when (currentState?.filterByAssignmentStatus) {
            "assigned" -> currentState.assignedPlantings
            "unassigned" -> currentState.unassignedPlantings
            else -> currentState?.pendingPlantings ?: emptyList()
        }
    }

    // Check if current user is assigned to a specific planting
    fun isCurrentUserAssigned(planting: PendingPlanting): Boolean {
        val currentUserName = sessionManager.fetchUserName()
        return planting.assigned_worker == currentUserName
    }

    // Get plantings assigned to current user
    fun getMyAssignedPlantings(): List<PendingPlanting> {
        val currentUserName = sessionManager.fetchUserName()
        return _assignedPlantings.value?.filter { it.assigned_worker == currentUserName } ?: emptyList()
    }

    fun clearError() {
        _error.value = null
        _uiState.value = _uiState.value?.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _operationSuccess.value = null
    }

    fun onNavigatedToLogin() {
        _shouldNavigateToLogin.value = false
    }

    fun refreshData() {
        loadPendingPlantings()
    }
}
