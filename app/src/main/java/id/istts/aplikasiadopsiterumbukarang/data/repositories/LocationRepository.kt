package id.istts.aplikasiadopsiterumbukarang.repositories

import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi

/**
 * An interface for the Location repository.
 * This defines the contract for data operations related to locations.
 * The ViewModel will depend on this interface, not the implementation.
 */
interface LocationRepository {
    /**
     * Fetches a list of locations where a specific coral is available.
     * @param coralId The ID of the coral to filter by.
     * @return A list of Lokasi objects.
     * @throws Exception if the network call fails or the API returns an error.
     */
    suspend fun getLocationsForCoral(coralId: Int): List<Lokasi>
}