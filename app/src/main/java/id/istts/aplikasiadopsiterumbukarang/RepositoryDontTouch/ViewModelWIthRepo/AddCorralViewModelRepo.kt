package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepositoryEditCorral
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addCoral.AddCoralEvent
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addCoral.AddCoralUiState
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCase
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCorralViewModelRepo(
    private val repository: RepostioryCorral,
    private val sessionManager: SessionManager,
    private val fileUtils: FileUtils,
    private val addCoralUseCase: AddCoralUseCase,
    private val context: Context?=null
): ViewModel() {
    private val coralRepository = CoralRepositoryImpl()

    // UI State
    private val _uiState = MutableStateFlow(AddCoralUiState())
    val uiState: StateFlow<AddCoralUiState> = _uiState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Selected image
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    // Events
    private val _events = MutableStateFlow<AddCoralEvent?>(null)
    val events: StateFlow<AddCoralEvent?> = _events.asStateFlow()

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        _uiState.value = _uiState.value.copy(
            hasImage = uri != null
        )
    }

    fun setSelectedImageBitmap(bitmap: Bitmap) {
        val uri = fileUtils.saveBitmapToFile(bitmap)
        setSelectedImageUri(uri)
    }

    fun validateInputs(
        name: String,
        type: String,
        description: String,
        total: String,
        harga: String
    ): Boolean {
        val validationState = AddCoralUiState(
            nameError = if (name.trim().isEmpty()) "Coral name is required" else null,
            typeError = if (type.trim().isEmpty()) "Coral species is required" else null,
            descriptionError = if (description.trim().isEmpty()) "Description is required" else null,
            totalError = if (total.trim().isEmpty()) "Total is required" else null,
            hargaError = if (harga.trim().isEmpty()) "Harga is required" else null,
            hasImage = _selectedImageUri.value != null
        )

        _uiState.value = validationState

        val hasErrors = listOf(
            validationState.nameError,
            validationState.typeError,
            validationState.descriptionError,
            validationState.totalError,
            validationState.hargaError
        ).any { it != null }

        if (!validationState.hasImage) {
            _events.value = AddCoralEvent.ShowError("Please select an image")
            return false
        }

        return !hasErrors
    }

    fun saveCoralRepo(
        name: String,
        type: String,
        description: String,
        total: String,
        harga: String
    ) {
        if (_isLoading.value) return

        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            _events.value = AddCoralEvent.ShowError("Session expired. Please login again.")
            return
        }

        val imageUri = _selectedImageUri.value
        if (imageUri == null) {
            _events.value = AddCoralEvent.ShowError("Please select an image")
            return
        }

        _isLoading.value = true

        val params = AddCoralUseCase.AddCoralParams(
            token = token,
            name = name.trim(),
            jenis = type.trim(),
            harga = harga.trim(),
            stok = total.trim(),
            description = description.trim(),
            imageUri = imageUri,
        )

        viewModelScope.launch {
            try {
                val resultRepo = repository.insertHybridly(token = token, name = name, type = type, description = description, total = total.toInt(), harga = harga.toInt(), uri = imageUri, fileUtils = fileUtils, context = context)
                if (resultRepo=="success remotely"){
                    _isLoading.value = false
                    // Sekarang kita hanya mengirim SATU event yang jelas
                    _events.value = AddCoralEvent.SuccessAndNavigate(resultRepo)
                }else{
                    _isLoading.value = false
                    // Sekarang kita hanya mengirim SATU event yang jelas
                    _events.value = AddCoralEvent.SuccessAndNavigate("inserted locally")
                }
//                val result = addCoralUseCase.execute(params)
//
//                result.fold(
//                    onSuccess = { message ->
//                        _isLoading.value = false
//                        // Sekarang kita hanya mengirim SATU event yang jelas
//                        _events.value = AddCoralEvent.SuccessAndNavigate(message)
//                    },
//                    onFailure = { exception ->
//                        _isLoading.value = false
//                        _events.value = AddCoralEvent.ShowError(exception.message ?: "Unknown error")
//                    }
//                )
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("AddCoralViewModel", "Unexpected error", e)
                _events.value = AddCoralEvent.ShowError("Error preparing image file")
            }
        }
    }

    fun saveCoral(
        name: String,
        type: String,
        description: String,
        total: String,
        harga: String
    ) {
        if (_isLoading.value) return

        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            _events.value = AddCoralEvent.ShowError("Session expired. Please login again.")
            return
        }

        val imageUri = _selectedImageUri.value
        if (imageUri == null) {
            _events.value = AddCoralEvent.ShowError("Please select an image")
            return
        }

        _isLoading.value = true

        val params = AddCoralUseCase.AddCoralParams(
            token = token,
            name = name.trim(),
            jenis = type.trim(),
            harga = harga.trim(),
            stok = total.trim(),
            description = description.trim(),
            imageUri = imageUri
        )

        viewModelScope.launch {
            try {
                val result = addCoralUseCase.execute(params)

                result.fold(
                    onSuccess = { message ->
                        _isLoading.value = false
                        // Sekarang kita hanya mengirim SATU event yang jelas
                        _events.value = AddCoralEvent.SuccessAndNavigate(message)
                    },
                    onFailure = { exception ->
                        _isLoading.value = false
                        _events.value = AddCoralEvent.ShowError(exception.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("AddCoralViewModel", "Unexpected error", e)
                _events.value = AddCoralEvent.ShowError("Error preparing image file")
            }
        }
    }

    fun clearEvent() {
        _events.value = null
    }

    fun clearValidationErrors() {
        _uiState.value = AddCoralUiState()
    }
}