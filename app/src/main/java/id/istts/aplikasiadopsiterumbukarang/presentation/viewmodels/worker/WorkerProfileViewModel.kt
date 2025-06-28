package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerProfile
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {
    // Expose individual MutableLiveData for two-way databinding
    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val dateOfBirth = MutableLiveData<String>()
    val phone = MutableLiveData<String>()

    // Original model for read-only data
    private val _workerProfile = MutableLiveData<WorkerProfile>()
    val workerProfile: LiveData<WorkerProfile> = _workerProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadWorkerProfile()
    }

    private fun loadWorkerProfile() {
        _isLoading.value = true
        try {
            val profile = WorkerProfile(
                name = sessionManager.fetchUserName() ?: "",
                email = sessionManager.fetchUserEmail() ?: "",
                jobTitle = if (sessionManager.fetchUserStatus().equals("worker", ignoreCase = true)) "Diver" else (sessionManager.fetchUserStatus() ?: "Worker"),
                profilePhotoUrl = sessionManager.fetchUserImagePath(),
                // Placeholders
                dateOfBirth = "01/01/1990",
                phone = "081234567890",
                joinedDate = "01/01/2024",
                workerId = "1234-4321-1234"
            )

            _workerProfile.value = profile
            // Set values for the editable fields
            name.value = profile.name
            email.value = profile.email
            dateOfBirth.value = profile.dateOfBirth
            phone.value = profile.phone

        } catch (e: Exception) {
            _errorMessage.value = "Failed to load profile data: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshProfile() {
        loadWorkerProfile()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}