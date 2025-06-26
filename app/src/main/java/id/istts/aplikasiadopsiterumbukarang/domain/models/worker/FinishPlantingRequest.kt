package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

import com.google.gson.annotations.SerializedName

// This data class models the exact JSON body `{"worker_id": ...}` that your backend expects.
data class FinishPlantingRequest(
    @SerializedName("worker_id")
    val workerId: Int
)
