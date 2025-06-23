package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.CreateTransactionRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.MidtransTransactionResponse
import id.istts.aplikasiadopsiterumbukarang.domain.models.TransactionItem
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.TransactionRepository
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//data class PaymentUiState(
//    val coralDetails: Coral? = null,
//    val isLoading: Boolean = false,
//    val error: String? = null
//)

// MODIFIED: Constructor now accepts SessionManager
class UserPaymentViewModel(
    private val coralRepository: CoralRepository,
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _midtransResponse = MutableLiveData<MidtransTransactionResponse?>(null)
    val midtransResponse: LiveData<MidtransTransactionResponse?> = _midtransResponse

    // Function to load the initial coral details (name, price)
    fun loadCoralDetails(coralId: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // THE FIX: Get the token from SessionManager first.
                val token = sessionManager.fetchAuthToken()
                    ?: throw Exception("User not authenticated. Please login again.")

                // Pass the real token to the repository function.
                val coral = coralRepository.getSingleCoral( coralId,token)
                _uiState.update { it.copy(isLoading = false, coralDetails = coral.getOrNull()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Function called when the "Pay Now" button is clicked
    fun initiatePayment(locationId: Int, nickname: String, message: String) {
        val currentCoral = _uiState.value.coralDetails ?: return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val request = CreateTransactionRequest(
                    locationId = locationId,
                    nickname = nickname,
                    message = message,
                    items = listOf(TransactionItem(coralId = currentCoral.id_tk, amount = 1))
                )
                val response = transactionRepository.createAdoptionTransaction(request)
                _midtransResponse.value = response
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Function for the "developer shortcut"
    fun manuallyFulfillOrderForDebug(coralId: Int, locationId: Int, amount: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                transactionRepository.fulfillOrderForDebug(coralId, locationId, amount)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}