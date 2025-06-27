package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import id.istts.aplikasiadopsiterumbukarang.domain.models.UserCoral
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.launch

/**
 * ViewModel for the UserDashboardFragment.
 * This class is responsible for fetching and holding the UI-related data for the user's dashboard,
 * surviving configuration changes, and communicating with the repository layer.
 */
class UserDashboardViewModel : ViewModel() {
    val apiService = RetrofitClient.instance
    // You would inject your repository, for example using Hilt/Dagger or a simple factory.
    // For now, we'll instantiate it directly.
    private val userRepository = UserRepository(apiService)

    // LiveData to hold the list of user's corals. This is observable by the Fragment.
    // Private MutableLiveData that can be modified within the ViewModel.
    private val _userCorals = MutableLiveData<List<UserCoral>>()
    // Public LiveData that is read-only for the UI.
    val userCorals: LiveData<List<UserCoral>> = _userCorals

    // LiveData to hold the loading state.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData to hold any error messages.
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Fetches the user's coral collection from the repository.
     * It updates the LiveData properties based on the result of the API call.
     *
     * @param token The authentication token for the user.
     */
    fun fetchUserCorals(token: String) {
        // Launch a coroutine in the ViewModel's scope.
        // This scope is automatically cancelled when the ViewModel is cleared.
        viewModelScope.launch {
            // Set loading state to true before starting the network request.
            _isLoading.value = true
            Log.d("UserDashboardViewModel", "Fetching user corals...")

            try {
                // Call the repository to get the user's coral collection.
                val response = userRepository.getUserCoralCollection(token)

                if (response.isSuccessful && response.body() != null) {
                    // If the request is successful, update the userCorals LiveData.
                    _userCorals.value = response.body()!!.collection
                    Log.d("UserDashboardViewModel", "Successfully fetched ${response.body()!!.collection.size} corals.")
                } else {
                    // If the API call is not successful, post an error message.
                    val errorMsg = "Failed to fetch data: ${response.message()}"
                    _error.value = errorMsg
                    Log.e("UserDashboardViewModel", errorMsg)
                }
            } catch (e: Exception) {
                // If an exception occurs (e.g., network error), post an error message.
                val errorMsg = "An error occurred: ${e.message}"
                _error.value = errorMsg
                Log.e("UserDashboardViewModel", errorMsg, e)
            } finally {
                // Set loading state to false after the request is complete.
                _isLoading.value = false
            }
        }
    }
}
