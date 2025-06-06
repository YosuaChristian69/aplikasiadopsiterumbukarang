package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class Lokasi(
    @SerializedName("id_lokasi")
    val idLokasi: Int,

    @SerializedName("lokasi_name")
    val lokasiName: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("description")
    val description: String,

    @SerializedName("initial_tk_population")
    val initialTkPopulation: Int
)