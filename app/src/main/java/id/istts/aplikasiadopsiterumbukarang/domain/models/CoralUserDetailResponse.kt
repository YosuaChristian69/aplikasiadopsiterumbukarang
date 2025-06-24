package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

// This is the main response object from the GET /collection/:id endpoint
data class CoralDetailResponse(
    @SerializedName("ownershipDetails")
    val ownershipDetails: OwnershipDetails?, // The main nested object

    // Corrected the key to match the JSON ("planterDetails")
    @SerializedName("planterDetails")
    val planter: PlanterDetails?,

    // Corrected the key to match the JSON ("locationDetails")
    @SerializedName("locationDetails")
    val location: LocationDetails?
)

/**
 * This class now represents the object inside "ownershipDetails".
 */
data class OwnershipDetails(
    @SerializedName("id_ownership")
    val ownershipId: Int,
    @SerializedName("coral_nickname")
    val coralNickname: String?,
    @SerializedName("adopted_at")
    val adoptedAt: String?,

    // The nested objects for species and owner are inside here
    @SerializedName("terumbu_karang")
    val species: SpeciesDetails?,
    @SerializedName("user")
    val owner: OwnerDetails?
)

// These nested classes can remain mostly the same
data class SpeciesDetails(
    @SerializedName("tk_name")
    val name: String,
    @SerializedName("tk_jenis")
    val scientificName: String,
    @SerializedName("img_path")
    val imagePath: String?
)

data class LocationDetails(
    @SerializedName("lokasi_name")
    val name: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("description")
    val address: String
)

data class PlanterDetails(
    @SerializedName("full_name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone_number")
    val phone: String?,
    @SerializedName("img_path")
    val imagePath: String?
)

data class OwnerDetails(
    @SerializedName("full_name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone_number")
    val phone: String?
)
