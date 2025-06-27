package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.GeocodingService
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RepositoryProvider {
    val apiService: ApiService = RetrofitClient.instance

    val geocodingService: GeocodingService by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingService::class.java)
    }

    val lokasiRepository: LokasiRepository by lazy {
        LokasiRepositoryImpl(apiService, geocodingService)
    }
}