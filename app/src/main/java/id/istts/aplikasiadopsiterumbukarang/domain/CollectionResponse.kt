package id.istts.aplikasiadopsiterumbukarang.domain


import com.google.gson.annotations.SerializedName
import id.istts.aplikasiadopsiterumbukarang.domain.models.UserCoral

data class CollectionResponse(
    @SerializedName("msg")
    val message: String,

    @SerializedName("collection")
    val collection: List<UserCoral>
)