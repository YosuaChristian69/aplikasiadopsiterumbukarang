package id.istts.aplikasiadopsiterumbukarang.domain.models


import com.google.gson.annotations.SerializedName

data class CreateTransactionRequest(
    @SerializedName("locationId")
    val locationId: Int,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("items")
    val items: List<TransactionItem>
)

data class TransactionItem(
    @SerializedName("id_tk")
    val coralId: Int,

    @SerializedName("amount")
    val amount: Int
)