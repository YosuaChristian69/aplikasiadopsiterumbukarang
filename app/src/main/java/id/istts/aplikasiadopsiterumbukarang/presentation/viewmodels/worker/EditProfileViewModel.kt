package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileUiState
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class EditProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }

    fun editProfile(token: String, email: String?, name: String?, imageFile: File? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.editProfile(token, email, name, imageFile)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = response.msg,
                        photoUpdated = response.photo_updated ?: false,
                        updatedUser = response.user
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun clearState() {
        _uiState.value = EditProfileUiState()
        _selectedImageUri.value = null
    }
}