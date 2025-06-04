package id.istts.aplikasiadopsiterumbukarang.domain.models

import java.util.Date

data class Worker(
    val id_user: String,
    val full_name: String,
    val email: String,
    val status: String,
    val balance: String,
    val user_status:String,
    val joined_at: Date
)
