package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class EditUserViewModel : ViewModel() {

    private val apiService = RetrofitClient.instance

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

    // FIXED: Changed parameter type to Map<String, String>
    fun updateUser(userId: String, updateData: Map<String, String>, sessionManager: SessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _updateSuccess.value = false

                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _error.value = "Authentication token not found"
                    return@launch
                }

                Log.d("EditUserViewModel", "Updating user with ID: $userId")
                Log.d("EditUserViewModel", "Update data: $updateData")

                val response = apiService.updateUserById(userId, token, updateData)

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse != null) {
                        _updateSuccess.value = true
                        Log.d("EditUserViewModel", "User updated successfully: ${updateResponse.msg}")

                        // Optionally update the local user data
                        _user.value = updateResponse.user
                    } else {
                        _error.value = "No response from server"
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid data provided"
                        401 -> "Authentication failed"
                        404 -> "User not found"
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