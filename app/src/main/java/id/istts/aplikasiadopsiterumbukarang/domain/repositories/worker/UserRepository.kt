package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

// 4. Repository (Enhanced with validation)
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Patterns
import id.istts.aplikasiadopsiterumbukarang.domain.CollectionResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.CoralDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileRequest
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import retrofit2.Response

class UserRepository(private val apiService: ApiService) {

    suspend fun getSingleCoralDetail(token: String, ownershipId: Int): Response<CoralDetailResponse> {
        Log.d("UserRepository", "Fetching detail for ownership ID: $ownershipId")
        return try {
            apiService.getSingleCoralDetail(token, ownershipId)
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception while fetching single coral detail", e)
            throw e
        }
    }
    suspend fun getUserCoralCollection(token: String): Response<CollectionResponse> {
        Log.d("UserRepository", "Attempting to fetch user coral collection with token.")
        return try {
            // Call the API service endpoint.
            val response = apiService.getUserCoralCollection(token)
            Log.d("UserRepository", "API response received with code: ${response.code()}")
            response
        } catch (e: Exception) {
            // It's good practice to catch potential exceptions (e.g., network issues)
            // and log them, although the ViewModel will also handle this.
            Log.e("UserRepository", "Exception while fetching user coral collection", e)
            // Re-throw the exception to be handled by the ViewModel's try-catch block.
            throw e
        }
    }
    suspend fun editProfile(
        token: String,
        email: String? = null,
        name: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate input at repository level
            if (email.isNullOrBlank() && name.isNullOrBlank()) {
                return@withContext Result.failure(Exception("At least one field (email or name) must be provided"))
            }

            email?.let { emailValue ->
                if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                    return@withContext Result.failure(Exception("Invalid email format"))
                }
            }

            name?.let { nameValue ->
                if (nameValue.length < 2) {
                    return@withContext Result.failure(Exception("Name must be at least 2 characters"))
                }
            }

            val request = EditProfileRequest(email = email, name = name)
            val response = apiService.editProfile(token, request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body.msg)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMsg = when (response.code()) {
                    400 -> {
                        try {
                            val errorBody = response.errorBody()?.string()
                            errorBody ?: "Bad Request"
                        } catch (e: Exception) {
                            "Bad Request"
                        }
                    }
                    401 -> "Token Spoofing - Invalid token"
                    403 -> "Forbidden - Access denied"
                    404 -> "Endpoint not found"
                    422 -> "Email already exists - Please use a different email"
                    500 -> "Server error - Please try again later"
                    else -> "HTTP Error: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Network timeout - Please check your connection"))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Network error - Please check your internet connection"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}