package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

data class DebugFulfillmentRequest(
    @SerializedName("coralId")
    val coralId: Int,

    @SerializedName("locationId")
    val locationId: Int,

    @SerializedName("amount")
    val amount: Int,

    // ADDED: Optional nickname for the coral.
    @SerializedName("nickname")
    val nickname: String? = null,

    // ADDED: Optional message/description for the coral.
    @SerializedName("message")
    val message: String? = null
)