package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class EditProfileResponse(
    val msg: String,
    val photo_updated: Boolean? = null,
    val user: UpdatedUserData? = null
)

