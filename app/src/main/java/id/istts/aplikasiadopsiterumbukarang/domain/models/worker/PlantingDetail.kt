package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

import com.google.gson.annotations.SerializedName

data class PlantingDetail(
    @SerializedName("id_htrans")
    val id_htrans: Int,

    @SerializedName("status_penanaman")
    val status_penanaman: String,

    @SerializedName("tanggal_pembelian")
    val tanggal_pembelian: String,

    @SerializedName("total_harga")
    val total_harga: Double,

    @SerializedName("pembeli")
    val pembeli: Pembeli,

    @SerializedName("penanam")
    val penanam: Pembeli?, // Worker details

    @SerializedName("lokasi_penanaman")
    val lokasi_penanaman: PlantingLocation,

    @SerializedName("detail_coral")
    val detail_coral: List<CoralDetail>
)