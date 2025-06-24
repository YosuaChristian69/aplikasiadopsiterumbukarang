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

    suspend fun getPendingPlantings(token: String): Result<PendingPlantingResponse> = withContext(
        Dispatchers.IO) {
        runCatching {
            val response = apiService.getPendingPlantings(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to fetch pending plantings: ${response.errorBody()?.string() ?: response.message()}")
            }
        }
    }

    suspend fun getPlantingDetails(token: String, id: Int): Result<PlantingDetailResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getPlantingDetails(token, id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to fetch planting details: ${response.errorBody()?.string() ?: response.message()}")
            }
        }
    }

    suspend fun finishPlanting(id: Int, workerId: Int, token: String): Result<FinishPlantingResponse> = withContext(Dispatchers.IO) {
        runCatching {
//            val requestBody = mapOf("workerId" to workerId)
            val requestBody = FinishPlantingRequest(workerId)
            ///finishPlanting(token, id, requestBody)
            val response = apiService.finishPlanting(
                request = requestBody,
                token = token,
                id = id
            )
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception("Failed to finish planting: ${response.errorBody()?.string() ?: response.message()}")
            }
        }
    }
    suspend fun uploadPlantingPhoto(plantingId: Int, imageFile: File, token: String): Boolean {
        return try {
            // Implementation for photo upload if needed
            // This would require multipart upload setup
            true
        } catch (e: Exception) {
            throw Exception("Photo upload error: ${e.message}")
        }
    }
}
