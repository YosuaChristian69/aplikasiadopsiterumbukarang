package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.repositories.LocationRepository
import id.istts.aplikasiadopsiterumbukarang.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LocationSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var mockLocationRepository: LocationRepository
    private lateinit var viewModel: LocationSelectionViewModel

    private val testCoralId = 1

    @Before
    fun setUp() {
        mockLocationRepository = mock()
    }

    @Test
    fun `init - state awal harus loading dan tidak ada error`() = runTest {
        viewModel = LocationSelectionViewModel(testCoralId, mockLocationRepository)

        val initialState = viewModel.uiState.value
        assertTrue("ViewModel harus dalam state loading saat inisialisasi", initialState.isLoading)
        assertNull("Seharusnya tidak ada error saat inisialisasi", initialState.error)
        assertTrue("List lokasi seharusnya kosong saat inisialisasi", initialState.availableLocations.isEmpty())
    }

    @Test
    fun `fetchAvailableLocations - sukses - state harus update dengan data lokasi dan loading false`() = runTest {
        val mockLocations = listOf(
            Lokasi(idLokasi = 1, lokasiName = "Taman Laut Bunaken", latitude = 1.6144, longitude = 124.7818),
            Lokasi(idLokasi = 2, lokasiName = "Raja Ampat", latitude = -0.5562, longitude = 130.5183, description = "Kepulauan indah")
        )
        whenever(mockLocationRepository.getLocationsForCoral(testCoralId)).thenReturn(mockLocations)

        viewModel = LocationSelectionViewModel(testCoralId, mockLocationRepository)

        testDispatcher.scheduler.runCurrent()

        val finalState = viewModel.uiState.value
        assertFalse("Loading harus false setelah fetch berhasil", finalState.isLoading)
        assertEquals("Data lokasi harus sesuai dengan yang dari repository", mockLocations, finalState.availableLocations)
        assertEquals("Jumlah lokasi harus sesuai", 2, finalState.availableLocations.size)
        assertNull("Error harus null setelah fetch berhasil", finalState.error)
    }

    @Test
    fun `fetchAvailableLocations - gagal - state harus update dengan pesan error dan loading false`() = runTest {
        val errorMessage = "Network error"
        whenever(mockLocationRepository.getLocationsForCoral(testCoralId)).thenThrow(RuntimeException(errorMessage))

        viewModel = LocationSelectionViewModel(testCoralId, mockLocationRepository)

        testDispatcher.scheduler.runCurrent()

        // THEN: State UI harus menunjukkan error setelah coroutine selesai
        val finalState = viewModel.uiState.value
        assertFalse("Loading harus false setelah fetch gagal", finalState.isLoading)
        assertNotNull("Error tidak boleh null setelah fetch gagal", finalState.error)
        assertEquals("Pesan error harus sesuai dengan yang dari exception", errorMessage, finalState.error)
        assertTrue("List lokasi harus kosong setelah fetch gagal", finalState.availableLocations.isEmpty())
    }

    @Test
    fun `onLocationSelected - state harus update dengan lokasi yang dipilih`() = runTest {
        whenever(mockLocationRepository.getLocationsForCoral(testCoralId)).thenReturn(emptyList())
        viewModel = LocationSelectionViewModel(testCoralId, mockLocationRepository)
        testDispatcher.scheduler.runCurrent() // Jalankan init fetch

        val selectedLocation = Lokasi(idLokasi = 5, lokasiName = "Gili Trawangan", latitude = -8.3499, longitude = 116.0396)

        viewModel.onLocationSelected(selectedLocation)

        val currentState = viewModel.uiState.value
        assertEquals("Lokasi yang dipilih harus sesuai", selectedLocation, currentState.selectedLocation)
        assertEquals("ID lokasi yang dipilih harus benar", 5, currentState.selectedLocation?.idLokasi)
    }

    @Test
    fun `clearError - state error harus menjadi null`() = runTest {
        // GIVEN: ViewModel dalam state error
        val errorMessage = "Initial error"
        whenever(mockLocationRepository.getLocationsForCoral(testCoralId)).thenThrow(RuntimeException(errorMessage))
        viewModel = LocationSelectionViewModel(testCoralId, mockLocationRepository)
        testDispatcher.scheduler.runCurrent() // Jalankan init fetch agar state error terisi
        assertEquals("Prasyarat: state error harus ada", errorMessage, viewModel.uiState.value.error)

        // WHEN: clearError dipanggil
        viewModel.clearError()

        // THEN: Error di state UI harus menjadi null
        val currentState = viewModel.uiState.value
        assertNull("Error harusnya sudah null setelah dibersihkan", currentState.error)
    }
}