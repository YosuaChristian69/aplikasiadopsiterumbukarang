package id.istts.aplikasiadopsiterumbukarang.domain.models

data class EditCoralRequest(
    val name: String?,
    val jenis: String?,
    val harga: Int?,
    val stok: Int?,
    val description: String?
)