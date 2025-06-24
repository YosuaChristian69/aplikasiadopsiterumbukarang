package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

// This is the main response object from the GET /collection/:id endpoint
data class CoralDetailResponse(
    @SerializedName("ownershipId")
    val ownershipId: Int,
    @SerializedName("coralNickname")
    val coralNickname: String,
    @SerializedName("adoptedAt")
    val adoptedAt: String,
    @SerializedName("species")
    val species: SpeciesDetails,
    @SerializedName("location")
    val location: LocationDetails,
    @SerializedName("planter")
    val planter: PlanterDetails?, // Planter can be null if not yet assigned
    @SerializedName("owner")
    val owner: OwnerDetails
)

// Nested data classes for the different parts of the response

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
    val address: String // Assuming the location description is the address
)

data class PlanterDetails(
    @SerializedName("full_name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone_number")
    val phone: String?, // Phone number can be null
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
