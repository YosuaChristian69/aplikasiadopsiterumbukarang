package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.workerdashboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.service.ApiService

class AdminWorkerDashboardViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminWorkerDashboardViewModel::class.java)) {
            return AdminWorkerDashboardViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}