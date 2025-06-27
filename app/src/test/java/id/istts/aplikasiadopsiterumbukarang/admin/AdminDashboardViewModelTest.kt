package id.istts.aplikasiadopsiterumbukarang.admin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard.AdminDashboardViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class AdminDashboardViewModelTest {

    // Aturan untuk eksekusi komponen Arsitektur secara sinkron
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Dispatcher khusus untuk testing coroutines
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mocks untuk dependensi ViewModel
    private lateinit var mockCoralRepository: CoralRepository
    private lateinit var mockSessionManager: SessionManager

    // Kelas yang sedang diuji
    private lateinit var viewModel: AdminDashboardViewModel

    // Data palsu untuk digunakan dalam test
    private val fakeCoralList = listOf(
        Coral(1, "Acropora", "Staghorn", 50000, 20, "A beautiful coral", false, null, null),
        Coral(2, "Montipora", "Plating", 75000, 5, "Low stock coral", false, null, null), // Stok rendah
        Coral(3, "Pocillopora", "Cauliflower", 60000, 15, "Another coral", false, null, null)
    )

    @Before
    fun setUp() {
        // Mengganti dispatcher Main dengan test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Inisialisasi mock
        mockCoralRepository = mock()
        mockSessionManager = mock()

        // Inisialisasi ViewModel dengan mock dependensi
        // Karena init block memanggil fungsi, kita perlu setup mock SEBELUM inisialisasi
        whenever(mockSessionManager.isLoggedIn()).thenReturn(true)
        whenever(mockSessionManager.fetchUserStatus()).thenReturn("admin")
        whenever(mockSessionManager.fetchAuthToken()).thenReturn("fake-token")
        // Kita buat `getCoralList` mengembalikan success by default untuk init
        runTest {
            whenever(mockCoralRepository.getCoralList(any())).thenReturn(Result.success(fakeCoralList))
        }
        viewModel = AdminDashboardViewModel(mockCoralRepository, mockSessionManager)
    }

    @After
    fun tearDown() {
        // Membersihkan dispatcher setelah test selesai
        Dispatchers.resetMain()
    }

    @Test
    fun `init - user is not admin - should navigate to login`() = runTest {
        // GIVEN: Session manager mengindikasikan user bukan admin
        whenever(mockSessionManager.isLoggedIn()).thenReturn(true)
        whenever(mockSessionManager.fetchUserStatus()).thenReturn("user")

        // WHEN: ViewModel diinisialisasi
        val newViewModel = AdminDashboardViewModel(mockCoralRepository, mockSessionManager)

        // THEN: Harus memicu navigasi ke login
        assertTrue(newViewModel.shouldNavigateToLogin.value)
    }

    @Test
    fun `loadCoralData - success - should update uiState with coral list and correct stats`() = runTest {
        // GIVEN: Repository akan mengembalikan data sukses (sudah di-setup di @Before)

        // WHEN: Data di-load (sudah terjadi di init, kita panggil lagi untuk memastikan)
        viewModel.loadCoralData()

        // THEN: Pastikan uiState diupdate dengan benar
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals(3, uiState.coralList.size)
        assertEquals(3, uiState.totalCorals)
        assertEquals(1, uiState.lowStockCount) // Hanya Montipora yang stoknya < 10
        assertEquals("MY CORAL'S SEEDS COLLECTION (3)", uiState.collectionTitle)
    }

    @Test
    fun `loadCoralData - repository returns expired token error - should navigate to login`() = runTest {
        // GIVEN: Repository mengembalikan error token expired
        val errorMessage = "Invalid or Expired Token"
        whenever(mockCoralRepository.getCoralList(any())).thenReturn(Result.failure(Exception(errorMessage)))

        // WHEN: Data di-load
        viewModel.loadCoralData()

        // THEN: Harus memicu navigasi ke login
        assertTrue(viewModel.shouldNavigateToLogin.value)
        assertEquals("Session expired. Please login again.", viewModel.uiState.value.error)
    }

    @Test
    fun `confirmDeleteCoral - success - should show message and refresh data`() = runTest {
        // GIVEN: Coral untuk dihapus sudah dipilih, dan repository akan sukses
        val coralToDelete = fakeCoralList.first()
        val refreshedList = fakeCoralList.drop(1)
        whenever(mockCoralRepository.deleteCoral(eq(coralToDelete.id_tk), any())).thenReturn(Result.success("Deleted"))
        // Mocking untuk refresh data setelah delete
        whenever(mockCoralRepository.getCoralList(any())).thenReturn(Result.success(refreshedList))

        // WHEN: Panggil fungsi delete
        viewModel.onCoralDeleteClick(coralToDelete) // Pilih coral untuk dihapus
        viewModel.confirmDeleteCoral()

        // THEN:
        // 1. Verifikasi `deleteCoral` dipanggil di repository
        verify(mockCoralRepository).deleteCoral(eq(coralToDelete.id_tk), any())
        // 2. Verifikasi pesan sukses muncul
        assertTrue(viewModel.showMessage.value?.contains("deleted successfully") ?: false)
        // 3. Verifikasi data di-refresh (list sekarang berisi 2 coral)
        assertEquals(2, viewModel.uiState.value.coralList.size)
    }

    @Test
    fun `confirmDeleteCoral - failure - should show error message`() = runTest {
        // GIVEN: Coral untuk dihapus sudah dipilih, dan repository akan gagal
        val coralToDelete = fakeCoralList.first()
        val errorMessage = "Cannot delete item"
        whenever(mockCoralRepository.deleteCoral(any(), any())).thenReturn(Result.failure(Exception(errorMessage)))

        // WHEN: Panggil fungsi delete
        viewModel.onCoralDeleteClick(coralToDelete)
        viewModel.confirmDeleteCoral()

        // THEN:
        // 1. Verifikasi `deleteCoral` dipanggil
        verify(mockCoralRepository).deleteCoral(any(), any())
        // 2. Verifikasi pesan error muncul
        assertTrue(viewModel.showMessage.value?.contains(errorMessage) ?: false)
    }

    @Test
    fun `onLogoutClick - should clear session and navigate to login`() = runTest {
        // WHEN
        viewModel.onLogoutClick()

        // THEN
        verify(mockSessionManager).clearSession()
        assertTrue(viewModel.shouldNavigateToLogin.value)
    }
}