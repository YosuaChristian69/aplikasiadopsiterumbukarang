package id.istts.aplikasiadopsiterumbukarang.repositories

import CoralRepository
import android.content.ContentValues.TAG
import android.util.Log
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlin.math.log

class CoralRepositoryImpl : CoralRepository {
    override suspend fun getCoralList(token: String): Result<List<Coral>> = withContext(Dispatchers.IO) {
        try {
            val tokenWithBearer = token
            val tokenfix = tokenWithBearer.replace("Bearer ", "", ignoreCase = true)
            val response = RetrofitClient.instance.getTerumbuKarang(tokenfix)
            Log.d(TAG, "Response: ${response.body().toString()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.data != null) {
                        Result.success(body.data)
                    } else {
                        // Handle empty data case or error message
                        val message = body.msg ?: "No data available"
                        if (message == "Data kosong") {
                            // Return empty list for "Data kosong" case
                            Result.success(emptyList())
                        } else {
                            Result.failure(Exception(message))
                        }
                    }
                } else {
                    Result.failure(Exception("Response body is null"))
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
            Log.e(TAG, "Network error in getCoralList", e)
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