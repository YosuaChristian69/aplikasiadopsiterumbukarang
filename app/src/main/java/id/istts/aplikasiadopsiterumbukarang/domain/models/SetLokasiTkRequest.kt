package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class SetLokasiTkRequest(
    @SerializedName("id_lokasi")
    val idLokasi: Int,

    @SerializedName("id_tk")
    val idTk: Int
)