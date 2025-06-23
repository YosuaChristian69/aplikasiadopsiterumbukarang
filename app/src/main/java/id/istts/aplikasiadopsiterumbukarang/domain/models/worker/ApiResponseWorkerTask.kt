package id.istts.aplikasiadopsiterumbukarang.domain.models.worker


data class ApiResponseWorkerTask<T>(
    val msg: String,
    val data: T? = null
)
