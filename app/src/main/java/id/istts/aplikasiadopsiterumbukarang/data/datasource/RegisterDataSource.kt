package id.istts.aplikasiadopsiterumbukarang.data.datasource

import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterResponse
import retrofit2.Call

interface RegisterDataSource {
    fun requestVerification(request: RequestVerificationRequest): Call<RequestVerificationResponse>
    fun verifyAndRegister(request: VerifyAndRegisterRequest): Call<VerifyAndRegisterResponse>
}