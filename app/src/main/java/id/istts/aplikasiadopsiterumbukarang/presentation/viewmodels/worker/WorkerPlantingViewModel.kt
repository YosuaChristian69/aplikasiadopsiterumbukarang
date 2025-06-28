package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerPlantingUiState
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.IWorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch
import android.util.Log

class WorkerPlantingViewModel(
    private val repository: IWorkerPlantingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableLiveData(WorkerPlantingUiState())
    val uiState: LiveData<WorkerPlantingUiState> = _uiState

    // Use a SingleLiveEvent pattern for events that should only be observed once.
    private val _viewEvent = MutableLiveData<Event<ViewEvent>>()
    val viewEvent: LiveData<Event<ViewEvent>> = _viewEvent

    init {
        val userStatus = sessionManager.fetchUserStatus()
        val userName = sessionManager.fetchUserName()
        if (!sessionManager.isLoggedIn() || userStatus != "worker") {
            _uiState.value = _uiState.value?.copy(shouldNavigateToLogin = true)
        } else {
            _uiState.value = _uiState.value?.copy(userName = userName ?: "Worker")
            loadPendingPlantings()
        }
    }

    fun loadPendingPlantings() {
        if (_uiState.value?.isLoading == true) return

        _uiState.value = _uiState.value?.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    handleAuthenticationError()
                    return@launch
                }

                repository.getPendingPlantings(token)
                    .onSuccess { response ->
                        _uiState.postValue(
                            _uiState.value?.copy(
                                isLoading = false,
                                pendingPlantings = response.data ?: emptyList()
                            )
                        )
                    }
                    .onFailure { exception -> handleError(exception, "Failed to load pending plantings") }
            } catch (e: Exception) {
                _uiState.postValue(_uiState.value?.copy(isLoading = false, errorMessage = "Unexpected error: ${e.message}"))
            }
        }
    }

    fun loadPlantingDetails(id: Int) {
        _uiState.value = _uiState.value?.copy(isLoading = true, selectedPlanting = null)
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    handleAuthenticationError()
                    return@launch
                }
                repository.getPlantingDetails(token, id)
                    .onSuccess { response ->
                        _uiState.postValue(_uiState.value?.copy(isLoading = false, selectedPlanting = response.data))
                    }
                    .onFailure { exception -> handleError(exception, "Failed to load planting details") }
            } catch (e: Exception) {
                _uiState.postValue(_uiState.value?.copy(isLoading = false, errorMessage = "Unexpected error: ${e.message}"))
            }
        }
    }

    fun finishPlanting(id: Int) {
        _uiState.value = _uiState.value?.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                val workerId = sessionManager.fetchUserId()
                if (token.isNullOrEmpty() || workerId <= 0) {
                    handleAuthenticationError()
                    return@launch
                }

                repository.finishPlanting(id, workerId, token)
                    .onSuccess {
                        _viewEvent.postValue(Event(ViewEvent.ShowToast("Coral planting completed successfully! 🐠")))
                        _viewEvent.postValue(Event(ViewEvent.NavigateBack))
                        loadPendingPlantings() // Refresh the main list
                    }
                    .onFailure { exception -> handleError(exception, "Failed to complete planting") }
            } catch (e: Exception) {
                _uiState.postValue(_uiState.value?.copy(isLoading = false, errorMessage = "Unexpected error: ${e.message}"))
            } finally {
                _uiState.postValue(_uiState.value?.copy(isLoading = false))
            }
        }
    }

    private fun handleError(exception: Throwable, defaultMessage: String) {
        val errorMessage = if (exception.message?.contains("401") == true) {
            _uiState.postValue(_uiState.value?.copy(shouldNavigateToLogin = true))
            "Session expired. Please login again."
        } else {
            exception.message ?: defaultMessage
        }
        _uiState.postValue(_uiState.value?.copy(isLoading = false, errorMessage = errorMessage))
    }

    private fun handleAuthenticationError() {
        _uiState.value = _uiState.value?.copy(
            isLoading = false,
            errorMessage = "Authentication token not found.",
            shouldNavigateToLogin = true
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value?.copy(errorMessage = null)
    }

    fun onNavigatedToLogin() {
        _uiState.value = _uiState.value?.copy(shouldNavigateToLogin = false)
    }

    sealed class ViewEvent {
        data class ShowToast(val message: String) : ViewEvent()
        object NavigateBack : ViewEvent()
    }

    // Helper class for events
    class Event<T>(private val content: T) {
        private var hasBeenHandled = false
        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }
    }
}