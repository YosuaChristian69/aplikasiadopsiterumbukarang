package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class WorkerPlantingUiState(
    val userName: String = "",
    val welcomeMessage: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
