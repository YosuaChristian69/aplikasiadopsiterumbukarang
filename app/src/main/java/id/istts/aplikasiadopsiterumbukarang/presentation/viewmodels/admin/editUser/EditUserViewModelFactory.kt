package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editUser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.istts.aplikasiadopsiterumbukarang.service.ApiService

class EditUserViewModelFactory(
    private val apiService: ApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Cek apakah ViewModel yang diminta adalah EditUserViewModel
        if (modelClass.isAssignableFrom(EditUserViewModel::class.java)) {
            // Jika ya, buat instance-nya dengan memberikan apiService
            return EditUserViewModel(apiService) as T
        }
        // Jika bukan, lemparkan error
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}