package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.TransactionRepository
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

// MODIFIED: Now accepts SessionManager as a dependency
class UserPaymentViewModelFactory(
    private val coralRepository: CoralRepository,
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager // Add this
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserPaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // MODIFIED: Pass the sessionManager to the ViewModel's constructor
            return UserPaymentViewModel(coralRepository, transactionRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}