package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

data class AddCoralUiState(
    val nameError: String? = null,
    val typeError: String? = null,
    val descriptionError: String? = null,
    val totalError: String? = null,
    val hargaError: String? = null,
    val hasImage: Boolean = false
)