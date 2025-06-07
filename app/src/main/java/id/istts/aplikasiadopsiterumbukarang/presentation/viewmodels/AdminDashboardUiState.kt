package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val coralList: List<Coral> = emptyList(),
    val userName: String = "",
    val welcomeMessage: String = "",
    val collectionTitle: String = "MY CORAL'S SEEDS COLLECTION (0)",
    val totalCorals: Int = 0,
    val lowStockCount: Int = 0,
    val error: String? = null
)