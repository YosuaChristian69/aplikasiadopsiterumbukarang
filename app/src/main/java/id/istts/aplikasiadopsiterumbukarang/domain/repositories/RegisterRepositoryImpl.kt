package id.istts.aplikasiadopsiterumbukarang.data.repository

import id.istts.aplikasiadopsiterumbukarang.data.datasource.RegisterDataSource
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterResponse
import id.istts.aplikasiadopsiterumbukarang.domain.repository.RegisterRepository
import retrofit2.Call

class RegisterRepositoryImpl(
    private val remoteDataSource: RegisterDataSource
) : RegisterRepository {

    override fun requestVerification(request: RequestVerificationRequest): Call<RequestVerificationResponse> {
        return remoteDataSource.requestVerification(request)
    }

    override fun verifyAndRegister(request: VerifyAndRegisterRequest): Call<VerifyAndRegisterResponse> {
        return remoteDataSource.verifyAndRegister(request)
    }
}