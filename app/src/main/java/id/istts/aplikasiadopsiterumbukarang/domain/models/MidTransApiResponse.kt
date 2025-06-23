package id.istts.aplikasiadopsiterumbukarang.domain.models

import com.google.gson.annotations.SerializedName

// This is the wrapper for the response from your /transactions/gopay endpoint
data class MidtransApiResponse(
    @SerializedName("msg")
    val message: String,

    @SerializedName("transaction_details")
    val transactionDetails: MidtransTransactionResponse
)

// This holds the actual token and redirect URL from Midtrans
data class MidtransTransactionResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("redirect_url")
    val redirectUrl: String
)