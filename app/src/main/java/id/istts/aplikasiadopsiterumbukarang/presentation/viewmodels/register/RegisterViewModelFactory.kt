package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.domain.repository.RegisterRepository

class RegisterViewModelFactory(
    private val repository: RegisterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}