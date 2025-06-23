package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)