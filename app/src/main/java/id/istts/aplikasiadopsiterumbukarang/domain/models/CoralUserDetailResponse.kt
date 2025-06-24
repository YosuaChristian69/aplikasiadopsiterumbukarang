package id.istts.aplikasiadopsiterumbukarang.domain.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
// This is the main response object from the GET /collection/:id endpoint

@Parcelize
data class CoralDetailResponse(
    @SerializedName("ownershipId")
    val ownershipId: Int,
    @SerializedName("coralNickname")
    val coralNickname: String?,
    @SerializedName("adoptedAt")
    val adoptedAt: String?,

    // Nested objects for details
    @SerializedName("species")
    val species: SpeciesDetails?,
    @SerializedName("location")
    val location: LocationDetails?,
    @SerializedName("planter")
    val planter: PlanterDetails?,
    @SerializedName("owner")
    val owner: OwnerDetails?
) : Parcelable

// The rest of these nested classes are also Parcelable

@Parcelize
data class SpeciesDetails(
    @SerializedName("tk_name")
    val name: String?,
    @SerializedName("tk_jenis")
    val scientificName: String?,
    @SerializedName("img_path")
    val imagePath: String?
) : Parcelable

@Parcelize
data class LocationDetails(
    @SerializedName("lokasi_name")
    val name: String?,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("description")
    val address: String?
) : Parcelable

@Parcelize
data class PlanterDetails(
    @SerializedName("full_name")
    val name: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone_number")
    val phone: String?,
    @SerializedName("img_path")
    val imagePath: String?
) : Parcelable

@Parcelize
data class OwnerDetails(
    @SerializedName("full_name")
    val name: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone_number")
    val phone: String?
) : Parcelable
