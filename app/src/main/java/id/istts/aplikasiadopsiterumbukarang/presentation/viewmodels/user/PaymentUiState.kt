package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

// Represents the state of the payment screen
data class PaymentUiState(
    val coralDetails: Coral? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)