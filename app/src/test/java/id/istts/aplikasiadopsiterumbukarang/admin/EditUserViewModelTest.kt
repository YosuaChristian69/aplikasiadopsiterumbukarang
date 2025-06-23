package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editUser

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.SingleUserResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.UpdateUserResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.*
import retrofit2.Response
import java.util.Date

@ExperimentalCoroutinesApi
class EditUserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    // Dependensi yang akan di-mock
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager

    // Variabel untuk mengontrol mock statis
    private lateinit var mockedLog: MockedStatic<Log>

    // Kelas yang sedang diuji
    private lateinit var viewModel: EditUserViewModel

    // Data palsu untuk digunakan ulang
    private val fakeWorker = Worker("1", "Budi", "budi@email.com", "worker", "100000", "active", Date(), "", "")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

       mockedLog = Mockito.mockStatic(Log::class.java)

        mockApiService = mock()
        mockSessionManager = mock()
        viewModel = EditUserViewModel(mockApiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()

        mockedLog.close()
    }

    @Test
    fun `fetchUserById - success - should update user LiveData`() = runTest {
        val userId = "1"
        val token = "fake-token"
        val successResponse = Response.success(SingleUserResponse("Success", fakeWorker))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.fetchUserById(userId, token)).thenReturn(successResponse)

        viewModel.fetchUserById(userId, mockSessionManager)

        verify(mockApiService).fetchUserById(userId, token)
        assertFalse(viewModel.isLoading.value ?: true)
        assertNull(viewModel.error.value)
        assertEquals(fakeWorker, viewModel.user.value)
    }

    @Test
    fun `fetchUserById - API error (404 Not Found) - should set error LiveData`() = runTest {
        val userId = "1"
        val token = "fake-token"
        val errorResponse = Response.error<SingleUserResponse>(404, "Not Found".toResponseBody("text/plain".toMediaTypeOrNull()))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.fetchUserById(userId, token)).thenReturn(errorResponse)

        viewModel.fetchUserById(userId, mockSessionManager)

        assertFalse(viewModel.isLoading.value ?: true)
        assertNull(viewModel.user.value)
        assertEquals("User not found", viewModel.error.value)
    }

    @Test
    fun `fetchUserById - no auth token - should set error and not call API`() = runTest {
        // GIVEN
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(null)

        // WHEN
        viewModel.fetchUserById("1", mockSessionManager)

        // THEN
        verify(mockApiService, never()).fetchUserById(any(), any())
        assertEquals("Authentication token not found", viewModel.error.value)
    }
    @Test
    fun `updateUser - success - should set success flag and update user`() = runTest {
        // GIVEN
        val userId = "1"
        val token = "fake-token"
        val updateData = mapOf("full_name" to "Budi Santoso")
        val updatedWorker = fakeWorker.copy(full_name = "Budi Santoso")
        val successResponse = Response.success(UpdateUserResponse("Updated", updatedWorker))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.updateUserById(userId, token, updateData)).thenReturn(successResponse)

        // WHEN
        viewModel.updateUser(userId, updateData, mockSessionManager)

        // THEN
        verify(mockApiService).updateUserById(userId, token, updateData)
        assertFalse(viewModel.isLoading.value ?: true)
        assertTrue(viewModel.updateSuccess.value ?: false)
        assertEquals(updatedWorker, viewModel.user.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `updateUser - local validation fails (invalid email) - should set error and not call API`() = runTest {
        // GIVEN
        val userId = "1"
        val token = "fake-token"
        val updateData = mapOf("email" to "budi-invalid-email")
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token) // Token still needed for the initial check

        // WHEN
        viewModel.updateUser(userId, updateData, mockSessionManager)

        // THEN
        verify(mockApiService, never()).updateUserById(any(), any(), any())
        assertFalse(viewModel.isLoading.value ?: true)
        assertFalse(viewModel.updateSuccess.value ?: false)
        assertEquals("Invalid email format", viewModel.error.value)
    }

    @Test
    fun `updateUser - API error (409 Conflict) - should set error LiveData`() = runTest {
        // GIVEN
        val userId = "1"
        val token = "fake-token"
        val updateData = mapOf("email" to "existing@email.com")
        val errorResponse = Response.error<UpdateUserResponse>(409, "Conflict".toResponseBody("text/plain".toMediaTypeOrNull()))
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.updateUserById(userId, token, updateData)).thenReturn(errorResponse)

        // WHEN
        viewModel.updateUser(userId, updateData, mockSessionManager)

        // THEN
        assertFalse(viewModel.isLoading.value ?: true)
        assertFalse(viewModel.updateSuccess.value ?: false)
        assertEquals("Email already exists", viewModel.error.value)
    }

    @Test
    fun `updateUser - network exception - should set error LiveData`() = runTest {
        // GIVEN
        val userId = "1"
        val token = "fake-token"
        val updateData = mapOf("full_name" to "Budi")
        val exception = RuntimeException("No internet")
        whenever(mockSessionManager.fetchAuthToken()).thenReturn(token)
        whenever(mockApiService.updateUserById(any(), any(), any())).thenThrow(exception)

        // WHEN
        viewModel.updateUser(userId, updateData, mockSessionManager)

        // THEN
        assertFalse(viewModel.isLoading.value ?: true)
        assertFalse(viewModel.updateSuccess.value ?: false)
        assertEquals("Network error: No internet", viewModel.error.value)
    }
    //endregion
}