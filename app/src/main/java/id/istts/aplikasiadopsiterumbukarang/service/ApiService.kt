package id.istts.aplikasiadopsiterumbukarang.service

import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RegisterRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RegisterResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.VerifyAndRegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
    @POST("/users/register")
    fun register(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @POST("/users/requestVerification")
    fun requestVerification(@Body request: RequestVerificationRequest): Call<RequestVerificationResponse>

    @POST("/users/verifyAndRegister")
    fun verifyAndRegister(@Body request: VerifyAndRegisterRequest): Call<VerifyAndRegisterResponse>
}
