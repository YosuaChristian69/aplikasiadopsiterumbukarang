package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class WorkerProfile(
    val name: String,
    val email: String,
    val jobTitle: String,
    val dateOfBirth: String,
    val phone: String,
    val joinedDate: String,
    val workerId: String,
    val profilePhotoUrl: String? = null
)
