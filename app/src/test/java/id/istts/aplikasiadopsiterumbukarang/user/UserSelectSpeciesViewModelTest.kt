package id.istts.aplikasiadopsiterumbukarang.user

import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.presentation.SelectionMode
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.UserSelectSpeciesViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.MainDispatcherRule
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UserSelectSpeciesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var mockCoralRepository: CoralRepository
    private lateinit var mockSessionManager: SessionManager

    private lateinit var viewModel: UserSelectSpeciesViewModel
    private val coralInStock = Coral(id_tk = 1, tk_name = "Acropora In Stock", tk_jenis = "Staghorn", harga_tk = 150000, stok_tersedia = 10, description = "", is_deleted = false, null, null)
    private val coralOutOfStock = Coral(id_tk = 2, tk_name = "Montipora Out of Stock", tk_jenis = "Plating", harga_tk = 200000, stok_tersedia = 0, description = "", is_deleted = false, null, null)
    private val anotherCoralInStock = Coral(id_tk = 3, tk_name = "Favia In Stock", tk_jenis = "Brain", harga_tk = 180000, stok_tersedia = 5, description = "", is_deleted = false, null, null)
    private val allCoralsMock = listOf(coralInStock, coralOutOfStock, anotherCoralInStock)

    @Before
    fun setUp() {
        mockCoralRepository = mock()
        mockSessionManager = mock()
    }

    @Test
    fun `loadCorals - mode USER - hanya menampilkan koral yang stoknya tersedia`() = runTest {
        // GIVEN
        whenever(mockSessionManager.fetchAuthToken()).thenReturn("valid_token")
        whenever(mockCoralRepository.getCoralList(any())).thenReturn(Result.success(allCoralsMock))

        // WHEN: ViewModel dibuat dengan mode USER
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.USER_SINGLE_SELECTION)
        testDispatcher.scheduler.runCurrent() // Jalankan coroutine dari init

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Harus ada 2 koral yang tersedia", 2, state.availableCorals.size)
        assertTrue("Koral yang stoknya habis tidak boleh ada", state.availableCorals.none { it.stok_tersedia == 0 })
        assertTrue("Koral 1 (in stock) harus ada", state.availableCorals.contains(coralInStock))
        assertTrue("Koral 3 (in stock) harus ada", state.availableCorals.contains(anotherCoralInStock))
    }

    @Test
    fun `loadCorals - mode ADMIN - menampilkan semua koral`() = runTest {
        // GIVEN
        whenever(mockSessionManager.fetchAuthToken()).thenReturn("valid_token")
        whenever(mockCoralRepository.getCoralList(any())).thenReturn(Result.success(allCoralsMock))

        // WHEN: ViewModel dibuat dengan mode ADMIN
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.ADMIN_MULTI_SELECTION)
        testDispatcher.scheduler.runCurrent()

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Harus ada 3 koral yang tersedia (semua)", 3, state.availableCorals.size)
        assertTrue("Koral yang stoknya habis (koral 2) harus tetap ada", state.availableCorals.contains(coralOutOfStock))
    }

    @Test
    fun `loadCorals - token tidak valid - uiState harus update dengan error`() = runTest {
        // GIVEN
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(null)

        // WHEN
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.USER_SINGLE_SELECTION)
        testDispatcher.scheduler.runCurrent()

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Authentication token not found. Please login.", state.error)
        verify(mockCoralRepository, never()).getCoralList(any())
    }

    @Test
    fun `toggleSelection - mode USER - memilih dan membatalkan pilihan koral`() {
        // GIVEN: Inisialisasi ViewModel dengan mode USER
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.USER_SINGLE_SELECTION)

        // 1. Pilih koral pertama kali
        viewModel.toggleCoralSelection(coralInStock)
        var selected = viewModel.uiState.value.selectedCorals
        assertEquals("Harus ada 1 koral terpilih", 1, selected.size)
        assertEquals(coralInStock, selected.first())

        // 2. Pilih koral yang sama lagi (membatalkan pilihan)
        viewModel.toggleCoralSelection(coralInStock)
        selected = viewModel.uiState.value.selectedCorals
        assertTrue("Seharusnya tidak ada koral yang terpilih", selected.isEmpty())
    }

    @Test
    fun `toggleSelection - mode USER - mengganti pilihan koral`() {
        // GIVEN
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.USER_SINGLE_SELECTION)
        viewModel.toggleCoralSelection(coralInStock) // Pilih koral pertama

        // WHEN: Pilih koral yang berbeda
        viewModel.toggleCoralSelection(anotherCoralInStock)

        // THEN
        val selected = viewModel.uiState.value.selectedCorals
        assertEquals("Harus ada 1 koral terpilih", 1, selected.size)
        assertEquals("Koral yang terpilih harus koral yang baru", anotherCoralInStock, selected.first())
    }

    @Test
    fun `toggleSelection - mode ADMIN - memilih beberapa koral`() {
        // GIVEN
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.ADMIN_MULTI_SELECTION)

        // WHEN: Pilih dua koral
        viewModel.toggleCoralSelection(coralInStock)
        viewModel.toggleCoralSelection(anotherCoralInStock)

        // THEN
        val selected = viewModel.uiState.value.selectedCorals
        assertEquals("Harus ada 2 koral terpilih", 2, selected.size)
        assertTrue(selected.containsAll(listOf(coralInStock, anotherCoralInStock)))
    }

    @Test
    fun `toggleSelection - mode ADMIN - membatalkan salah satu dari beberapa pilihan`() {
        // GIVEN
        viewModel = UserSelectSpeciesViewModel(mockCoralRepository, mockSessionManager, SelectionMode.ADMIN_MULTI_SELECTION)
        viewModel.toggleCoralSelection(coralInStock)
        viewModel.toggleCoralSelection(anotherCoralInStock)

        // WHEN: Batalkan pilihan koral pertama
        viewModel.toggleCoralSelection(coralInStock)

        // THEN
        val selected = viewModel.uiState.value.selectedCorals
        assertEquals("Hanya ada 1 koral yang tersisa", 1, selected.size)
        assertEquals("Koral yang tersisa harus koral kedua", anotherCoralInStock, selected.first())
    }
}