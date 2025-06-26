package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

import com.google.gson.annotations.SerializedName

data class PendingPlanting(
    @SerializedName("id_htrans")
    val id_htrans: Int,

    @SerializedName("nama_pembeli")
    val nama_pembeli: String,

    @SerializedName("email_pembeli")
    val email_pembeli: String,

    @SerializedName("lokasi_penanaman")
    val lokasi_penanaman: String,

    @SerializedName("koordinat")
    val koordinat: String,

    @SerializedName("tanggal_pembelian")
    val tanggal_pembelian: String,

    @SerializedName("total_harga")
    val total_harga: Double,

    @SerializedName("jumlah_jenis_coral")
    val jumlah_jenis_coral: Int,

    @SerializedName("ringkasan_coral")
    val ringkasan_coral: String,

    @SerializedName("assignment_status")
    val assignment_status: String,

    @SerializedName("assigned_worker")
    val assigned_worker: String?
)