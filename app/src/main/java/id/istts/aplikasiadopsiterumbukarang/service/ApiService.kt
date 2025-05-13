package id.istts.aplikasiadopsiterumbukarang.service

import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}
