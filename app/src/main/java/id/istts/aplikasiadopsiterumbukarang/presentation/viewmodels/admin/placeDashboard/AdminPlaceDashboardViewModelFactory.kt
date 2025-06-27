package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.placeDashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.LokasiRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminPlaceDashboardViewModelFactory(
    private val lokasiRepository: LokasiRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminPlaceDashboardViewModel::class.java)) {
            return AdminPlaceDashboardViewModel(lokasiRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
