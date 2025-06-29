package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

import com.google.gson.annotations.SerializedName

data class CoralDetail(
    @SerializedName("id_dtrans")
    val id_dtrans: Int,

    @SerializedName("nama_coral")
    val nama_coral: String,

    @SerializedName("jenis")
    val jenis: String,

    @SerializedName("harga_satuan")
    val harga_satuan: Double,

    @SerializedName("jumlah")
    val jumlah: Int,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("deskripsi")
    val deskripsi: String?,

    // ADDED: New field to hold the image URL from the API response
    @SerializedName("ImgUrl")
    val ImgUrl: String?
)