package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class LokasiResponse(
    @SerializedName("res")
    val res: List<Lokasi>
)