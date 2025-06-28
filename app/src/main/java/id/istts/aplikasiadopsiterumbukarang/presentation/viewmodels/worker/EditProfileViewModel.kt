package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileUiState
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import kotlinx.coroutines.launch
import java.io.File

class EditProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableLiveData(EditProfileUiState())
    val uiState: LiveData<EditProfileUiState> = _uiState

    private val _selectedImageUri = MutableLiveData<Uri?>(null)
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun editProfile(token: String, email: String?, name: String?, imageFile: File? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
            try {
                val result = repository.editProfile(token, email, name, imageFile)
                result
                    .onSuccess { response ->
                        _uiState.postValue(_uiState.value?.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = response.msg,
                            photoUpdated = response.photo_updated ?: false,
                            updatedUser = response.user
                        ))
                    }
                    .onFailure { exception ->
                        _uiState.postValue(_uiState.value?.copy(
                            isLoading = false,
                            error = exception.message ?: "Error tidak diketahui"
                        ))
                    }
            } catch (e: Exception) {
                _uiState.postValue(_uiState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Terjadi error yang tidak terduga"
                ))
            }
        }
    }

    fun clearState() {
        _uiState.value = EditProfileUiState()
        _selectedImageUri.value = null
    }
}