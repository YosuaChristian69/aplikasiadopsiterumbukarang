package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class PendingPlanting(
    val id_htrans: Int,
    val nama_pembeli: String,
    val email_pembeli: String,
    val lokasi_penanaman: String,
    val koordinat: String,
    val tanggal_pembelian: String,
    val total_harga: Double,
    val jumlah_jenis_coral: Int,
    val ringkasan_coral: String,
    val assignment_status: String, // New field
    val assigned_worker: String?   // New field - nullable since it can be null for unassigned
)
