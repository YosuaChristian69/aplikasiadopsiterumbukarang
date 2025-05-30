package id.istts.aplikasiadopsiterumbukarang.domain.models

data class GetCoralResponse(
    val success: Boolean,
    val message: String,
    val data: List<Coral>
)