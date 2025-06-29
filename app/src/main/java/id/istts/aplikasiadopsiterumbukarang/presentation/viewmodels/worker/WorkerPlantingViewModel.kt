package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerPlantingUiState
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.IWorkerPlantingRepository
import java.io.File

class WorkerPlantingViewModel(
    private val repository: IWorkerPlantingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
//    val selectedPlanting: Planting? = null,
    private val _uiState = MutableStateFlow(WorkerPlantingUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ViewEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
//    val coralImageUrl: String? = selectedPlanting?.detail_coral?.firstOrNull()?.ImgUrl

    init {
        val userStatus = sessionManager.fetchUserStatus()
        val userName = sessionManager.fetchUserName()
        if (!sessionManager.isLoggedIn() || userStatus != "worker") {
            _uiState.update { it.copy(shouldNavigateToLogin = true) }
        } else {
            _uiState.update { it.copy(userName = userName ?: "Worker") }
            loadPendingPlantings()
        }
    }

    fun loadPendingPlantings() {
        // Only fetch if not already loading
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                Log.d("ViewModelToken", "Token being sent: $token") // This log is still useful

                if (token.isNullOrEmpty()) {
                    handleAuthenticationError()
                    return@launch
                }

                repository.getPendingPlantings(token)
                    .onSuccess { response ->
                        Log.d("WorkerViewModel", "Fetched data: ${response.data}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pendingPlantings = response.data ?: emptyList()
                            )
                        }
                    }
                    .onFailure { exception -> handleError(exception, "Failed to load pending plantings") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Unexpected error: ${e.message}") }
            }
        }
    }

    fun loadPlantingDetails(id: Int) {
        _uiState.update { it.copy(isLoading = true, selectedPlanting = null) }
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    handleAuthenticationError()
                    return@launch
                }
                repository.getPlantingDetails(token, id)
                    .onSuccess { response ->
                        _uiState.update { it.copy(isLoading = false, selectedPlanting = response.data) }
                    }
                    .onFailure { exception -> handleError(exception, "Failed to load planting details") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Unexpected error: ${e.message}") }
            }
        }
    }

    fun finishPlanting(id: Int , img_url: File) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
//                Log.d("tokenworker", sessionManager.fetchAuthToken().toString())
                val workerId = sessionManager.fetchUserId()
                Log.d("tokenworker", sessionManager.fetchUserId().toString())
                Log.d("file", img_url.toString())
                if (token.isNullOrEmpty() || workerId <= 0) {
                    handleAuthenticationError()
                    return@launch
                }

                repository.finishPlanting(id = id, workerId= workerId, token = token , img_url = img_url)
                    .onSuccess {
                        _eventFlow.emit(ViewEvent.ShowToast("Coral planting completed successfully! 🐠"))
                        _eventFlow.emit(ViewEvent.NavigateBack) // Navigate back after completion
                        loadPendingPlantings() // Refresh the main list
                    }
                    .onFailure { exception -> handleError(exception, "Failed to complete planting") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Unexpected error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleError(exception: Throwable, defaultMessage: String) {
        val errorMessage = if (exception.message?.contains("401") == true) {
            _uiState.update { it.copy(shouldNavigateToLogin = true) }
            "Session expired. Please login again."
        } else {
            exception.message ?: defaultMessage
        }
        _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
    }

    private fun handleAuthenticationError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "Authentication token not found.",
                shouldNavigateToLogin = true
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onNavigatedToLogin() {
        _uiState.update { it.copy(shouldNavigateToLogin = false) }
    }

    sealed class ViewEvent {
        data class ShowToast(val message: String) : ViewEvent()
        object NavigateBack : ViewEvent()
    }
}