package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

import com.google.gson.annotations.SerializedName

data class PendingPlantingResponse(
    @SerializedName("msg")
    val msg: String,

    @SerializedName("total_pending")
    val total_pending: Int? = null,

    // I am keeping this nullable as it is a safer practice
    @SerializedName("data")
    val data: List<PendingPlanting>? = null
)