package id.istts.aplikasiadopsiterumbukarang.service

import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.GetCoralResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.LokasiResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.SingleCoralResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.SingleUserResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.UpdateUserResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.VerifyAndRegisterResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.WorkerResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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
        @Part("description") description: RequestBody,
        @Part profile_picture: MultipartBody.Part
    ): Call<ResponseBody>

    @DELETE("/tk/deleteTk/{id}")
    suspend fun deleteTerumbuKarang(
        @Path("id") id: Int,
        @Header("x-auth-token") token: String
    ): Response<ResponseBody>

    @GET("/tk/getTk")
    suspend fun getTerumbuKarang(
        @Header("x-auth-token") token: String
    ): Response<GetCoralResponse>

    @GET("/users/fetchAllUsers")
    suspend fun fetchAllUsers(
        @Header("x-auth-token") token: String
    ): Response<WorkerResponse>

    @GET("/users/fetchUser/{id}")
    suspend fun fetchUserById(
        @Path("id") id: String,
        @Header("x-auth-token") token: String
    ): Response<SingleUserResponse>


    @POST("/users/addUserWorker")
    suspend fun addUserWorker(
        @Header("x-auth-token") token: String,
        @Body request: AddWorkerRequest
    ): Response<AddWorkerResponse>
    @PUT("/users/updateUser/{id}")
    suspend fun updateUserById(
        @Path("id") id: String,
        @Header("x-auth-token") token: String,
        @Body updateData: Map<String, String>
    ): Response<UpdateUserResponse>

    // New endpoints for edit functionality
    @GET("/tk/getSingleTk/{id}")
    suspend fun getSingleTerumbuKarang(
        @Path("id") id: Int,
        @Header("x-auth-token") token: String
    ): Response<SingleCoralResponse>

    @POST("/tk/editTk/{id}")
    suspend fun editTerumbuKarang(
        @Path("id") id: Int,
        @Header("x-auth-token") token: String,
        @Body editRequest: EditCoralRequest
    ): Response<ResponseBody>


    //LOKASI
    @GET("/lokasi/Lokasi")
    suspend fun getLokasi(
        @Header("x-auth-token") token: String
    ): Response<LokasiResponse>
}