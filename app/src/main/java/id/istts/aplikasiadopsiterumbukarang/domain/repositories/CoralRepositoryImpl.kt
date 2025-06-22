package id.istts.aplikasiadopsiterumbukarang.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.google.gson.Gson
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.DeleteResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CoralRepositoryImpl : CoralRepository {

    private val apiService = RetrofitClient.instance

    override suspend fun getCoralList(token: String): Result<List<Coral>> = withContext(Dispatchers.IO) {
        try {
            val tokenWithBearer = token
            val tokenfix = tokenWithBearer.replace("Bearer ", "", ignoreCase = true)
            val response = apiService.getTerumbuKarang(tokenfix)
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
        description: RequestBody,
        profilePicture: MultipartBody.Part
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addTerumbuKarang(
                token = token,
                name = name,
                jenis = jenis,
                harga = harga,
                stok = stok,
                description = description,
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

    override suspend fun deleteCoral(id: Int, token: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Remove Bearer prefix if present since backend expects raw token
            val cleanToken = token.replace("Bearer ", "", ignoreCase = true)
            val response = apiService.deleteTerumbuKarang(id, cleanToken)

            Log.d(TAG, "Delete response code: ${response.code()}")

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Log.d(TAG, "Delete response body: $responseBody")

                if (responseBody != null && responseBody.isNotEmpty()) {
                    try {
                        val gson = Gson()
                        val deleteResponse = gson.fromJson(responseBody, DeleteResponse::class.java)
                        Result.success(deleteResponse.msg)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing delete response", e)
                        Result.success("Coral deleted successfully")
                    }
                } else {
                    Result.success("Coral deleted successfully")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Delete error body: $errorBody")

                val errorMessage = if (errorBody != null && errorBody.isNotEmpty()) {
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, DeleteResponse::class.java)
                        errorResponse.msg
                    } catch (e: Exception) {
                        when (response.code()) {
                            400 -> "Coral not found or unauthorized access"
                            401 -> "Invalid or expired token. Please login again."
                            403 -> "Access forbidden"
                            404 -> "Coral not found"
                            500 -> "Internal server error"
                            else -> "Failed to delete coral. Error: ${response.code()}"
                        }
                    }
                } else {
                    when (response.code()) {
                        400 -> "Coral not found or unauthorized access"
                        401 -> "Invalid or expired token. Please login again."
                        403 -> "Access forbidden"
                        404 -> "Coral not found"
                        500 -> "Internal server error"
                        else -> "Failed to delete coral. Error: ${response.code()}"
                    }
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in deleteCoral", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun getSingleCoral(id: Int, token: String): Result<Coral> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = token.replace("Bearer ", "", ignoreCase = true)
            val response = apiService.getSingleTerumbuKarang(id, cleanToken)

            Log.d(TAG, "Get single coral response code: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.corral)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid request or coral not found"
                    401 -> "Invalid or expired token. Please login again."
                    403 -> "Access forbidden"
                    404 -> "Coral not found"
                    500 -> "Internal server error"
                    else -> "Failed to fetch coral data. Error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in getSingleCoral", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun editCoral(id: Int, token: String, editRequest: EditCoralRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = token.replace("Bearer ", "", ignoreCase = true)
            val response = apiService.editTerumbuKarang(id, cleanToken, editRequest)

            Log.d(TAG, "Edit coral response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success("Coral updated successfully")
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid data or coral not found"
                    401 -> "Invalid or expired token. Please login again."
                    403 -> "Access forbidden"
                    404 -> "Coral not found"
                    500 -> "Internal server error"
                    else -> "Failed to update coral. Error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error in editCoral", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}