package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class UpdatedUserData(
    val id_user: Int,
    val full_name: String,
    val email: String,
    val status: String,
    val balance: Double,
    val joined_at: String,
    val user_status: String,
    val img_path: String?
)
