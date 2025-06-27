package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.GeocodingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.GeocodingService
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Interface mendefinisikan "kontrak" atau apa saja yang bisa dilakukan oleh repository ini
interface LokasiRepository {
    suspend fun getLokasi(token: String): Result<List<Lokasi>>
    suspend fun getAlamatFromKoordinat(lat: Double, lng: Double, apiKey: String): Result<GeocodingResponse>
}