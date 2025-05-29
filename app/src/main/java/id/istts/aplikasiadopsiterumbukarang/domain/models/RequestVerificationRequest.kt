package id.istts.aplikasiadopsiterumbukarang.domain.models

data class RequestVerificationRequest(
    val name: String,
    val email: String,
    val password: String
)