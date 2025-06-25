package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

import android.content.SharedPreferences
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetailResponse
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WorkerPlantingRepository(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun getPendingPlantings(token: String): Result<PendingPlantingResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getPendingPlantings("Bearer $token")
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

    suspend fun getPlantingDetails(token: String, id: Int): Result<PlantingDetailResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getPlantingDetails("Bearer $token", id)
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

    suspend fun finishPlanting(id: Int, workerId: Int, token: String): Result<FinishPlantingResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val requestBody = FinishPlantingRequest(workerId)
            val response = apiService.finishPlanting(
                request = requestBody,
                token = "Bearer $token",
                id = id
            )
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Planting record not found"
                    422 -> "Invalid request data"
                    500 -> "Server error"
                    else -> "Failed to finish planting: ${response.errorBody()?.string() ?: response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }

    suspend fun uploadPlantingPhoto(plantingId: Int, imageFile: File, token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            // Implementation for photo upload if needed
            // This would require multipart upload setup
            // For now, return success
            true
        }
    }
}