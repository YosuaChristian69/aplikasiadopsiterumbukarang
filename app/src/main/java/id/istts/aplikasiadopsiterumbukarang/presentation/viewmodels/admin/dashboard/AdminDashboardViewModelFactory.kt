package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminDashboardViewModelFactory(
    private val coralRepository: CoralRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
            return AdminDashboardViewModel(coralRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}