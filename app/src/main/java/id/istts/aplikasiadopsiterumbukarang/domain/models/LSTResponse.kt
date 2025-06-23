package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class LSTResponse (
    @SerializedName("msg")
    val message: String,
        @SerializedName("data")
        val data: List<Lokasi>
)