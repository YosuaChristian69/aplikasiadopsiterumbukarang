package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class AddLokasiResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: LokasiData?
)