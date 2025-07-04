package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

import android.content.SharedPreferences
import android.util.Log
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
import okhttp3.RequestBody.Companion.toRequestBody
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
            val workerIdPart = workerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = img_url!!.let {
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData("assignment_picture", it.name, requestFile)
            }
            Log.d("workerId",workerIdPart.toString())
            Log.d("img",imagePart.toString())
            val response = apiService.finishPlanting(
                token = token,
                id = id,
                workerId = workerIdPart,
                assignment_picture = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorBodyString = response.errorBody()?.string() ?: response.message()
                Log.d("FinishPlantingRepo", "API Error - Code: ${response.code()}, Body: $errorBodyString")
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Planting record not found"
                    422 -> "Invalid request data."
                    500 -> "Server error${response.errorBody()?.string() ?: response.message()}\""
                    else -> "Failed to finish planting: ${response.errorBody()?.string() ?: response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }
}