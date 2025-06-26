package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

import com.google.gson.annotations.SerializedName

data class PlantingLocation(
    @SerializedName("nama")
    val nama: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("deskripsi")
    val deskripsi: String?
)