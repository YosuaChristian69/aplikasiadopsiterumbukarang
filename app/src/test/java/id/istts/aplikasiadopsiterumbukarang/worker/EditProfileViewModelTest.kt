package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.EditProfileUiState
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.UpdatedUserData
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
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
import java.io.File

@ExperimentalCoroutinesApi
class EditProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Gunakan StandardTestDispatcher untuk kontrol penuh
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepository: UserRepository
    private lateinit var viewModel: EditProfileViewModel

    private val mockUri: Uri = mock()
    private val mockFile: File = mock()
    private val mockToken = "fake_token"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        viewModel = EditProfileViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectImage harusnya memperbarui state selectedImageUri`() {
        viewModel.selectImage(mockUri)
        assertEquals(mockUri, viewModel.selectedImageUri.value)
    }

    @Test
    fun `clearSelectedImage harusnya mengosongkan state selectedImageUri`() {
        viewModel.selectImage(mockUri)
        viewModel.clearSelectedImage()
        assertNull(viewModel.selectedImageUri.value)
    }

    @Test
    fun `editProfile - SUKSES harusnya memperbarui uiState dengan data sukses`() = runTest {
        // Arrange
        val mockUser = UpdatedUserData(
            id_user = 123,
            full_name = "Budi Perkasa",
            email = "budi.p@example.com",
            status = "active",
            balance = 50000.0,
            joined_at = "2025-01-10T10:00:00.000000Z",
            user_status = "verified",
            img_path = "/path/to/new_image.jpg"
        )
        val mockSuccessResponse = EditProfileResponse(
            msg = "Profile updated successfully",
            photo_updated = true,
            user = mockUser
        )
        whenever(mockRepository.editProfile(any(), any(), any(), any()))
            .thenReturn(Result.success(mockSuccessResponse))

        // Act
        viewModel.editProfile(mockToken, "budi.p@example.com", "Budi Perkasa", mockFile)

        // Pastikan semua coroutine yang dijadwalkan selesai
        advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.isSuccess)
        assertEquals("Profile updated successfully", finalState.successMessage)
        assertEquals(mockUser, finalState.updatedUser)
        assertTrue(finalState.photoUpdated)
        assertNull(finalState.error)

        verify(mockRepository).editProfile(mockToken, "budi.p@example.com", "Budi Perkasa", mockFile)
    }

    

    @Test
    fun `clearState harusnya mereset uiState dan selectedImageUri ke kondisi awal`() = runTest {
        // Arrange
        viewModel.selectImage(mockUri)
        val exception = Exception("Some Error")
        whenever(mockRepository.editProfile(any(), any(), any(), any()))
            .thenReturn(Result.failure(exception))
        viewModel.editProfile(mockToken, "email", "name")
        advanceUntilIdle()

        // Act
        viewModel.clearState()

        // Assert
        val finalState = viewModel.uiState.value
        val expectedInitialState = EditProfileUiState()

        assertNull("selectedImageUri harusnya null setelah clearState", viewModel.selectedImageUri.value)
        assertEquals("uiState harusnya kembali ke kondisi awal", expectedInitialState, finalState)
    }
}