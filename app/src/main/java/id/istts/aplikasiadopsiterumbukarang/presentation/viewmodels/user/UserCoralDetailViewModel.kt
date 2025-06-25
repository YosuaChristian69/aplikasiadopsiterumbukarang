package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.CoralDetailResponse
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.launch

class UserCoralDetailViewModel(private val userRepository: UserRepository) : ViewModel() {
    val apiService = RetrofitClient.instance

    // CORRECTED: The LiveData now holds the flat CoralDetailResponse
    private val _coralDetail = MutableLiveData<CoralDetailResponse?>()
    val coralDetail: LiveData<CoralDetailResponse?> = _coralDetail

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchCoralDetail(token: String, ownershipId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.getSingleCoralDetail(token, ownershipId)
                if (response.isSuccessful) {
                    _coralDetail.value = response.body()
                } else {
                    _error.value = "Failed to load details: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
