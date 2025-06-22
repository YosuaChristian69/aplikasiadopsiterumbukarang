// It's best to put this in its own file: UserAddCoralViewModelFactory.kt
package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.UserAddCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class UserAddCoralViewModelFactory(
    private val coralRepository: CoralRepositoryImpl,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // This check ensures we only create the ViewModel this factory is designed for
        if (modelClass.isAssignableFrom(UserAddCoralViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Here, we manually call the constructor with the dependencies we have
            return UserAddCoralViewModel(coralRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}