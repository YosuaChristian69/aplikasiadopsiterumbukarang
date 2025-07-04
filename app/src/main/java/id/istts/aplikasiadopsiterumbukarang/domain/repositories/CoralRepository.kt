package id.istts.aplikasiadopsiterumbukarang.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.SingleCoralResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface CoralRepository {
    suspend fun addCoral(
        token: String,
        name: RequestBody,
        jenis: RequestBody,
        harga: RequestBody,
        stok: RequestBody,
        description: RequestBody,
        profilePicture: MultipartBody.Part
    ): Result<String>

    suspend fun getCoralList(token: String): Result<List<Coral>>
    suspend fun deleteCoral(id: Int, token: String): Result<String>
    suspend fun getSingleCoral(id: Int, token: String): Result<Coral>
    suspend fun editCoral(id: Int, token: String, editRequest: EditCoralRequest): Result<String>
}