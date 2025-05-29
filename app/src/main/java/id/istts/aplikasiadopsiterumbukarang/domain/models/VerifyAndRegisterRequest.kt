package id.istts.aplikasiadopsiterumbukarang.domain.models

data class VerifyAndRegisterRequest(
    val email: String,
    val verificationCode: String
)