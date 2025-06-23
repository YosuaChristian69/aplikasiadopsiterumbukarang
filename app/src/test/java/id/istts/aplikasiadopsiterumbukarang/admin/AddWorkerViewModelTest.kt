package id.istts.aplikasiadopsiterumbukarang.admin

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerResponse
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.AddWorkerRepository
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addWorker.AddWorkerViewModel
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

@ExperimentalCoroutinesApi
class AddWorkerViewModelTest {

    // This rule is required for testing Architecture Components (like LiveData) synchronously.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Use a test dispatcher for coroutines to run them on the test thread.
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mocks for dependencies that the ViewModel needs.
    private lateinit var mockRepository: AddWorkerRepository
    private lateinit var mockContext: Context
    private lateinit var mockUri: Uri

    // The ViewModel instance we are going to test.
    private lateinit var viewModel: AddWorkerViewModel

    @Before
    fun setUp() {
        // Set the main dispatcher to our test dispatcher before each test.
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks and the ViewModel.
        mockRepository = mock()
        mockContext = mock()
        mockUri = mock()
        viewModel = AddWorkerViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after each test to avoid interference.
        Dispatchers.resetMain()
    }

    @Test
    fun `addWorker - with image - SUCCESS - should update LiveData with success result`() = runTest {
        // GIVEN: A valid request and a successful API response.
        val token = "fake-token"
        val request = AddWorkerRequest("John Doe", "john@example.com", "password123")
        val successMessage = "Worker added successfully!"
        val successResponse = Response.success(AddWorkerResponse(msg = successMessage))

        // We tell the mock repository what to return when `addWorker` is called.
        whenever(mockRepository.addWorker(token, request, mockUri, mockContext)).thenReturn(successResponse)

        // WHEN: The addWorker function is called in the ViewModel.
        viewModel.addWorker(token, request, mockUri, mockContext)

        // THEN: We verify the results.
        // 1. The repository's addWorker method should have been called exactly once.
        verify(mockRepository).addWorker(token, request, mockUri, mockContext)

        // 2. The loading state should be false after the operation completes.
        assertEquals(false, viewModel.isLoading.value)

        // 3. The result LiveData should contain a success Result with the correct message.
        val result = viewModel.addWorkerResult.value
        assertTrue(result?.isSuccess ?: false)
        assertEquals(successMessage, result?.getOrNull())
    }

    @Test
    fun `addWorker - without image - SUCCESS - should update LiveData with success result`() = runTest {
        // GIVEN: A valid request without an image and a successful API response.
        val token = "fake-token"
        val request = AddWorkerRequest("Jane Doe", "jane@example.com", "password123")
        val successMessage = "Worker added successfully!"
        val successResponse = Response.success(AddWorkerResponse(msg = successMessage))

        // Setup mock response for the case without an image.
        whenever(mockRepository.addWorker(token, request, null, null)).thenReturn(successResponse)

        // WHEN: The addWorker function is called without an image URI or context.
        viewModel.addWorker(token, request, imageUri = null, context = null)

        // THEN: We verify the results.
        verify(mockRepository).addWorker(token, request, null, null)
        assertEquals(false, viewModel.isLoading.value)
        val result = viewModel.addWorkerResult.value
        assertTrue(result?.isSuccess ?: false)
        assertEquals(successMessage, result?.getOrNull())
    }

    @Test
    fun `addWorker - API Error - should update LiveData with failure result`() = runTest {
        // GIVEN: The API returns a specific error (e.g., email already exists).
        val token = "fake-token"
        val request = AddWorkerRequest("John Doe", "john@example.com", "password123")
        val errorJson = """{"msg":"Email already registered"}"""
        val errorResponseBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<AddWorkerResponse>(400, errorResponseBody)

        // Setup mock to return the error response.
        whenever(mockRepository.addWorker(any(), any(), any(), any())).thenReturn(errorResponse)

        // WHEN: The addWorker function is called.
        viewModel.addWorker(token, request, mockUri, mockContext)

        // THEN: The result LiveData should contain a failure with the parsed error message.
        verify(mockRepository).addWorker(token, request, mockUri, mockContext)
        assertEquals(false, viewModel.isLoading.value)
        val result = viewModel.addWorkerResult.value
        assertTrue(result?.isFailure ?: false)
        assertEquals("Email already registered", result?.exceptionOrNull()?.message)
    }

    @Test
    fun `addWorker - Network Exception - should update LiveData with failure result`() = runTest {
        // GIVEN: The repository throws an exception (e.g., network failure).
        val token = "fake-token"
        val request = AddWorkerRequest("John Doe", "john@example.com", "password123")
        val networkException = RuntimeException("Network connection failed")

        // Setup mock to throw an exception.
        whenever(mockRepository.addWorker(any(), any(), any(), any())).thenThrow(networkException)

        // WHEN: The addWorker function is called.
        viewModel.addWorker(token, request, mockUri, mockContext)

        // THEN: The result LiveData should contain a failure with the exception.
        verify(mockRepository).addWorker(token, request, mockUri, mockContext)
        assertEquals(false, viewModel.isLoading.value)
        val result = viewModel.addWorkerResult.value
        assertTrue(result?.isFailure ?: false)
        assertEquals(networkException.message, result?.exceptionOrNull()?.message)
    }
}