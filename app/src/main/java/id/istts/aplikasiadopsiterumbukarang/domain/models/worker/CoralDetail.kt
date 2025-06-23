package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class CoralDetail(
    val id_dtrans: Int,
    val nama_coral: String,
    val jenis: String,
    val harga_satuan: Double,
    val jumlah: Int,
    val subtotal: Double,
    val deskripsi: String?
)
