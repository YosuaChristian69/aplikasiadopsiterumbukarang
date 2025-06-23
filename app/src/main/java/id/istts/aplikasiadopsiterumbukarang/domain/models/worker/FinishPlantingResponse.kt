package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class FinishPlantingResponse(
    val msg: String,
    val id_htrans: Int? = null,
    val completed_at: String? = null,
    val error: String? = null
)