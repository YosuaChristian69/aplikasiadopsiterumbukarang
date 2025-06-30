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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
// @RunWith(MockitoJUnitRunner::class) // <-- DIHAPUS!
class LoginViewModelTest {

    // Aturan ini untuk memastikan LiveData berjalan di thread yang sama secara sinkron
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // DITAMBAHKAN: Aturan ini menggantikan fungsi @RunWith untuk menginisialisasi @Mock
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var loginRepository: LoginRepository

    @Mock
    private lateinit var sessionManager: SessionManager

    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Anotasi @Mock sudah diinisialisasi oleh mockitoRule, jadi kita bisa langsung pakai
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

        // DITAMBAHKAN: Mock untuk id_user sesuai dengan signatur baru
        whenever(fakeAdminDetailsJson.getInt("id_user")).thenReturn(1)
        whenever(fakeAdminDetailsJson.getString("full_name")).thenReturn("Admin Coral")
        whenever(fakeAdminDetailsJson.getString("email")).thenReturn("admin@coralproject.com")
        whenever(fakeAdminDetailsJson.getString("status")).thenReturn("admin")

        val viewModelSpy = spy(viewModel)
        doReturn(fakeAdminDetailsJson).whenever(viewModelSpy).decodeTokenPayload(any())
        whenever(loginRepository.login(any())).thenReturn(Result.success(loginResponse))

        // Act
        viewModelSpy.performLogin(email, password)

        // Assert
        verify(loginRepository).login(eq(loginRequest))
        verify(sessionManager).saveAuthToken(fakeToken)
        // DIUBAH: Sesuaikan verify dengan 4 parameter (id, nama, email, status)
        verify(sessionManager).saveUserDetails(1, "Admin Coral", "admin@coralproject.com", "admin")

        assertEquals(LoginViewModel.LoginState.Success, viewModel.loginState.value)
        assertEquals("Login successful", viewModel.successMessage.value)
        assertEquals(LoginViewModel.NavigationTarget.ADMIN_DASHBOARD, viewModel.navigationEvent.value?.target)
        assertFalse(viewModel.isLoading.value ?: true)
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
        // DIUBAH: Sesuaikan verify dengan 4 parameter menggunakan any()
        verify(sessionManager, never()).saveUserDetails(any(), any(), any(), any())

        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Error)
        assertEquals(errorMessage, (viewModel.loginState.value as LoginViewModel.LoginState.Error).message)
        assertEquals(errorMessage, viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value ?: true)
    }

    @Test
    fun `performLogin dengan email kosong harus menampilkan error field`() = runTest {
        // Arrange
        val email = ""
        val password = "password123"

        // Act
        viewModel.performLogin(email, password)

        // Assert
        verify(loginRepository, never()).login(any())
        assertEquals(LoginViewModel.FieldType.EMAIL, viewModel.fieldError.value?.field)
        assertEquals("Email is required", viewModel.fieldError.value?.message)
        assertNull(viewModel.isLoading.value)
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
        assertEquals(LoginViewModel.FieldType.PASSWORD, viewModel.fieldError.value?.field)
        assertEquals("Password is required", viewModel.fieldError.value?.message)
        assertNull(viewModel.isLoading.value)
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
        assertEquals(LoginViewModel.NavigationTarget.ADMIN_DASHBOARD, viewModel.navigationEvent.value?.target)
    }
}