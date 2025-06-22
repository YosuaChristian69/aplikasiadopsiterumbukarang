    package id.istts.aplikasiadopsiterumbukarang.usecases

    import android.net.Uri
    import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
    import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
    import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
    import kotlinx.coroutines.ExperimentalCoroutinesApi
    import kotlinx.coroutines.test.runTest
    import okhttp3.MultipartBody
    import okhttp3.RequestBody
    import org.junit.Assert.assertEquals
    import org.junit.Assert.assertTrue
    import org.junit.Before
    import org.junit.Test
    import org.mockito.kotlin.any
    import org.mockito.kotlin.mock
    import org.mockito.kotlin.verify
    import org.mockito.kotlin.whenever
    import java.io.File

    @ExperimentalCoroutinesApi
    class AddCoralUseCaseTest {

        // Mocks for dependencies
        private lateinit var mockCoralRepository: CoralRepository // Use interface for mocking
        private lateinit var mockFileUtils: FileUtils

        // The class we are testing
        private lateinit var addCoralUseCase: AddCoralUseCase

        // Mock data
        private val mockUri: Uri = mock()
        private val mockFile: File = mock()
        private val mockParams = AddCoralUseCase.AddCoralParams(
            token = "test_token",
            name = "Acropora",
            jenis = "Staghorn",
            harga = "150000",
            stok = "20",
            description = "A beautiful branching coral.",
            imageUri = mockUri
        )

        @Before
        fun setUp() {
            mockCoralRepository = mock()
            mockFileUtils = mock()
            addCoralUseCase = AddCoralUseCase(mockCoralRepository, mockFileUtils)
        }

        @Test
        fun `execute on success returns success result`() = runTest {
            // Given
            val successMessage = "Coral added"
            // Mock FileUtils to return a valid file from URI
            whenever(mockFileUtils.getFileFromUri(mockUri)).thenReturn(mockFile)
            // Mock repository to return success
            whenever(
                mockCoralRepository.addCoral(
                    any(), any(), any(), any(), any(), any(), any()
                )
            ).thenReturn(Result.success(successMessage))

            // When
            val result = addCoralUseCase.execute(mockParams)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(successMessage, result.getOrNull())
            // Verify that repository method was called
            verify(mockCoralRepository).addCoral(
                token = any(),
                name = any<RequestBody>(),
                jenis = any<RequestBody>(),
                harga = any<RequestBody>(),
                stok = any<RequestBody>(),
                description = any<RequestBody>(),
                profilePicture = any<MultipartBody.Part>()
            )
        }

        @Test
        fun `execute when file utils throws exception returns failure`() = runTest {
            // Given
            val exceptionMessage = "File not found"
            // Mock FileUtils to throw an error
            whenever(mockFileUtils.getFileFromUri(mockUri)).thenThrow(RuntimeException(exceptionMessage))

            // When
            val result = addCoralUseCase.execute(mockParams)

            // Then
            assertTrue(result.isFailure)
            assertEquals(exceptionMessage, result.exceptionOrNull()?.message)
        }

        @Test
        fun `execute when repository fails returns failure`() = runTest {
            // Given
            val exceptionMessage = "API Error"
            // Mock FileUtils to return a valid file
            whenever(mockFileUtils.getFileFromUri(mockUri)).thenReturn(mockFile)
            // Mock repository to return failure
            whenever(
                mockCoralRepository.addCoral(any(), any(), any(), any(), any(), any(), any())
            ).thenReturn(Result.failure(Exception(exceptionMessage)))

            // When
            val result = addCoralUseCase.execute(mockParams)

            // Then
            assertTrue(result.isFailure)
            assertEquals(exceptionMessage, result.exceptionOrNull()?.message)
        }
    }