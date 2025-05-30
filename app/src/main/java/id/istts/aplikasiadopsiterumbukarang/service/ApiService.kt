package id.istts.aplikasiadopsiterumbukarang.service

import id.istts.aplikasiadopsiterumbukarang.data.sources.local.Entities.TerumbuKarangEntities
import id.istts.aplikasiadopsiterumbukarang.domain.models.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.VerifyAndRegisterResponse
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

    @GET("/tk/getTk")
    fun getTk(
        @Header("x-auth-token") token: String,
    ): List<TerumbuKarangEntities>

}
