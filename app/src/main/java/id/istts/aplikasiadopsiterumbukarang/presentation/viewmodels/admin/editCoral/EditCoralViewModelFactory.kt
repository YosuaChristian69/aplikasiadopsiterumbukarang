package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository

class EditCoralViewModelFactory(
    private val repository: CoralRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditCoralViewModel::class.java)) {
            return EditCoralViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}