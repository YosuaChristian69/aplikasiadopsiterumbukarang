package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

/**
 * Response for creating a single location-species association.
 */
data class SetLokasiTkResponse(
    @SerializedName("msg")
    val message: String,

    @SerializedName("data")
    val data: LokasiSpesiesData
)

data class LokasiSpesiesData(
    @SerializedName("id_lokasi")
    val idLokasi: Int,

    @SerializedName("id_tk")
    val idTk: Int
)