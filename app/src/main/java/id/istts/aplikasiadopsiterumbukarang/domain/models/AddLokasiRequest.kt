package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class AddLokasiRequest(
    @SerializedName("address")
    val address: String
)