package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepositoryWorker
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.AddCorralViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.EditCorralViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.register.AdminWorkerDashboardViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.Worker.AdminWorkerEditUserViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboardRepo.AdminDashboardViewModelFullRepo
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCase
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class ViewModelFactory(private val repository: RepostioryCorral, private val coralRepository: CoralRepository, private val sessionManager: SessionManager,private val fileUtils: FileUtils?=null,private val context: Context?=null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminDashboardViewModelFullRepo::class.java)) {
            return AdminDashboardViewModelFullRepo(
                repository,
                coralRepository,
                sessionManager,
                fileUtils,
                context
            ) as T
        }
        if (modelClass.isAssignableFrom(EditCorralViewModelRepo::class.java)) {
            return EditCorralViewModelRepo(repository,coralRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ViewModelFactoryAdd(private val repository: RepostioryCorral,private val sessionManager: SessionManager, private val fileUtils: FileUtils, private val addCoralUseCase: AddCoralUseCase,val context: Context?=null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCorralViewModelRepo::class.java)) {
            return AddCorralViewModelRepo(repository,sessionManager,fileUtils,addCoralUseCase,context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ViewModelFactoryForAdminWorker(private val repository: RepositoryWorker, private val sessionManager: SessionManager,private val context: Context?=null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminWorkerDashboardViewModelRepo::class.java)) {
            return AdminWorkerDashboardViewModelRepo(repository = repository, context = context) as T
        }
        if (modelClass.isAssignableFrom(AdminWorkerEditUserViewModelRepo::class.java)) {
            return AdminWorkerEditUserViewModelRepo(repository = repository, context = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}