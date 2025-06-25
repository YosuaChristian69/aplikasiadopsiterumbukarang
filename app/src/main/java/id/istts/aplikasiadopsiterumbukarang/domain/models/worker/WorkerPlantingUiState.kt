package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class WorkerPlantingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "",
    val welcomeMessage: String = "",
    val pendingPlantings: List<PendingPlanting> = emptyList(),
    val selectedPlanting: PlantingDetail? = null,
    val totalPending: Int = 0,
    val filterByAssignmentStatus: String? = null, // New field for filtering
    val assignedPlantings: List<PendingPlanting> = emptyList(), // New field for assigned tasks
    val unassignedPlantings: List<PendingPlanting> = emptyList() // New field for unassigned tasks
)
