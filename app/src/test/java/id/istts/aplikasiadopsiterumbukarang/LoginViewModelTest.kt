package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.istts.aplikasiadopsiterumbukarang.domain.models.login.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.login.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.LoginRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.* // <-- IMPORT PENTING

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var loginRepository: LoginRepository

    @Mock
    private lateinit var sessionManager: SessionManager

    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginRepository)
        viewModel.initSessionManager(sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `performLogin dengan kredensial admin valid harus menavigasi ke dasbor admin`() = runTest {
        // Arrange
        val email = "admin@coralproject.com"
        val password = "admin123"
        val fakeToken = "dummy.admin_token.string"

        val loginResponse = LoginResponse("success login", fakeToken)
        val loginRequest = LoginRequest(email, password)

        val fakeAdminDetailsJson = mock<JSONObject>()

        whenever(fakeAdminDetailsJson.getString("full_name")).thenReturn("Admin Coral")
        whenever(fakeAdminDetailsJson.getString("email")).thenReturn("admin@coralproject.com")
        whenever(fakeAdminDetailsJson.getString("status")).thenReturn("admin")


        val viewModelSpy = spy(viewModel)

        doReturn(fakeAdminDetailsJson).whenever(viewModelSpy).decodeTokenPayload(any())

        whenever(loginRepository.login(any())).thenReturn(Result.success(loginResponse))

        viewModelSpy.performLogin(email, password)

        verify(loginRepository).login(eq(loginRequest))
        verify(sessionManager).saveAuthToken(fakeToken)
        verify(sessionManager).saveUserDetails(
            "Admin Coral",
            "admin@coralproject.com",
            "admin"
        )

        assert(viewModel.loginState.value == LoginViewModel.LoginState.Success)
        assert(viewModel.successMessage.value == "Login successful")
        assert(viewModel.navigationEvent.value?.target == LoginViewModel.NavigationTarget.ADMIN_DASHBOARD)
        assert(viewModel.isLoading.value == false)
    }

    @Test
    fun `performLogin dengan kredensial tidak valid harus gagal`() = runTest {
        // Arrange
        val email = "wrong@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"
        val loginRequest = LoginRequest(email, password)

        whenever(loginRepository.login(any())).thenReturn(Result.failure(Exception(errorMessage)))

        // Act
        viewModel.performLogin(email, password)

        // Assert
        verify(loginRepository).login(eq(loginRequest))
        verify(sessionManager, never()).saveAuthToken(any())
        verify(sessionManager, never()).saveUserDetails(any(), any(), any())

        assert(viewModel.loginState.value is LoginViewModel.LoginState.Error)
        assert((viewModel.loginState.value as LoginViewModel.LoginState.Error).message == errorMessage)
        assert(viewModel.errorMessage.value == errorMessage)
        assert(viewModel.successMessage.value == null)
        assert(viewModel.isLoading.value == false)
    }

    @Test
    fun `performLogin dengan email kosong harus menampilkan error field`() = runTest {
        // Arrange
        val email = ""
        val password = "password123"

        // Act
        viewModel.performLogin(email, password)

        // Assert
        // Gunakan any() dari mockito-kotlin yang aman
        verify(loginRepository, never()).login(any())

        assert(viewModel.fieldError.value?.field == LoginViewModel.FieldType.EMAIL)
        assert(viewModel.fieldError.value?.message == "Email is required")
        assert(viewModel.isLoading.value == null || viewModel.isLoading.value == false)
    }

    @Test
    fun `performLogin dengan password kosong harus menampilkan error field`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = ""

        // Act
        viewModel.performLogin(email, password)

        // Assert
        verify(loginRepository, never()).login(any())

        assert(viewModel.fieldError.value?.field == LoginViewModel.FieldType.PASSWORD)
        assert(viewModel.fieldError.value?.message == "Password is required")
        assert(viewModel.isLoading.value == null || viewModel.isLoading.value == false)
    }

    @Test
    fun `checkExistingSession dengan sesi admin harus menavigasi ke dasbor admin`() {
        // Arrange
        whenever(sessionManager.isLoggedIn()).thenReturn(true)
        whenever(sessionManager.fetchUserStatus()).thenReturn("admin")

        // Act
        viewModel.checkExistingSession()

        // Assert
        verify(sessionManager).isLoggedIn()
        verify(sessionManager).fetchUserStatus()
        assert(viewModel.navigationEvent.value?.target == LoginViewModel.NavigationTarget.ADMIN_DASHBOARD)
    }
}