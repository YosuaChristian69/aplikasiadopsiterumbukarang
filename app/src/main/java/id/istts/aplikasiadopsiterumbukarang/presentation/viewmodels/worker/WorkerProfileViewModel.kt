package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerProfile
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _workerProfile = MutableLiveData<WorkerProfile>()
    val workerProfile: LiveData<WorkerProfile> = _workerProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isEditMode = MutableLiveData<Boolean>()
    val isEditMode: LiveData<Boolean> = _isEditMode

    init {
        loadWorkerProfile()
    }

    private fun loadWorkerProfile() {
        _isLoading.value = true

        try {
            // Get user data from session
            val userName = sessionManager.fetchUserName() ?: "Unknown User"
            val userEmail = sessionManager.fetchUserEmail() ?: "No Email"
            val userStatus = sessionManager.fetchUserStatus() ?: "Worker"

            // Create profile with session data and placeholder values
            // In a real app, you would fetch this from an API or database
            val profile = WorkerProfile(
                name = userName,
                email = userEmail,
                jobTitle = if (userStatus.equals("worker", ignoreCase = true)) "Diver" else userStatus,
                dateOfBirth = "01/01/1990", // Placeholder - should come from API
                phone = "081234567890", // Placeholder - should come from API
                joinedDate = "01/01/2024", // Placeholder - should come from API
                workerId = "1234-4321-1234", // Placeholder - should come from API
                profilePhotoUrl = null // Placeholder - should come from API
            )

            _workerProfile.value = profile
            _errorMessage.value = null

        } catch (e: Exception) {
            _errorMessage.value = "Failed to load profile data: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshProfile() {
        loadWorkerProfile()
    }

    fun toggleEditMode() {
        _isEditMode.value = _isEditMode.value?.not() ?: true
    }

    fun updateProfile(updatedProfile: WorkerProfile) {
        _isLoading.value = true

        try {
            // In a real app, you would send this data to an API
            // For now, we'll just update the local data and session if needed

            // Update session data if name or email changed
            if (updatedProfile.name != _workerProfile.value?.name ||
                updatedProfile.email != _workerProfile.value?.email) {
                sessionManager.saveUserDetails(
                    updatedProfile.name,
                    updatedProfile.email,
                    updatedProfile.jobTitle
                )
            }

            _workerProfile.value = updatedProfile
            _isEditMode.value = false
            _errorMessage.value = null

        } catch (e: Exception) {
            _errorMessage.value = "Failed to update profile: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getUserToken(): String? {
        return sessionManager.fetchAuthToken()
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}