package id.istts.aplikasiadopsiterumbukarang.RegisterLogic

data class VerifyAndRegisterRequest(
    val email: String,
    val verificationCode: String
)