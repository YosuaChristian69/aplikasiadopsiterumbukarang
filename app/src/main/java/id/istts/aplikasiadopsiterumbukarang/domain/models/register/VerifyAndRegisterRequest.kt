package id.istts.aplikasiadopsiterumbukarang.domain.models.register

data class VerifyAndRegisterRequest(
    val email: String,
    val verificationCode: String
)