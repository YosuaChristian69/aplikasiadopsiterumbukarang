package id.istts.aplikasiadopsiterumbukarang.domain.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
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
    val description: String? = null,

    @SerializedName("initial_tk_population")
    val initialTkPopulation: Int? = null,

    @SerializedName("terumbu_karangs")
    val corals: List<Coral>? = null
): Parcelable