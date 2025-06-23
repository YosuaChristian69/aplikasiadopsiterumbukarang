package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editUser

import android.util.Log
// import android.util.Patterns // Dihapus karena sudah tidak digunakan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class EditUserViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // Regex untuk validasi email, tidak bergantung pada Android Framework
    private companion object {
        private const val EMAIL_REGEX = "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    }

    private val _user = MutableLiveData<Worker?>()
    val user: LiveData<Worker?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    fun fetchUserById(userId: String, sessionManager: SessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token not found"
                    _isLoading.value = false
                    return@launch
                }

                Log.d("EditUserViewModel", "Fetching user with ID: $userId")
                val response = apiService.fetchUserById(userId, token)

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        _user.value = userResponse.user
                        Log.d("EditUserViewModel", "User fetched successfully: ${userResponse.user.full_name}")
                    } else {
                        _error.value = "No user data received"
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Authentication failed"
                        404 -> "User not found"
                        else -> "Failed to fetch user: ${response.message()}"
                    }
                    _error.value = errorMsg
                    Log.e("EditUserViewModel", "Error fetching user: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                Log.e("EditUserViewModel", "Exception fetching user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(userId: String, updateData: Map<String, String>, sessionManager: SessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _updateSuccess.value = false

                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token not found"
                    _isLoading.value = false
                    return@launch
                }

                Log.d("EditUserViewModel", "Updating user with ID: $userId")
                Log.d("EditUserViewModel", "Update data: $updateData")

                // Validasi email menggunakan Regex standar, bukan Patterns
                updateData["email"]?.let { email ->
                    if (email.isNotBlank() && !email.matches(EMAIL_REGEX.toRegex())) {
                        _error.value = "Invalid email format"
                        _isLoading.value = false
                        return@launch
                    }
                }

                val response = apiService.updateUserById(userId, token, updateData)

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse != null) {
                        _updateSuccess.value = true
                        Log.d("EditUserViewModel", "User updated successfully: ${updateResponse.msg}")
                        _user.value = updateResponse.user
                    } else {
                        _error.value = "No response from server"
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid data provided"
                        401 -> "Authentication failed"
                        404 -> "User not found"
                        409 -> "Email already exists"
                        422 -> "Invalid email format or data validation failed"
                        else -> "Failed to update user: ${response.message()}"
                    }
                    _error.value = errorMsg
                    Log.e("EditUserViewModel", "Error updating user: ${response.code()} - ${response.message()}")
                }

            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                Log.e("EditUserViewModel", "Exception updating user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}