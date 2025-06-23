package id.istts.aplikasiadopsiterumbukarang.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.LSTResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The concrete implementation of the LocationRepository.
 * It uses Retrofit's ApiService to fetch data from the network.
 */
class LocationRepositoryImpl(
    private val apiService: ApiService,
    private val sessionManager: SessionManager // Dependency for getting the auth token
) : LocationRepository {

    override suspend fun getLocationsForCoral(coralId: Int): List<Lokasi> {
        // Use withContext(Dispatchers.IO) for network operations
        return withContext(Dispatchers.IO) {
            val token = sessionManager.fetchAuthToken()
            if (token.isNullOrEmpty()) {
                throw Exception("User is not authenticated.")
            }

            val response = apiService.getLocationsForSpecies(
                token = token,
                coralId = coralId

            )

            if (response.isSuccessful) {
                // This is the "unwrapping" step. We return the list from the 'data' field.
                // If the body or data is null, return an empty list.
                response.body()?.data ?: emptyList()
            } else {
                // If the API call fails, throw an exception with the error code.
                throw Exception("Failed to fetch locations: API Error ${response.code()}")
            }
        }
    }
}