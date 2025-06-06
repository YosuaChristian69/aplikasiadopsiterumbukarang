package id.istts.aplikasiadopsiterumbukarang.domain.models

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latLng: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "id"
    ): Response<GeocodingResponse>
}