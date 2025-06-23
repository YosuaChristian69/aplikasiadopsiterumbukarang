package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.CreateTransactionRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.MidtransTransactionResponse


interface TransactionRepository {
    /**
     * Calls the backend to create a Midtrans transaction.
     * @return The MidtransTransactionResponse containing the Snap Token.
     * @throws Exception if the API call fails.
     */
    suspend fun createAdoptionTransaction(request: CreateTransactionRequest): MidtransTransactionResponse

    /**
     * Calls the debug endpoint to manually fulfill an order.
     * @throws Exception if the API call fails.
     */
    suspend fun fulfillOrderForDebug(coralId: Int, locationId: Int, amount: Int)
}