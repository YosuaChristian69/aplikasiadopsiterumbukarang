package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

// 4. Repository (Enhanced with validation)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Patterns
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileRequest
import id.istts.aplikasiadopsiterumbukarang.service.ApiService

class UserRepository(private val apiService: ApiService) {

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
            val response = apiService.editProfile("Bearer $token", request)

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