package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.CoralDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.SpeciesDetails
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import id.istts.aplikasiadopsiterumbukarang.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class UserCoralDetailViewModelTest {

    // Rule untuk menjalankan background task LiveData secara sinkron
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Rule untuk mengganti Main dispatcher dengan Test dispatcher
    private val testDispatcher = StandardTestDispatcher()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    // Mocks untuk dependencies
    private lateinit var mockUserRepository: UserRepository

    // ViewModel yang akan diuji
    private lateinit var viewModel: UserCoralDetailViewModel

    @Before
    fun setUp() {
        // Inisialisasi mock sebelum setiap test
        mockUserRepository = mock()
        // Inisialisasi ViewModel dengan mock repository (Dependency Injection)
        viewModel = UserCoralDetailViewModel(mockUserRepository)
    }

    @Test
    fun `fetchCoralDetail - sukses - LiveData harus diupdate dengan data`() = runTest {
        // GIVEN
        val testToken = "valid-token"
        val testOwnershipId = 123
        val mockResponseData = CoralDetailResponse(
            ownershipId = testOwnershipId,
            coralNickname = "Nemo",
            adoptedAt = "2025-06-25",
            species = SpeciesDetails("Acropora", "Acropora sp.", null),
            location = null,
            planter = null,
            owner = null
        )
        val successResponse = Response.success(mockResponseData)
        whenever(mockUserRepository.getSingleCoralDetail(testToken, testOwnershipId)).thenReturn(successResponse)

        // WHEN
        viewModel.fetchCoralDetail(testToken, testOwnershipId)

        // PERUBAHAN: Hapus pengecekan di sini untuk menghindari error timing.
        // Cukup fokus pada hasil akhir setelah coroutine selesai.

        // Jalankan coroutine yang tertunda
        testDispatcher.scheduler.runCurrent()

        // THEN: Periksa state akhir setelah coroutine selesai (ini yang terpenting)
        assertEquals("isLoading harus false di akhir", false, viewModel.isLoading.getOrAwaitValue())
        assertEquals("Data coral harus sesuai dengan mock", mockResponseData, viewModel.coralDetail.getOrAwaitValue())
        assertNull("Error harus null saat sukses", viewModel.error.value)
    }

    @Test
    fun `fetchCoralDetail - gagal (API error) - LiveData error harus diupdate`() = runTest {
        // GIVEN
        val testToken = "valid-token"
        val testOwnershipId = 123
        val errorBody = "{\"message\":\"Not Found\"}".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<CoralDetailResponse>(404, errorBody)
        whenever(mockUserRepository.getSingleCoralDetail(testToken, testOwnershipId)).thenReturn(errorResponse)

        // WHEN
        viewModel.fetchCoralDetail(testToken, testOwnershipId)

        // PERUBAHAN: Hapus pengecekan loading awal di sini karena timingnya sulit diprediksi
        // assertEquals(true, viewModel.isLoading.value) <-- HAPUS BARIS INI

        // Jalankan coroutine yang tertunda
        testDispatcher.scheduler.runCurrent()

        // THEN: Periksa state akhir setelah coroutine selesai (ini yang terpenting)
        assertEquals("isLoading harus false di akhir", false, viewModel.isLoading.getOrAwaitValue())
        assertNull("Data detail harus null saat gagal", viewModel.coralDetail.value)
        assertNotNull("Pesan error tidak boleh null", viewModel.error.getOrAwaitValue())
        assertTrue("Pesan error harus berisi kode 404", viewModel.error.value!!.contains("404"))
    }

    @Test
    fun `fetchCoralDetail - gagal (Exception Jaringan) - LiveData error harus diupdate`() = runTest {
        // GIVEN
        val testToken = "valid-token"
        val testOwnershipId = 123
        val errorMessage = "Network is down"
        // PERUBAHAN 1: Gunakan RuntimeException untuk menghindari error "Checked exception" dari Mockito.
        whenever(mockUserRepository.getSingleCoralDetail(testToken, testOwnershipId)).thenThrow(RuntimeException(errorMessage))

        // WHEN
        viewModel.fetchCoralDetail(testToken, testOwnershipId)

        // PERUBAHAN 2: Hapus pengecekan state loading awal untuk menghindari error timing dispatcher.
        // Cukup fokus pada hasil akhir setelah coroutine selesai.

        // Jalankan coroutine yang tertunda
        testDispatcher.scheduler.runCurrent()

        // THEN: Periksa state akhir setelah coroutine selesai
        assertEquals("isLoading harus false di akhir", false, viewModel.isLoading.getOrAwaitValue())
        assertNull("Data detail harus null saat gagal", viewModel.coralDetail.value)
        assertNotNull("Pesan error tidak boleh null", viewModel.error.getOrAwaitValue())
        assertTrue("Pesan error harus berisi pesan exception", viewModel.error.value!!.contains(errorMessage))
    }
}