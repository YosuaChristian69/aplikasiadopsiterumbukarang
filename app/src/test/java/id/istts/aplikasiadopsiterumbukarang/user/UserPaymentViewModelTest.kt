package id.istts.aplikasiadopsiterumbukarang.user

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.MidtransTransactionResponse
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.TransactionRepository
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
import org.mockito.kotlin.*
import java.util.concurrent.TimeoutException
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserPaymentViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.getOrAwaitValue
import org.junit.After
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class UserPaymentViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    // Mocks
    private lateinit var mockCoralRepository: CoralRepository
    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockedLog: MockedStatic<Log>
    // ViewModel
    private lateinit var viewModel: UserPaymentViewModel

    @Before
    fun setUp() {
        mockedLog = Mockito.mockStatic(Log::class.java)
        whenever(Log.d(anyString(), anyString())).thenReturn(0)

        mockCoralRepository = mock()
        mockTransactionRepository = mock()
        mockSessionManager = mock()
        viewModel = UserPaymentViewModel(mockCoralRepository, mockTransactionRepository, mockSessionManager)
    }

    @After
    fun tearDown() {
        mockedLog.close()
    }


    @Test
    fun `loadCoralDetails - sukses - uiState harus update dengan data coral`() = runTest {
        // GIVEN
        val testCoralId = 1
        val testToken = "valid_token"
        val mockCoral = Coral(id_tk = testCoralId, tk_name = "Acropora", tk_jenis = "Staghorn", harga_tk = 150000, stok_tersedia = 10, description = "A beautiful coral", is_deleted = false, img_path = null, public_id = null)
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(testToken)
        whenever(mockCoralRepository.getSingleCoral(testCoralId, testToken)).thenReturn(Result.success(mockCoral))

        // WHEN
        viewModel.loadCoralDetails(testCoralId)
        testDispatcher.scheduler.runCurrent() // Jalankan coroutine

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(mockCoral, state.coralDetails)
    }

    @Test
    fun `loadCoralDetails - repository gagal - uiState harus update dengan error`() = runTest {
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
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        assertNull(state.coralDetails)
    }

    @Test
    fun `loadCoralDetails - token tidak valid - uiState harus update dengan error sesi`() = runTest {
        // GIVEN
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(null)

        // WHEN
        viewModel.loadCoralDetails(1)
        testDispatcher.scheduler.runCurrent()

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User token is null or empty.", state.error)
        verify(mockCoralRepository, never()).getSingleCoral(any(), any())
    }

     @Test
    fun `initiatePayment - sukses - midtransResponse LiveData harus terupdate`() = runTest {
        // GIVEN: First, successfully load a coral into the state
        `loadCoralDetails - sukses - uiState harus update dengan data coral`()

        val mockMidtransResponse = MidtransTransactionResponse(token = "snap_token_123", redirectUrl = "https://app.midtrans.com/snap/v1/...")
        // Gunakan `any()` untuk request karena kita tidak perlu memvalidasi isi request di tes ini
        whenever(mockTransactionRepository.createAdoptionTransaction(any())).thenReturn(mockMidtransResponse)

        // WHEN
        viewModel.initiatePayment(locationId = 1, nickname = "MyCoral", message = "Hello")
        testDispatcher.scheduler.runCurrent()

        // THEN
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(mockMidtransResponse, viewModel.midtransResponse.getOrAwaitValue())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initiatePayment - repository gagal - uiState harus update dengan error`() = runTest {
        // GIVEN: First, load a coral
        `loadCoralDetails - sukses - uiState harus update dengan data coral`()

        val errorMessage = "Midtrans is down"
        whenever(mockTransactionRepository.createAdoptionTransaction(any())).thenThrow(RuntimeException(errorMessage))

        // WHEN
        viewModel.initiatePayment(locationId = 1, nickname = "MyCoral", message = "Hello")
        testDispatcher.scheduler.runCurrent()

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        assertNull(viewModel.midtransResponse.value) // LiveData tidak boleh terupdate
    }

    @Test
    fun `initiatePayment - koral belum di-load - tidak melakukan apa-apa`() = runTest {
        // GIVEN: The initial state where coralDetails is null

        // WHEN
        viewModel.initiatePayment(locationId = 1, nickname = "MyCoral", message = "Hello")
        testDispatcher.scheduler.runCurrent()

        // THEN: Verify that the repository was never called
        verify(mockTransactionRepository, never()).createAdoptionTransaction(any())
        assertFalse(viewModel.uiState.value.isLoading) // Loading tidak boleh aktif
    }

    // --- Tests for manuallyFulfillOrderForDebug ---
    @Test
    fun `manuallyFulfillOrderForDebug - sukses - tidak ada error`() = runTest {
        // GIVEN: mock repository will do nothing on success
        // Mockito's default behavior for suspend functions returning Unit is to do nothing, which is perfect.

        // WHEN
        viewModel.manuallyFulfillOrderForDebug(1, 1, 1, "Debug", "Test")
        testDispatcher.scheduler.runCurrent()

        // THEN
        verify(mockTransactionRepository).fulfillOrderForDebug(1, 1, 1, "Debug", "Test")
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `manuallyFulfillOrderForDebug - repository gagal - uiState harus update dengan error`() = runTest {
        // GIVEN
        val errorMessage = "Debug endpoint failed"
        whenever(mockTransactionRepository.fulfillOrderForDebug(any(), any(), any(), any(), any())).thenThrow(RuntimeException(errorMessage))

        // WHEN
        viewModel.manuallyFulfillOrderForDebug(1, 1, 1, "Debug", "Test")
        testDispatcher.scheduler.runCurrent()

        // THEN
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
    }
}
