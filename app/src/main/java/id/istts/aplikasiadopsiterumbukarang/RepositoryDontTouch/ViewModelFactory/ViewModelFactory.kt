package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class ViewModelFactory(private val repository: RepostioryCorral, private val coralRepository: CoralRepository, private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModelFullRepo::class.java)) {
            return AdminDashboardViewModelFullRepo(repository,coralRepository,sessionManager) as T
        }
//        if (modelClass.isAssignableFrom(AdminDashboardViewModelTestRepo::class.java)) {
//            return AdminDashboardViewModelTestRepo(repository,sessionManager) as T
//        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}