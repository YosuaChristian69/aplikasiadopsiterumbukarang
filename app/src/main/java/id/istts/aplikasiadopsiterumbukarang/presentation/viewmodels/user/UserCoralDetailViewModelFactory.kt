package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository // Ensure correct import

class UserCoralDetailViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserCoralDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserCoralDetailViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}