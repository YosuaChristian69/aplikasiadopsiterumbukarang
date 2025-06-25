package id.istts.aplikasiadopsiterumbukarang.service

import id.istts.aplikasiadopsiterumbukarang.domain.CollectionResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddLokasiRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddLokasiResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.CoralDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.CreateTransactionRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.DebugFulfillmentRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.GetCoralResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.LSTResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.login.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.login.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.LokasiResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.MidtransApiResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.SetLokasiTkRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.SetLokasiTkResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.SingleCoralResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.SingleUserResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.UpdateUserResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.register.VerifyAndRegisterResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.WorkerResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.CompleteTaskResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.FinishPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlantingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PlantingDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.UnfinishedTasksResponse
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
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

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
    // Add this method to your existing ApiService interface

    @Multipart
    @POST("/users/addUserWorker")
    suspend fun addUserWorkerWithImage(
        @Header("x-auth-token") token: String,
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profile_picture: MultipartBody.Part
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
    // Add this method to your existing ApiService interface

    @POST("/lokasi/LokasiGoogleSpesiesTersedia")
    suspend fun addLokasiGoogle(
        @Header("x-auth-token") token: String,
        @Body request: AddLokasiRequest
    ): Response<AddLokasiResponse>
    @GET("/lokasi/Lokasi")
    suspend fun getLokasi(
        @Header("x-auth-token") token: String
    ): Response<LokasiResponse>
    @GET("/lokasi/lokasi-spesies-tersedia") // Use your actual route path
    suspend fun getLocationsForSpecies(
        @Header("x-auth-token") token: String,
        @Query("id_tk") coralId: Int // This adds "?id_tk=..." to the URL
    ): Response<LSTResponse>
    @POST("transaksi/purchase") // Use your actual route
    suspend fun createGopayTransaction(
        @Header("x-auth-token") token: String,
        @Body request: CreateTransactionRequest
    ): Response<MidtransApiResponse>

    @POST("transaksi/fulfill-debug") // The debug route
    suspend fun fulfillOrderForDebug(
        @Header("x-auth-token") token: String,
        @Body request: DebugFulfillmentRequest
    ): Response<Unit>
    @GET("transaksi/collection") // The endpoint URL from your router
    suspend fun getUserCoralCollection(
        @Header("x-auth-token") token: String // The authorization header
    ): Response<CollectionResponse>
    @GET("transaksi/collection/{id}")
    suspend fun getSingleCoralDetail(
        @Header("x-auth-token") token: String,
        @Path("id") ownershipId: Int // The ID from the URL
    ): Response<CoralDetailResponse>

    //WORKER
    @POST("fetchAllUnfinishedTask")
    suspend fun fetchAllUnfinishedTasks(): Response<UnfinishedTasksResponse>

    @Multipart
    @POST("completeTask/{htid}")
    suspend fun completeTask(
        @Path("htid") htid: Int,
        @Part profilePicture: MultipartBody.Part
    ): Response<CompleteTaskResponse>

    @GET("/users/worker/pendingPlantings")
    suspend fun getPendingPlantings(
        @Header("x-auth-token") token: String
    ): Response<PendingPlantingResponse>

    @GET("/users/worker/plantingDetails/{id}")
    suspend fun getPlantingDetails(
        @Header("x-auth-token") token: String,
        @Path("id") id: Int
    ): Response<PlantingDetailResponse>

    @PUT("/users/worker/finishPlanting/{id}")
    suspend fun finishPlanting(
        @Header("x-auth-token") token: String,
        @Path("id") id: Int,
        @Body request: FinishPlantingRequest
    ): Response<FinishPlantingResponse>

    @Multipart
    @PUT("/users/worker/editUserWorker")
    suspend fun editProfile(
        @Header("x-auth-token") token: String,
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part profile_picture: MultipartBody.Part?
    ): Response<EditProfileResponse>
}