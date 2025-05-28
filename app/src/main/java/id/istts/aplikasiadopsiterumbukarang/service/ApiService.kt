package id.istts.aplikasiadopsiterumbukarang.service

import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RegisterRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RegisterResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.VerifyAndRegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
    @POST("/users/register")
    fun register(@Body registerRequest: RegisterRequest): Call<RegisterResponse>

    @POST("/users/requestVerification")
    fun requestVerification(@Body request: RequestVerificationRequest): Call<RequestVerificationResponse>

    @POST("/users/verifyAndRegister")
    fun verifyAndRegister(@Body request: VerifyAndRegisterRequest): Call<VerifyAndRegisterResponse>

    @Multipart
    @POST("/tk/addTk")
    fun addTerumbuKarang(
        @Header("x-auth-token") token: String,
        @Part("name") name: RequestBody,
        @Part("jenis") jenis: RequestBody,
        @Part("harga") harga: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part profile_picture: MultipartBody.Part
    ): Call<ResponseBody>

}
