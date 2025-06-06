package id.istts.aplikasiadopsiterumbukarang.domain.models

data class GeocodingResult(
    val formatted_address: String,
    val geometry: Geometry,
    val address_components: List<AddressComponent>
)
