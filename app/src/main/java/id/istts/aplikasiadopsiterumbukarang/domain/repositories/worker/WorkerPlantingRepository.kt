package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

import android.content.SharedPreferences
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlanting
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetail
import id.istts.aplikasiadopsiterumbukarang.service.ApiService

// Alternative: Use different name for generic parameter
class WorkerPlantingRepository<T>(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
) {

    private fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    suspend fun getPendingPlantings(): Result<List<PendingPlanting>> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("No auth token found"))
            val response = apiService.getPendingPlantings("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlantingDetails(id: Int): Result<T> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("No auth token found"))
            val response = apiService.getPlantingDetails("Bearer $token", id)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(body.data as T)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                when (response.code()) {
                    404 -> Result.failure(Exception("Detail pembelian tidak ditemukan"))
                    else -> Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finishPlanting(id: Int, workerId: Int? = null): Result<String> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("No auth token found"))
            val request = FinishPlantingRequest(worker_id = workerId)
            val response = apiService.finishPlanting("Bearer $token", id, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.msg)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                when (response.code()) {
                    404 -> Result.failure(Exception("Transaksi tidak ditemukan"))
                    400 -> Result.failure(Exception("Coral sudah ditanam sebelumnya"))
                    else -> Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}