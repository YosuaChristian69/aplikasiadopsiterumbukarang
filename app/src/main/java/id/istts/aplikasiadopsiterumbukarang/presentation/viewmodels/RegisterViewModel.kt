package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.istts.aplikasiadopsiterumbukarang.domain.models.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.VerifyAndRegisterResponse
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    private val _verificationState = MutableLiveData<VerificationState>()
    val verificationState: LiveData<VerificationState> = _verificationState

    private val _recaptchaState = MutableLiveData<RecaptchaState>()
    val recaptchaState: LiveData<RecaptchaState> = _recaptchaState

    private val _isGetCodeLoading = MutableLiveData<Boolean>()
    val isGetCodeLoading: LiveData<Boolean> = _isGetCodeLoading

    private val _isRegisterLoading = MutableLiveData<Boolean>()
    val isRegisterLoading: LiveData<Boolean> = _isRegisterLoading

    private val _isRecaptchaLoading = MutableLiveData<Boolean>()
    val isRecaptchaLoading: LiveData<Boolean> = _isRecaptchaLoading

    private val _fieldError = MutableLiveData<FieldError>()
    val fieldError: LiveData<FieldError> = _fieldError

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    private val _timerEvent = MutableLiveData<TimerEvent>()
    val timerEvent: LiveData<TimerEvent> = _timerEvent

    private val _fieldsEnabledState = MutableLiveData<Boolean>()
    val fieldsEnabledState: LiveData<Boolean> = _fieldsEnabledState

    // reCAPTCHA token storage
    private var recaptchaToken: String? = null

    fun requestVerificationCode(fullName: String, email: String, password: String, confirmPassword: String) {

        if (!validateFieldsForVerification(fullName, email, password, confirmPassword)) {
            return
        }

        _isGetCodeLoading.value = true
        _verificationState.value = VerificationState.Loading

        val request = RequestVerificationRequest(
            name = fullName,
            email = email,
            password = password
        )

        RetrofitClient.instance.requestVerification(request).enqueue(object :
            Callback<RequestVerificationResponse> {
            override fun onResponse(call: Call<RequestVerificationResponse>, response: Response<RequestVerificationResponse>) {
                _isGetCodeLoading.value = false

                if (response.isSuccessful) {
                    val requestResponse = response.body()
                    _verificationState.value = VerificationState.Success
                    _successMessage.value = requestResponse?.msg ?: "Verification code sent"

                    _fieldsEnabledState.value = false
                    _timerEvent.value = TimerEvent.Start(600000)
                } else {
                    _verificationState.value = VerificationState.Error("Failed to send verification code")
                    _errorMessage.value = "Failed to send verification code. Please try again."
                }
            }

            override fun onFailure(call: Call<RequestVerificationResponse>, t: Throwable) {
                _isGetCodeLoading.value = false
                _verificationState.value = VerificationState.Error("Network error: ${t.message}")
                _errorMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun verifyAndRegister(email: String, verificationCode: String) {
        // Validate verification code
        if (!validateVerificationCode(verificationCode)) {
            return
        }

        // Validate reCAPTCHA
        if (!validateRecaptcha()) {
            return
        }

        _isRegisterLoading.value = true
        _registerState.value = RegisterState.Loading

        val request = VerifyAndRegisterRequest(
            email = email,
            verificationCode = verificationCode
            // You can add recaptcha token to your request model if needed
            // recaptchaToken = recaptchaToken
        )

        RetrofitClient.instance.verifyAndRegister(request).enqueue(object :
            Callback<VerifyAndRegisterResponse> {
            override fun onResponse(call: Call<VerifyAndRegisterResponse>, response: Response<VerifyAndRegisterResponse>) {
                _isRegisterLoading.value = false

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.msg == "Registration successful") {
                        _registerState.value = RegisterState.Success
                        _navigationEvent.value = NavigationEvent.ShowSuccessDialog
                    } else {
                        _registerState.value = RegisterState.Error(registerResponse?.msg ?: "Registration failed")
                        _errorMessage.value = registerResponse?.msg ?: "Registration failed"
                    }
                } else {
                    _registerState.value = RegisterState.Error("Registration failed")
                    _errorMessage.value = "Registration failed. Please try again."
                }
            }

            override fun onFailure(call: Call<VerifyAndRegisterResponse>, t: Throwable) {
                _isRegisterLoading.value = false
                _registerState.value = RegisterState.Error("Network error: ${t.message}")
                _errorMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun validateAllInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        verificationCode: String,
        termsAccepted: Boolean
    ): Boolean {
        if (!validateFieldsForVerification(fullName, email, password, confirmPassword)) {
            return false
        }

        if (!validateVerificationCode(verificationCode)) {
            return false
        }

        if (!validateRecaptcha()) {
            return false
        }

        if (!termsAccepted) {
            _errorMessage.value = "Please accept the terms and conditions"
            return false
        }

        return true
    }

    private fun validateFieldsForVerification(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        // Validate full name
        if (fullName.isEmpty()) {
            _fieldError.value = FieldError(FieldType.FULL_NAME, "Full name is required")
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            _fieldError.value = FieldError(FieldType.EMAIL, "Email is required")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _fieldError.value = FieldError(FieldType.EMAIL, "Please enter a valid email")
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            _fieldError.value = FieldError(FieldType.PASSWORD, "Password is required")
            return false
        }

        if (password.length < 6) {
            _fieldError.value = FieldError(FieldType.PASSWORD, "Password must be at least 6 characters")
            return false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            _fieldError.value = FieldError(FieldType.CONFIRM_PASSWORD, "Please confirm your password")
            return false
        }

        if (password != confirmPassword) {
            _fieldError.value = FieldError(FieldType.CONFIRM_PASSWORD, "Passwords do not match")
            return false
        }

        return true
    }

    private fun validateVerificationCode(verificationCode: String): Boolean {
        if (verificationCode.isEmpty()) {
            _fieldError.value = FieldError(FieldType.VERIFICATION_CODE, "Verification code is required")
            return false
        }

        if (verificationCode.length != 5) {
            _fieldError.value = FieldError(FieldType.VERIFICATION_CODE, "Verification code must be 5 digits")
            return false
        }

        return true
    }

    private fun validateRecaptcha(): Boolean {
        if (recaptchaToken.isNullOrEmpty()) {
            _errorMessage.value = "Please complete reCAPTCHA verification"
            return false
        }
        return true
    }

    // reCAPTCHA related functions
    fun startRecaptchaVerification() {
        _isRecaptchaLoading.value = true
        _recaptchaState.value = RecaptchaState.Loading
    }

    fun onRecaptchaSuccess(token: String) {
        recaptchaToken = token
        _isRecaptchaLoading.value = false
        _recaptchaState.value = RecaptchaState.Success
        _successMessage.value = "reCAPTCHA verification successful"
        updateRegisterButtonState()
    }

    fun onRecaptchaFailure(errorMessage: String? = null) {
        recaptchaToken = null
        _isRecaptchaLoading.value = false
        _recaptchaState.value = RecaptchaState.Error(errorMessage ?: "reCAPTCHA verification failed")
        _errorMessage.value = errorMessage ?: "reCAPTCHA verification failed. Please try again."
        updateRegisterButtonState()
    }

    fun resetRecaptcha() {
        recaptchaToken = null
        _recaptchaState.value = RecaptchaState.Idle
        updateRegisterButtonState()
    }

    private fun updateRegisterButtonState() {
        // You can use this to enable/disable register button based on reCAPTCHA state
        // This will be observed in the fragment
    }

    fun isRecaptchaVerified(): Boolean {
        return !recaptchaToken.isNullOrEmpty() && _recaptchaState.value is RecaptchaState.Success
    }

    fun onTimerFinished() {
        _fieldsEnabledState.value = true
        _timerEvent.value = TimerEvent.Finished
    }

    fun navigateToLogin() {
        _navigationEvent.value = NavigationEvent.NavigateToLogin
    }

    // Sealed classes dan data classes untuk state management
    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    sealed class VerificationState {
        object Idle : VerificationState()
        object Loading : VerificationState()
        object Success : VerificationState()
        data class Error(val message: String) : VerificationState()
    }

    sealed class RecaptchaState {
        object Idle : RecaptchaState()
        object Loading : RecaptchaState()
        object Success : RecaptchaState()
        data class Error(val message: String) : RecaptchaState()
    }

    data class FieldError(val field: FieldType, val message: String)

    enum class FieldType {
        FULL_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD, VERIFICATION_CODE
    }

    sealed class NavigationEvent {
        object NavigateToLogin : NavigationEvent()
        object ShowSuccessDialog : NavigationEvent()
    }

    sealed class TimerEvent {
        data class Start(val milliseconds: Long) : TimerEvent()
        object Finished : TimerEvent()
    }
}