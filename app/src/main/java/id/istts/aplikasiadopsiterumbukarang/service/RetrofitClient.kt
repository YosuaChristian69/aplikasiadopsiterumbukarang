package id.istts.aplikasiadopsiterumbukarang.service

import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//    private const val BASE_URL = "http://192.168.18.16:3000"
    private const val BASE_URL = "http://10.0.2.2:3000"
//    private const val BASE_URL = "https://xkkgvtkdyk5ciukwtpzzcjdoiq0ospxl.lambda-url.us-east-1.on.aws"

    // 1. Create the logging interceptor to see network traffic
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }

    // 2. Create an OkHttpClient and add the interceptor to it
    private val client = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) // This line attaches the logger
            .build()

        retrofit.create(ApiService::class.java)
    }
}