package id.istts.aplikasiadopsiterumbukarang.worker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerProfile
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkerProfileViewModelTest {

    // Aturan ini WAJIB ada untuk testing LiveData.
    // Ia memastikan semua proses LiveData berjalan secara sinkron di thread yang sama.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Deklarasi mock dan ViewModel
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: WorkerProfileViewModel

    @Before
    fun setUp() {
        // Buat mock untuk SessionManager sebelum setiap test
        // relaxed = true berarti jika ada fungsi di mock yang dipanggil tanpa di-setup,
        // ia akan mengembalikan nilai default (null, 0, false) daripada crash.
        mockSessionManager = mockk(relaxed = true)
    }

    @Test
    fun `init - harus memuat profil worker dari session manager dengan benar`() {
        // Arrange (Atur apa yang akan dikembalikan oleh mock)
        every { mockSessionManager.fetchUserName() } returns "Budi Santoso"
        every { mockSessionManager.fetchUserEmail() } returns "budi.s@example.com"
        every { mockSessionManager.fetchUserStatus() } returns "worker"

        // Act (Buat instance ViewModel, yang akan otomatis memanggil init)
        viewModel = WorkerProfileViewModel(mockSessionManager)

        // Assert (Periksa hasilnya)
        val profile = viewModel.workerProfile.value
        assertNotNull(profile)
        assertEquals("Budi Santoso", profile?.name)
        assertEquals("budi.s@example.com", profile?.email)
        assertEquals("Diver", profile?.jobTitle) // Sesuai logika di ViewModel

        // Verifikasi bahwa fungsi-fungsi di mock memang dipanggil
        verify(exactly = 1) { mockSessionManager.fetchUserName() }
        verify(exactly = 1) { mockSessionManager.fetchUserEmail() }
        verify(exactly = 1) { mockSessionManager.fetchUserStatus() }
    }

    @Test
    fun `toggleEditMode - harus mengubah nilai isEditMode`() {
        // Arrange
        // Inisialisasi ViewModel terlebih dahulu
        viewModel = WorkerProfileViewModel(mockSessionManager)
        // Pastikan nilai awal adalah null atau false
        assertNull(viewModel.isEditMode.value)

        // Act & Assert 1
        viewModel.toggleEditMode()
        assertEquals(true, viewModel.isEditMode.value)

        // Act & Assert 2
        viewModel.toggleEditMode()
        assertEquals(false, viewModel.isEditMode.value)
    }

    @Test
    fun `updateProfile - saat nama berubah - harus update LiveData dan panggil saveUserDetails`() {
        // Arrange
        // Setup kondisi awal
        every { mockSessionManager.fetchUserName() } returns "Nama Lama"
        every { mockSessionManager.fetchUserEmail() } returns "email@lama.com"
        viewModel = WorkerProfileViewModel(mockSessionManager)

        // Buat data profil yang sudah diupdate
        val updatedProfile = viewModel.workerProfile.value!!.copy(name = "Nama Baru")

        // Act
        viewModel.updateProfile(updatedProfile)

        // Assert
        assertEquals("Nama Baru", viewModel.workerProfile.value?.name)
        assertEquals(false, viewModel.isEditMode.value)

        // Verifikasi bahwa saveUserDetails DIPANGGIL karena nama berubah
        verify(exactly = 1) {
            mockSessionManager.saveUserDetails("Nama Baru", "email@lama.com", any())
        }
    }

    @Test
    fun `updateProfile - saat tidak ada data berubah - TIDAK boleh panggil saveUserDetails`() {
        // Arrange
        val initialProfile = WorkerProfile(
            name = "Nama Sama",
            email = "email@sama.com",
            jobTitle = "Diver",
            dateOfBirth = "", phone = "", joinedDate = "", workerId = ""
        )
        every { mockSessionManager.fetchUserName() } returns initialProfile.name
        every { mockSessionManager.fetchUserEmail() } returns initialProfile.email
        viewModel = WorkerProfileViewModel(mockSessionManager)

        // Buat data update yang sama persis
        val updatedProfile = initialProfile.copy(phone = "08111") // Ubah data lain, bukan nama/email

        // Act
        viewModel.updateProfile(updatedProfile)

        // Assert
        assertEquals("08111", viewModel.workerProfile.value?.phone)

        // Verifikasi bahwa saveUserDetails TIDAK PERNAH DIPANGGIL
        verify(exactly = 0) { mockSessionManager.saveUserDetails(any(), any(), any()) }
    }

    @Test
    fun `isUserLoggedIn - harus mengembalikan nilai dari session manager`() {
        // Arrange
        every { mockSessionManager.isLoggedIn() } returns true
        viewModel = WorkerProfileViewModel(mockSessionManager)

        // Act & Assert
        assertTrue(viewModel.isUserLoggedIn())

        // Arrange 2
        every { mockSessionManager.isLoggedIn() } returns false

        // Act & Assert 2
        assertFalse(viewModel.isUserLoggedIn())
    }
}