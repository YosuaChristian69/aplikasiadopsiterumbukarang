package id.istts.aplikasiadopsiterumbukarang.repositories

import CoralRepository
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CoralRepositoryImpl : CoralRepository {
    override suspend fun getCoralList(token: String): Result<List<Coral>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.getTerumbuKarang(token)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error occurred"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid token or token spoofing detected"
                    401 -> "Invalid or expired token. Please login again."
                    403 -> "Access forbidden"
                    404 -> "Endpoint not found"
                    500 -> "Internal server error"
                    else -> "Failed to fetch coral data. Error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

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
