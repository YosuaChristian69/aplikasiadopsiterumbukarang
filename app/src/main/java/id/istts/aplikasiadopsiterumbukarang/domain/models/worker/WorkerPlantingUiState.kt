package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class WorkerPlantingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "",
    val welcomeMessage: String = "",
    val pendingPlantings: List<PendingPlanting> = emptyList(),
    val selectedPlanting: PlantingDetail? = null,
    val totalPending: Int = 0,
    val filterByAssignmentStatus: String? = null,
    val assignedPlantings: List<PendingPlanting> = emptyList(),
    val unassignedPlantings: List<PendingPlanting> = emptyList(),
    // ADDED: This property is required by WorkerDashboardFragment
    val shouldNavigateToLogin: Boolean = false
){
    val coralImageUrl: String?
        get() = selectedPlanting?.detail_coral?.firstOrNull()?.ImgUrl
}