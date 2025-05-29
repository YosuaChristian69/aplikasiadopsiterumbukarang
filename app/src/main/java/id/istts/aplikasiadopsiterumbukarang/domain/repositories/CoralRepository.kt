package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import okhttp3.MultipartBody
import okhttp3.RequestBody

interface CoralRepository {
    suspend fun addCoral(
        token: String,
        name: RequestBody,
        jenis: RequestBody,
        harga: RequestBody,
        stok: RequestBody,
        profilePicture: MultipartBody.Part
    ): Result<String>
}