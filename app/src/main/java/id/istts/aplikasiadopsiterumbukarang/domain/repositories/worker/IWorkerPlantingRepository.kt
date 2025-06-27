package id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker

import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetailResponse

interface IWorkerPlantingRepository {
    suspend fun getPendingPlantings(token: String): Result<PendingPlantingResponse>
    suspend fun getPlantingDetails(token: String, id: Int): Result<PlantingDetailResponse>
    suspend fun finishPlanting(id: Int, workerId: Int, token: String): Result<FinishPlantingResponse>
}
