package id.istts.aplikasiadopsiterumbukarang.domain.models

data class AddWorkerRequest(
    val name: String,
    val email: String,
    val password: String
)