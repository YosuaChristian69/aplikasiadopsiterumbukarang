package id.istts.aplikasiadopsiterumbukarang.repositories

import android.util.Log
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepositoryImpl(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : LocationRepository {

    override suspend fun getLocationsForCoral(coralId: Int): List<Lokasi> {
        return withContext(Dispatchers.IO) {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrEmpty()) {
                throw Exception("User is not authenticated.")
            }

            // Call the service with the RAW token, no "Bearer " prefix
            val response = apiService.getLocationsForSpecies(
                token = token,
                coralId = coralId
            )

            // ... rest of your successful/failure logic
            if (response.isSuccessful) {
                val locationsList = response.body()?.data ?: emptyList()
                Log.d("APIDEBUG", "API call successful. Found ${locationsList.size} locations.")
                locationsList
            } else {
                throw Exception("Failed to fetch locations: API Error ${response.code()}")
            }
        }
    }
}
