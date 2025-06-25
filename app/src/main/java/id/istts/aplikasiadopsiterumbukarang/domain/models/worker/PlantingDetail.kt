package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class PlantingDetail(
    val id_htrans: Int,
    val nama_pembeli: String,
    val email_pembeli: String,
    val lokasi_penanaman: String,
    val koordinat: String,
    val tanggal_pembelian: String,
    val total_harga: Double,
    val jumlah_jenis_coral: Int,
    val ringkasan_coral: String,
    val assignment_status: String,
    val assigned_worker: String?,
    // Additional fields that might be in detail response
    val coral_details: List<CoralDetail>? = null
)
