package id.istts.aplikasiadopsiterumbukarang.domain.models

data class Coral(
    val id_tk: Int,           // Sesuai dengan database field
    val tk_name: String,
    val tk_jenis: String,
    val harga_tk: Int,        // Sesuai dengan database field
    val stok_tersedia: Int,   // Sesuai dengan database field
    val is_deleted: Int,      // Sesuai dengan database field
    val img_path: String?,    // Sesuai dengan database field (nullable)
    val public_id: String?    // Sesuai dengan database field (nullable)
)