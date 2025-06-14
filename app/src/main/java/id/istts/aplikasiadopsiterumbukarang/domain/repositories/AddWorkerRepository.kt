package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerResponse
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class AddWorkerRepository {

    suspend fun addWorker(
        token: String,
        request: AddWorkerRequest,
        imageUri: Uri? = null,
        context: Context? = null
    ): Response<AddWorkerResponse> {

        return if (imageUri != null && context != null) {
            // Add worker with image
            Log.d("AddWorkerRepository", "Adding worker with image")
            addWorkerWithImage(token, request, imageUri, context)
        } else {
            // Add worker without image (original functionality)
            Log.d("AddWorkerRepository", "Adding worker without image")
            RetrofitClient.instance.addUserWorker(token, request)
        }
    }

    private suspend fun addWorkerWithImage(
        token: String,
        request: AddWorkerRequest,
        imageUri: Uri,
        context: Context
    ): Response<AddWorkerResponse> {
        try {
            val fileUtils = FileUtils(context)

            Log.d("AddWorkerRepository", "Processing image URI: $imageUri")

            // Convert URI to File
            val imageFile = fileUtils.getFileFromUri(imageUri)
            if (imageFile == null) {
                Log.e("AddWorkerRepository", "Failed to convert URI to file")
                throw Exception("Failed to process image file")
            }

            Log.d("AddWorkerRepository", "Image file: ${imageFile.name}, size: ${imageFile.length()} bytes, exists: ${imageFile.exists()}")

            // Create request body for image
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val profilePicture = MultipartBody.Part.createFormData(
                "profile_picture",
                imageFile.name,
                requestFile
            )

            // Create request bodies for other fields
            val nameBody = request.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val emailBody = request.email.toRequestBody("text/plain".toMediaTypeOrNull())
            val passwordBody = request.password.toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d("AddWorkerRepository", "Sending request with image to server")

            return RetrofitClient.instance.addUserWorkerWithImage(
                token = token,
                name = nameBody,
                email = emailBody,
                password = passwordBody,
                profile_picture = profilePicture
            )
        } catch (e: Exception) {
            Log.e("AddWorkerRepository", "Error in addWorkerWithImage: ${e.message}")
            throw e
        }
    }
}