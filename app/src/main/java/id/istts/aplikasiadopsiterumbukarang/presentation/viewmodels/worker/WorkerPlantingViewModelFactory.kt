package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

// NO CHANGES NEEDED: This class is already correctly structured.
// It accepts the repository and sessionManager and passes them to the ViewModel.
class WorkerPlantingViewModelFactory(
    // CORRECTED: Removed the <Any?> generic type
    private val repository: WorkerPlantingRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerPlantingViewModel::class.java)) {
            return WorkerPlantingViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}