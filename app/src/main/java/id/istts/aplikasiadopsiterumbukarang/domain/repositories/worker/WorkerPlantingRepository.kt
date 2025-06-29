package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

import android.content.SharedPreferences
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetailResponse
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class WorkerPlantingRepository(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
):IWorkerPlantingRepository {

    override suspend fun getPendingPlantings(token: String): Result<PendingPlantingResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getPendingPlantings("$token")
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Endpoint not found"
                    500 -> "Server error"
                    else -> "Failed to fetch pending plantings: ${response.errorBody()?.string() ?: response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }

    override suspend fun getPlantingDetails(token: String, id: Int): Result<PlantingDetailResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getPlantingDetails("$token", id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Planting details not found"
                    500 -> "Server error"
                    else -> "Failed to fetch planting details: ${response.errorBody()?.string() ?: response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }

    // CORRECTED: The signature of this function now matches what the ViewModel is calling.
    // It no longer takes a File and prepares a simple JSON request body.
    override suspend fun finishPlanting(id: Int, workerId: Int, token: String, img_url: File): Result<FinishPlantingResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val requestBody = FinishPlantingRequest(workerId = workerId)
            val imagePart = img_url!!.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData("profile_picture", it.name, requestFile)
            }
            val response = apiService.finishPlanting(
                token = token,
                id = id,
                request = requestBody,
                assignment_picture = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Planting record not found"
                    422 -> "Invalid request data."
                    500 -> "Server error"
                    else -> "Failed to finish planting: ${response.errorBody()?.string() ?: response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }
}