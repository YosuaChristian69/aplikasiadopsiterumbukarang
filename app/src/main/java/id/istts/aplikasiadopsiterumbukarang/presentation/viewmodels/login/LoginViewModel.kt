package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.login

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.login.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.LoginRepository
import id.istts.aplikasiadopsiterumbukarang.domain.models.login.LoginResponse
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    // LiveData untuk UI state
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    private val _fieldError = MutableLiveData<FieldError>()
    val fieldError: LiveData<FieldError> = _fieldError

    // Session manager
    private var sessionManager: SessionManager? = null

    fun initSessionManager(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    fun checkExistingSession() {
        sessionManager?.let { sm ->
            if (sm.isLoggedIn()) {
                val userStatus = sm.fetchUserStatus()
                navigateBasedOnRole(userStatus)
            }
        }
    }

    fun performLogin(email: String, password: String) {
        // Validation
        if (!validateInput(email, password)) {
            return
        }

        // Set loading state
        _isLoading.value = true
        _loginState.value = LoginState.Loading

        // API call using repository
        viewModelScope.launch {
            try {
                val result = loginRepository.login(LoginRequest(email, password))

                result.fold(
                    onSuccess = { loginResponse ->
                        handleLoginSuccess(loginResponse)
                    },
                    onFailure = { exception ->
                        handleLoginError(exception)
                    }
                )
            } catch (e: Exception) {
                handleLoginError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                _fieldError.value = FieldError(FieldType.EMAIL, "Email is required")
                return false
            }
            password.isEmpty() -> {
                _fieldError.value = FieldError(FieldType.PASSWORD, "Password is required")
                return false
            }
            else -> return true
        }
    }

    private fun handleLoginSuccess(loginResponse: LoginResponse) {
        if (loginResponse.msg.equals("success login", true) && loginResponse.token != null) {
            sessionManager?.saveAuthToken(loginResponse.token)

            try {
                val userDetails = decodeTokenPayload(loginResponse.token)
                sessionManager?.saveUserDetails(
                    userDetails.getString("full_name"),
                    userDetails.getString("email"),
                    userDetails.getString("status")
                )

                _loginState.value = LoginState.Success
                _successMessage.value = "Login successful"
                navigateBasedOnRole(userDetails.getString("status"))

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Token decode error", e)
                _loginState.value = LoginState.Error("Authentication error")
                _errorMessage.value = "Authentication error"
            }
        } else {
            _loginState.value = LoginState.Error("Login failed")
            _errorMessage.value = "Login failed"
        }
    }

    private fun handleLoginError(exception: Throwable) {
        val errorMsg = exception.message ?: "Unknown error occurred"
        _loginState.value = LoginState.Error(errorMsg)
        _errorMessage.value = errorMsg
    }

    internal fun decodeTokenPayload(token: String): JSONObject {
        val payload = token.split(".")[1]
        val decodedBytes = Base64.decode(
            normalizeBase64(payload),
            Base64.DEFAULT
        )
        return JSONObject(String(decodedBytes))
    }

    private fun normalizeBase64(input: String): String {
        val padding = when (input.length % 4) {
            1 -> "==="
            2 -> "=="
            3 -> "="
            else -> ""
        }
        return input + padding
    }

    private fun navigateBasedOnRole(status: String?) {
        val navigationTarget = when (status) {
            "user" -> NavigationTarget.USER_DASHBOARD
            "admin" -> NavigationTarget.ADMIN_DASHBOARD
            else -> NavigationTarget.WORKER_DASHBOARD
        }
        _navigationEvent.value = NavigationEvent(navigationTarget)
    }

    // Sealed classes untuk state management
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    data class FieldError(val field: FieldType, val message: String)

    enum class FieldType {
        EMAIL, PASSWORD
    }

    data class NavigationEvent(val target: NavigationTarget)

    enum class NavigationTarget {
        USER_DASHBOARD, ADMIN_DASHBOARD, WORKER_DASHBOARD
    }
}