package id.istts.aplikasiadopsiterumbukarang.domain.models

data class Coral(
    val id_tk: Int,
    val tk_name: String,
    val tk_jenis: String,
    val harga_tk: Int,
    val stok_tersedia: Int,
    val description: String,
    val is_deleted: Boolean,
    val img_path: String?,
    val public_id: String?
)