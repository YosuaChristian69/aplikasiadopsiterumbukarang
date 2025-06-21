package id.istts.aplikasiadopsiterumbukarang.data.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.LoginRepository
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginRepositoryImpl(private val apiService: ApiService) : LoginRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = suspendCoroutine<Result<LoginResponse>> { continuation ->
                    apiService.login(loginRequest).enqueue(object : retrofit2.Callback<LoginResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<LoginResponse>,
                            response: retrofit2.Response<LoginResponse>
                        ) {
                            if (response.isSuccessful && response.body() != null) {
                                continuation.resume(Result.success(response.body()!!))
                            } else {
                                val errorMsg = try {
                                    val errorJson = JSONObject(response.errorBody()?.string() ?: "{}")
                                    errorJson.optString("msg", "Login failed")
                                } catch (e: Exception) {
                                    "Login failed: ${response.code()}"
                                }
                                continuation.resume(Result.failure(Exception(errorMsg)))
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                            continuation.resume(Result.failure(t))
                        }
                    })
                }
                response
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}