package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCase
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AddCoralViewModelTest {

    // Aturan-aturan ini WAJIB ada untuk testing ViewModel dengan LiveData dan Coroutines
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Untuk komponen Arsitektur Android (seperti LiveData)

    // Setup dispatcher untuk Coroutines
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        // Mengganti dispatcher Main dengan dispatcher test sebelum setiap test
        Dispatchers.setMain(testDispatcher)

        // Inisialisasi mock dan ViewModel
        mockSessionManager = mock()
        mockFileUtils = mock()
        mockAddCoralUseCase = mock()
        viewModel = AddCoralViewModel(mockSessionManager, mockFileUtils, mockAddCoralUseCase)
    }

    @After
    fun tearDown() {
        // Membersihkan dispatcher setelah setiap test selesai
        Dispatchers.resetMain()
    }

    // Komponen palsu (mock) untuk dependensi
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockFileUtils: FileUtils
    private lateinit var mockAddCoralUseCase: AddCoralUseCase
    private lateinit var viewModel: AddCoralViewModel

    // Data palsu untuk testing
    private val mockUri: Uri = mock()

    @Test
    fun `validateInputs - GAGAL jika nama kosong`() {
        // Kondisi: Gambar sudah dipilih, tapi nama kosong
        viewModel.setSelectedImageUri(mockUri)

        // Aksi: Lakukan validasi
        val isValid = viewModel.validateInputs(
            name = "", // Nama sengaja dikosongkan
            type = "Staghorn",
            description = "A beautiful coral",
            total = "10",
            harga = "150000"
        )

        // Hasil: Seharusnya tidak valid dan pesan error nama muncul
        assertFalse(isValid)
        assertNotNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `validateInputs - SUKSES jika semua data terisi`() {
        // Kondisi: Gambar sudah dipilih
        viewModel.setSelectedImageUri(mockUri)

        // Aksi: Lakukan validasi dengan data yang benar
        val isValid = viewModel.validateInputs(
            name = "Acropora",
            type = "Staghorn",
            description = "A beautiful coral",
            total = "10",
            harga = "150000"
        )

        // Hasil: Seharusnya valid dan tidak ada pesan error
        assertTrue(isValid)
        assertNull(viewModel.uiState.value.nameError)
        assertNull(viewModel.uiState.value.typeError)
    }

    // Ganti fungsi tes yang lama dengan yang ini
    @Test
    fun `saveCoral - SUKSES akan menampilkan pesan sukses dan event navigasi`() = runTest {
        // Kondisi:
        val successMessage = "Coral added successfully"
        viewModel.setSelectedImageUri(mockUri)
        whenever(mockSessionManager.fetchAuthToken()).thenReturn("fake_token")
        whenever(mockAddCoralUseCase.execute(any())).thenReturn(Result.success(successMessage))

        val events = mutableListOf<AddCoralEvent?>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.events.toList(events)
        }

        // Aksi:
        viewModel.saveCoral("Acropora", "Staghorn", "Desc", "10", "150000")

        verify(mockAddCoralUseCase).execute(any())

        verify(mockAddCoralUseCase).execute(any())
        assertTrue(events.last() is AddCoralEvent.SuccessAndNavigate)

        job.cancel()
    }

    @Test
    fun `saveCoral - GAGAL akan menampilkan pesan error`() = runTest {
        // Kondisi:
        val errorMessage = "Network Error"
        viewModel.setSelectedImageUri(mockUri) // Gambar ada
        whenever(mockSessionManager.fetchAuthToken()).thenReturn("fake_token") // Token ada
        whenever(mockAddCoralUseCase.execute(any())).thenReturn(Result.failure(Exception(errorMessage))) // UseCase GAGAL

        val events = mutableListOf<AddCoralEvent?>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.events.toList(events)
        }

        // Aksi: Panggil fungsi saveCoral
        viewModel.saveCoral("Acropora", "Staghorn", "Desc", "10", "150000")

        // Hasil:
        // 1. Verifikasi bahwa use case dipanggil
        verify(mockAddCoralUseCase).execute(any())

        // 2. Cek apakah event yang keluar adalah error
        // events akan berisi [null, ShowError]
        assertTrue(events[1] is AddCoralEvent.ShowError)
        assertEquals(errorMessage, (events[1] as AddCoralEvent.ShowError).message)

        job.cancel()
    }
}