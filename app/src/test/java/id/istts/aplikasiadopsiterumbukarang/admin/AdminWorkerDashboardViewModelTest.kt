package id.istts.aplikasiadopsiterumbukarang.admin
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.domain.models.WorkerResponse
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.workerdashboard.AdminWorkerDashboardViewModel
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.*
import retrofit2.Response
import java.util.*

@ExperimentalCoroutinesApi
class AdminWorkerDashboardViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    // Mocks
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockedLog: MockedStatic<Log>

    // Class under test
    private lateinit var viewModel: AdminWorkerDashboardViewModel

    // Test data
    private val fakeUserList = listOf(
        Worker("1", "Budi Worker", "budi@worker.com", "worker", "100", "active", Date(), "", ""), // Worker Aktif
        Worker("2", "Citra Worker", "citra@worker.com", "worker", "200", "inactive", Date(), "", ""), // Worker Inaktif
        Worker("3", "Dedi Admin", "dedi@admin.com", "admin", "0", "admin", Date(), "", ""), // Admin (Harus difilter)
        Worker("4", "Eka User", "eka@user.com", "user", "0", "user", Date(), "", ""), // User biasa (Harus difilter)
        Worker("5", "Fafa Worker", "fafa@worker.com", "worker", "300", "aktif", Date(), "", "") // Worker Aktif (case "aktif")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockedLog = Mockito.mockStatic(Log::class.java)
        mockApiService = mock()
        mockSessionManager = mock()
        viewModel = AdminWorkerDashboardViewModel(mockApiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedLog.close()
    }

    @Test
    fun `loadWorkers - success - should filter out admins and users, and update stats correctly`() = runTest {
        // GIVEN
        val token = "fake-token"
        val successResponse = Response.success(WorkerResponse("Success", fakeUserList))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.fetchAllUsers(token)).thenReturn(successResponse)

        // WHEN
        viewModel.loadWorkers(mockSessionManager)

        // THEN
        verify(mockApiService).fetchAllUsers(token)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)

        // 1. Verifikasi list workers (hanya 3 dari 5 yang merupakan worker)
        assertEquals(3, viewModel.workers.value?.size)
        assertTrue(viewModel.workers.value?.any { it.full_name == "Budi Worker" } ?: false)
        assertFalse(viewModel.workers.value?.any { it.full_name == "Dedi Admin" } ?: true)

        // 2. Verifikasi statistik
        assertEquals(3, viewModel.totalWorkersCount.value)
        assertEquals(2, viewModel.activeWorkersCount.value) // Budi dan Fafa
    }

    @Test
    fun `loadWorkers - API error - should set error and clear data`() = runTest {
        // GIVEN
        val token = "fake-token"
        val errorResponse = Response.error<WorkerResponse>(500, mock())
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.fetchAllUsers(token)).thenReturn(errorResponse)

        // WHEN
        viewModel.loadWorkers(mockSessionManager)

        // THEN
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.workers.value?.isEmpty() ?: false)
        assertEquals(0, viewModel.totalWorkersCount.value)
        assertEquals(0, viewModel.activeWorkersCount.value)
    }

    @Test
    fun `searchWorkers - with query - should filter from original list without calling API`() = runTest {
        // GIVEN: Anggap loadWorkers sudah berhasil dan mengisi data
        val token = "fake-token"
        val successResponse = Response.success(WorkerResponse("Success", fakeUserList))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.fetchAllUsers(token)).thenReturn(successResponse)
        viewModel.loadWorkers(mockSessionManager)

        // WHEN: Lakukan pencarian
        viewModel.searchWorkers("budi")

        // THEN
        // 1. API fetchAllUsers hanya dipanggil sekali (saat load awal)
        verify(mockApiService, times(1)).fetchAllUsers(any())

        // 2. Hasil filter dan statistik harus benar
        assertEquals(1, viewModel.workers.value?.size)
        assertEquals("Budi Worker", viewModel.workers.value?.first()?.full_name)
        assertEquals(1, viewModel.totalWorkersCount.value)
        assertEquals(1, viewModel.activeWorkersCount.value)
    }

    @Test
    fun `searchWorkers - with blank query - should restore original list`() = runTest {
        // GIVEN: Anggap sudah load dan sudah difilter
        val token = "fake-token"
        val successResponse = Response.success(WorkerResponse("Success", fakeUserList))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.fetchAllUsers(token)).thenReturn(successResponse)
        viewModel.loadWorkers(mockSessionManager)
        viewModel.searchWorkers("budi") // Kondisi sudah terfilter

        // WHEN: Hapus query pencarian
        viewModel.searchWorkers("  ") // Query kosong/spasi

        // THEN
        // 1. Data kembali seperti semula
        assertEquals(3, viewModel.workers.value?.size)
        assertEquals(3, viewModel.totalWorkersCount.value)
        assertEquals(2, viewModel.activeWorkersCount.value)
    }
}