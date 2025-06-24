package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a single coral instance owned by the user.
 * This class is designed to match the JSON object returned by the server's
 * `getUserCoralCollection` endpoint.
 */
data class UserCoral(
    @SerializedName("id_ownership")
    val ownershipId: Int,

    @SerializedName("id_tk")
    val coralId: Int,

    @SerializedName("coral_nickname")
    val coralNickname: String?, // Can be null if not set

    @SerializedName("coral_description")
    val coralDescription: String?, // Can be null if not set

    @SerializedName("adopted_at")
    val adoptedAt: String, // Keeping as String for easy parsing

    // This field represents the nested object containing the coral's species details.
    // The key in the JSON is "terumbu_karang".
    @SerializedName("terumbu_karang")
    val coralDetails: Coral
)
