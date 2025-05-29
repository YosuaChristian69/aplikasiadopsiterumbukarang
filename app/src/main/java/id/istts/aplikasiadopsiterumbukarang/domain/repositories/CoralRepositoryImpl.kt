package id.istts.aplikasiadopsiterumbukarang.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CoralRepositoryImpl : CoralRepository {

    override suspend fun addCoral(
        token: String,
        name: RequestBody,
        jenis: RequestBody,
        harga: RequestBody,
        stok: RequestBody,
        profilePicture: MultipartBody.Part
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.addTerumbuKarang(
                token = token,
                name = name,
                jenis = jenis,
                harga = harga,
                stok = stok,
                profile_picture = profilePicture
            ).execute()

            if (response.isSuccessful) {
                Result.success("Coral added successfully")
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid data or unauthorized access"
                    401 -> "Session expired. Please login again."
                    else -> "Failed to add coral. Error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}