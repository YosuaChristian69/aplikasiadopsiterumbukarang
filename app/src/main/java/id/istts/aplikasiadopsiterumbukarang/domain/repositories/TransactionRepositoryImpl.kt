package id.istts.aplikasiadopsiterumbukarang.domain.repositories


import id.istts.aplikasiadopsiterumbukarang.domain.models.CreateTransactionRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.DebugFulfillmentRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.MidtransTransactionResponse
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TransactionRepositoryImpl(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : TransactionRepository {

    override suspend fun createAdoptionTransaction(request: CreateTransactionRequest): MidtransTransactionResponse {
        return withContext(Dispatchers.IO) {
            val token = sessionManager.fetchAuthToken()
                ?: throw Exception("User not authenticated.")

            val response = apiService.createGopayTransaction(token, request)

            if (response.isSuccessful) {
                // Safely unwrap the response and return the nested transaction details
                response.body()?.transactionDetails
                    ?: throw Exception("Empty response body from server.")
            } else {
                // Parse the error message from the backend
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody!!).getString("error")
                } catch (e: Exception) {
                    "Failed to create transaction: API Error ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        }
    }

//    override suspend fun fulfillOrderForDebug(
//        coralId: Int,
//        locationId: Int,
//        amount: Int,
//        nickname: String?,
//        message: String?
//    ) {
//        TODO("Not yet implemented")
//    }

    override suspend fun fulfillOrderForDebug(coralId: Int, locationId: Int, amount: Int , nickname: String?, message: String?) {
        withContext(Dispatchers.IO) {
            val token = sessionManager.fetchAuthToken()
                ?: throw Exception("User not authenticated.")

            val request = DebugFulfillmentRequest(coralId, locationId, amount, nickname, message)
            val response = apiService.fulfillOrderForDebug(token, request)

            if (!response.isSuccessful) {
                throw Exception("Failed to fulfill debug order: API Error ${response.code()}")
            }
            // No need to return anything on success
        }
    }
}