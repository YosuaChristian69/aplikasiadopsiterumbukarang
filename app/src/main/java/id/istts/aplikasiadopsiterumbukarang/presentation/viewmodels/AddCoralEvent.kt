package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

sealed class AddCoralEvent {
    data class ShowError(val message: String) : AddCoralEvent()
    data class ShowSuccess(val message: String) : AddCoralEvent()
    object NavigateBack : AddCoralEvent()
}