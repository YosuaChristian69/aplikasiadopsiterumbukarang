package id.istts.aplikasiadopsiterumbukarang.admin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
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
class EditCoralViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockRepository: CoralRepository
    private lateinit var viewModel: EditCoralViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        viewModel = EditCoralViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val fakeCoralId = 1
    private val fakeToken = "valid-token"
    private val fakeCoral = Coral(
        id_tk = fakeCoralId,
        tk_name = "Coral Test",
        tk_jenis = "Acropora",
        harga_tk = 100000,
        stok_tersedia = 10,
        description = "Deskripsi awal",
        img_path = "http://example.com/image.jpg",
        is_deleted = false,
        public_id = "test_public_id_123"
    )

    // --- Tes untuk setInitialCoralData (Pengganti fetchCoralData) ---

    @Test
    fun `setInitialCoralData - SUKSES akan mengisi LiveData coral`() {
        // Aksi: Panggil fungsi baru untuk mengisi data awal
        viewModel.setInitialCoralData(fakeCoral)

        // Verifikasi: Pastikan LiveData coral berisi data yang benar
        val coralValue = viewModel.coral.value
        assertEquals(fakeCoral, coralValue)
    }


    // --- Tes untuk validasi (Tidak ada perubahan, sudah benar) ---

    @Test
    fun `validateFields - SUKSES jika semua input valid`() {
        val (isValid, errors) = viewModel.validateFields(
            name = "Coral Baru",
            species = "Staghorn",
            price = "150000",
            stock = "20"
        )

        assertTrue(isValid)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validateFields - GAGAL jika nama kosong`() {
        val (isValid, errors) = viewModel.validateFields(
            name = " ", // Nama sengaja dikosongkan
            species = "Staghorn",
            price = "150000",
            stock = "20"
        )

        assertFalse(isValid)
        assertTrue(errors.containsKey("name"))
        assertEquals("Coral name is required", errors["name"])
    }

    @Test
    fun `validateFields - GAGAL jika harga bukan angka`() {
        // Aksi: Lakukan validasi dengan harga yang tidak valid.
        val (isValid, errors) = viewModel.validateFields(
            name = "Coral Bagus",
            species = "Staghorn",
            price = "bukan_angka",
            stock = "20"
        )
        assertFalse(isValid)
        assertTrue(errors.containsKey("price"))
        assertEquals("Price must be a valid number", errors["price"])
    }


    // --- Tes untuk updateCoral (Tidak ada perubahan, sudah benar) ---

    @Test
    fun `updateCoral - SUKSES akan set updateSuccess menjadi true`() = runTest {
        val editRequest = EditCoralRequest("Nama Baru", "Jenis Baru", 120000, 15, "Deskripsi baru")
        whenever(mockRepository.editCoral(fakeCoralId, fakeToken, editRequest))
            .thenReturn(Result.success("Coral updated successfully"))

        // Panggil fungsi update
        viewModel.updateCoral(fakeCoralId, fakeToken, editRequest)

        // Verifikasi
        verify(mockRepository).editCoral(fakeCoralId, fakeToken, editRequest)
        assertTrue(viewModel.updateSuccess.value ?: false)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value ?: true)
    }

    @Test
    fun `updateCoral - GAGAL akan mengisi LiveData errorMessage`() = runTest {
        val errorMessage = "Failed to update coral"
        val editRequest = EditCoralRequest("Nama Baru", "Jenis Baru", 120000, 15, "Deskripsi baru")
        whenever(mockRepository.editCoral(any(), any(), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // Panggil fungsi update
        viewModel.updateCoral(fakeCoralId, fakeToken, editRequest)

        // Verifikasi
        assertEquals(errorMessage, viewModel.errorMessage.value)
        assertNull(viewModel.updateSuccess.value)
        assertFalse(viewModel.isLoading.value ?: true)
    }

    // CATATAN: Tes `fetchCoralData - GAGAL` dihapus karena sudah tidak relevan.
    // ViewModel tidak lagi melakukan fetch, jadi tidak ada skenario kegagalan fetch di dalam ViewModel.
}