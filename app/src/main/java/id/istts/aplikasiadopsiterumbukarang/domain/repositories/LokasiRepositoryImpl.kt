package id.istts.aplikasiadopsiterumbukarang.domain.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.GeocodingResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.GeocodingService
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LokasiRepositoryImpl(
    private val apiService: ApiService,
    private val geocodingService: GeocodingService
) : LokasiRepository {

    override suspend fun getLokasi(token: String): Result<List<Lokasi>> {
        return try {
            val response = apiService.getLokasi(token)
            if (response.isSuccessful) {
                // Jika berhasil, kirimkan list datanya
                Result.success(response.body()?.res ?: emptyList())
            } else {
                // Jika gagal (error 4xx atau 5xx), kirimkan pesan error
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Jika terjadi error jaringan, kirimkan exception-nya
            Result.failure(e)
        }
    }

    override suspend fun getAlamatFromKoordinat(lat: Double, lng: Double, apiKey: String): Result<GeocodingResponse> {
        return try {
            val response = geocodingService.reverseGeocode("$lat,$lng", apiKey)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Geocoding failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
