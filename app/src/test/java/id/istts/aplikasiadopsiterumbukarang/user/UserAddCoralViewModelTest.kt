package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.getOrAwaitValue
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.MainDispatcherRule
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UserAddCoralViewModelTest {

    // Rule untuk menjalankan background task LiveData secara sinkron
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Rule untuk mengganti Main dispatcher dengan Test dispatcher
    private val testDispatcher = StandardTestDispatcher()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    // Mocks untuk dependencies
    // Mocks untuk dependencies
    private lateinit var mockCoralRepository: CoralRepositoryImpl // <-- Ubah ke kelas implementasi
    private lateinit var mockSessionManager: SessionManager

    // ViewModel yang akan diuji
    private lateinit var viewModel: UserAddCoralViewModel

    @Before
    fun setUp() {
        // Inisialisasi mock sebelum setiap test
        mockCoralRepository = mock()
        mockSessionManager = mock()

        // Selalu inisialisasi ViewModel di setiap test agar state bersih
        viewModel = UserAddCoralViewModel(mockCoralRepository, mockSessionManager)
    }

    @Test
    fun `loadCoralDetails - token valid dan repository sukses - uiState harus update dengan data coral`() = runTest {
        // GIVEN
        val testCoralId = 1
        val testToken = "valid_token"
        val mockCoral = Coral(
            id_tk = testCoralId, tk_name = "Acropora", tk_jenis = "Staghorn",
            harga_tk = 150000, stok_tersedia = 10, description = "A beautiful coral",
            is_deleted = false, img_path = null, public_id = null
        )

        whenever(mockSessionManager.fetchAuthToken()).thenReturn(testToken)
        whenever(mockCoralRepository.getSingleCoral(testCoralId, testToken)).thenReturn(Result.success(mockCoral))

        // WHEN
        viewModel.loadCoralDetails(testCoralId)
        testDispatcher.scheduler.runCurrent() // Jalankan coroutine

        // THEN
        val uiState = viewModel.uiState.value
        assertFalse("isLoading should be false after success", uiState.isLoading)
        assertNull("Error should be null on success", uiState.error)
        assertNotNull("Coral data should not be null", uiState.coral)
        assertEquals("Coral ID should match", testCoralId, uiState.coral?.id_tk)

        // Periksa juga state tombol, seharusnya masih false karena lokasi belum dipilih
        val isButtonEnabled = viewModel.isNextButtonEnabled.getOrAwaitValue()
        assertFalse("Next button should be disabled until location is selected", isButtonEnabled)
    }

    @Test
    fun `loadCoralDetails - token valid dan repository gagal - uiState harus update dengan error`() = runTest {
        // GIVEN
        val testCoralId = 1
        val testToken = "valid_token"
        val errorMessage = "Database error"
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(testToken)
        whenever(mockCoralRepository.getSingleCoral(testCoralId, testToken)).thenReturn(Result.failure(Exception(errorMessage)))

        // WHEN
        viewModel.loadCoralDetails(testCoralId)
        testDispatcher.scheduler.runCurrent()

        // THEN
        val uiState = viewModel.uiState.value
        assertFalse("isLoading should be false after failure", uiState.isLoading)
        assertNull("Coral data should be null on failure", uiState.coral)
        assertNotNull("Error should not be null", uiState.error)
        assertEquals("Error message should match", errorMessage, uiState.error)
    }

    @Test
    fun `loadCoralDetails - token tidak valid - uiState harus update dengan error sesi`() = runTest {
        // GIVEN
        val testCoralId = 1
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(null) // Token null

        // WHEN
        viewModel.loadCoralDetails(testCoralId)
        testDispatcher.scheduler.runCurrent()

        // THEN
        val uiState = viewModel.uiState.value
        assertFalse("isLoading should be false", uiState.isLoading)
        assertEquals("Session expired. Please login again.", uiState.error)

        // VERIFY: Pastikan repository tidak pernah dipanggil jika token tidak ada
        verify(mockCoralRepository, never()).getSingleCoral(testCoralId, "")
    }

    @Test
    fun `onLocationSelected - LiveData lokasi harus terupdate`() {
        // GIVEN
        val testLocationId = 101
        val testLocationName = "Gili Air"

        // WHEN
        viewModel.onLocationSelected(testLocationId, testLocationName)

        // THEN
        val locationId = viewModel.selectedLocationId.getOrAwaitValue()
        val locationName = viewModel.selectedLocationName.getOrAwaitValue()

        assertEquals(testLocationId, locationId)
        assertEquals(testLocationName, locationName)

        // Button seharusnya masih disabled karena koral belum di-load
        val isButtonEnabled = viewModel.isNextButtonEnabled.getOrAwaitValue()
        assertFalse("Next button should be disabled if coral is not loaded", isButtonEnabled)
    }

    @Test
    fun `isNextButtonEnabled - harus true HANYA jika koral di-load DAN lokasi dipilih`() = runTest {
        // --- TAHAP 1: KONDISI AWAL ---
        assertFalse("Button should be disabled initially", viewModel.isNextButtonEnabled.getOrAwaitValue())

        // --- TAHAP 2: LOAD KORAL (SUKSES) ---
        val testCoralId = 1
        val testToken = "valid_token"
        val mockCoral = Coral(id_tk = testCoralId, tk_name = "Acropora", tk_jenis = "Staghorn", harga_tk = 150000, stok_tersedia = 10, description = "A beautiful coral", is_deleted = false, img_path = null, public_id = null)
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(testToken)
        whenever(mockCoralRepository.getSingleCoral(testCoralId, testToken)).thenReturn(Result.success(mockCoral))

        viewModel.loadCoralDetails(testCoralId)
        testDispatcher.scheduler.runCurrent()

        // Tombol seharusnya masih false
        assertFalse("Button should still be disabled after only loading coral", viewModel.isNextButtonEnabled.getOrAwaitValue())

        // --- TAHAP 3: PILIH LOKASI ---
        viewModel.onLocationSelected(101, "Gili Air")

        // Tombol seharusnya menjadi true
        assertTrue("Button should be enabled after loading coral AND selecting location", viewModel.isNextButtonEnabled.getOrAwaitValue())
    }
}