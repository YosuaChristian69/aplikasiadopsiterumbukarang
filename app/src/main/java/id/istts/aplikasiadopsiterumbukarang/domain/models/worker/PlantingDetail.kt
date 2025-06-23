package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class PlantingDetail(
    val id_htrans: Int,
    val status_penanaman: String,
    val tanggal_pembelian: String,
    val total_harga: Double,
    val pembeli: Buyer,
    val lokasi_penanaman: PlantingLocation,
    val detail_coral: List<CoralDetail>
)
