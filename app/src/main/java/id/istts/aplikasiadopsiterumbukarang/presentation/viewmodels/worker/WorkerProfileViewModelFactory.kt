package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
// Add this import if you have an ApiService
// import id.istts.aplikasiadopsiterumbukarang.data.remote.ApiService

class WorkerProfileViewModelFactory(
    private val repository: UserRepository,
    private val sessionManager: SessionManager,
    // Add apiService parameter if needed
    // private val apiService: ApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WorkerProfileViewModel::class.java) -> {
                // Pass the required parameters to WorkerProfileViewModel
                WorkerProfileViewModel(sessionManager) as T
                // If you need both: WorkerProfileViewModel(repository, sessionManager) as T
                // If apiService is also needed: WorkerProfileViewModel(repository, sessionManager, apiService) as T
            }
            modelClass.isAssignableFrom(EditProfileViewModel::class.java) -> {
                // If you also want to support EditProfileViewModel
                EditProfileViewModel(repository) as T
                // Adjust parameters based on EditProfileViewModel constructor
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}