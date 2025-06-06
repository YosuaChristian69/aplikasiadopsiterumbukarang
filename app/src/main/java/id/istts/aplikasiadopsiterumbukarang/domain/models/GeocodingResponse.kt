package id.istts.aplikasiadopsiterumbukarang.domain.models

data class GeocodingResponse(
    val results: List<GeocodingResult>,
    val status: String
)
