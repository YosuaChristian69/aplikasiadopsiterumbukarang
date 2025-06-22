package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

// di file AddCoralEvent.kt
sealed class AddCoralEvent {
    data class ShowError(val message: String) : AddCoralEvent()
    // Hapus ShowSuccess dan NavigateBack yang lama, ganti dengan ini:
    data class SuccessAndNavigate(val successMessage: String) : AddCoralEvent()
}