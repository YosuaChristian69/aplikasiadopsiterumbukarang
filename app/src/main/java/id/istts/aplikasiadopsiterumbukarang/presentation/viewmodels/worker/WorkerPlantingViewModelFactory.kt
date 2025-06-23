package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerPlantingViewModelFactory(
    private val repository: WorkerPlantingRepository<Any?>,
    private val sessionManager: SessionManager // Add this parameter
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerPlantingViewModel::class.java)) {
            return WorkerPlantingViewModel(repository, sessionManager) as T // Pass both parameters
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
