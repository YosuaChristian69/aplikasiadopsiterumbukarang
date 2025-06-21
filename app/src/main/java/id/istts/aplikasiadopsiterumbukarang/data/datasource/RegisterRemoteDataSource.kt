package id.istts.aplikasiadopsiterumbukarang.data.datasource

import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterResponse
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import retrofit2.Call

class RegisterRemoteDataSource(private val apiService: ApiService) : RegisterDataSource {

    override fun requestVerification(request: RequestVerificationRequest): Call<RequestVerificationResponse> {
        return apiService.requestVerification(request)
    }

    override fun verifyAndRegister(request: VerifyAndRegisterRequest): Call<VerifyAndRegisterResponse> {
        return apiService.verifyAndRegister(request)
    }
}