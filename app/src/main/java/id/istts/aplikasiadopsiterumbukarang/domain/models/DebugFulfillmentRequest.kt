package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class DebugFulfillmentRequest(
    @SerializedName("coralId")
    val coralId: Int,

    @SerializedName("locationId")
    val locationId: Int,

    @SerializedName("amount")
    val amount: Int
)