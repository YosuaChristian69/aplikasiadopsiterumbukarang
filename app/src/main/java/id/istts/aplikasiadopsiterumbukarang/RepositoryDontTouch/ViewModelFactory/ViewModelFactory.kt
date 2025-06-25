package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.EditCorralViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class ViewModelFactory(private val repository: RepostioryCorral, private val coralRepository: CoralRepository, private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModelFullRepo::class.java)) {
            return AdminDashboardViewModelFullRepo(repository,coralRepository,sessionManager) as T
        }
        if (modelClass.isAssignableFrom(EditCorralViewModelRepo::class.java)) {
            return EditCorralViewModelRepo(repository,coralRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//class ViewModelFactoryEdit(private val repository: RepostioryCorral, private val coralRepository: CoralRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(EditCorralViewModelRepo::class.java)) {
//            return EditCorralViewModelRepo(repository,coralRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}