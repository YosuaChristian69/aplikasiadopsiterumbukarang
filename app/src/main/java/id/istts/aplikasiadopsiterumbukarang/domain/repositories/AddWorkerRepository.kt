package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerResponse
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.Response

class AddWorkerRepository {

    suspend fun addWorker(token: String, request: AddWorkerRequest): Response<AddWorkerResponse> {
        return RetrofitClient.instance.addUserWorker(token, request)
    }
}