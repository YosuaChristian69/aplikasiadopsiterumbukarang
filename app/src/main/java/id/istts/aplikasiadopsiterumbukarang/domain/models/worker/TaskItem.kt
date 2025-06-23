package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

// Data Models
data class TaskItem(
    val id_htrans: Int,
    val is_finished: Boolean,
    val img_path: String? = null,
    // Add other fields as needed based on your ht model
)
