package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

// Data Models
data class PendingPlantingResponse(
    val msg: String,
    val total_pending: Int? = null,
    val data: List<PendingPlanting>
)
