package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

// 4. Repository (Enhanced with validation)
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Patterns
import id.istts.aplikasiadopsiterumbukarang.domain.CollectionResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.CoralDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileResponse
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

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
        email: String?,
        name: String?,
        imageFile: File? = null
    ): Result<EditProfileResponse> {
        return try {
            val namePart = name?.let {
                RequestBody.create("text/plain".toMediaTypeOrNull(), it)
            }
            val emailPart = email?.let {
                RequestBody.create("text/plain".toMediaTypeOrNull(), it)
            }

            val imagePart = imageFile?.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData("profile_picture", it.name, requestFile)
            }

            val response = apiService.editProfile(
                token = token,
                name = namePart,
                email = emailPart,
                profile_picture = imagePart
            )

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    Result.success(responseBody)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}