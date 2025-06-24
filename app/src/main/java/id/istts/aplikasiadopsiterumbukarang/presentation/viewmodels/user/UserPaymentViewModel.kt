package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user

import android.util.Log
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
        // Checkpoint 1: The function is called.
        Log.d("PAYMENT_DEBUG", "Step 1: loadCoralDetails called. Setting isLoading=true.")
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Checkpoint 2: The coroutine starts.
                Log.d("PAYMENT_DEBUG", "Step 2: Coroutine launched. Fetching token.")

                val token = sessionManager.fetchAuthToken()
                    ?: throw Exception("User token is null or empty.")

                // Checkpoint 3: The token is found.
                Log.d("PAYMENT_DEBUG", "Step 3: Token found. Calling repository's getSingleCoral function.")

                // This is the network call.
                val result = coralRepository.getSingleCoral(token = token, id =coralId)

                // Checkpoint 4: The network call has completed.
                Log.d("PAYMENT_DEBUG", "Step 4: Repository call finished. The result was a success: ${result.isSuccess}")

                // This will fail if the result was a failure, and jump to the catch block.
                val coral = result.getOrThrow()

                // Checkpoint 5: The data was successfully unwrapped.
                Log.d("PAYMENT_DEBUG", "Step 5: Result unwrapped successfully. Coral is ${coral.tk_name}")

                // Update the UI with the final data.
                _uiState.update { it.copy(isLoading = false, coralDetails = coral) }
                Log.d("PAYMENT_DEBUG", "Step 6: Final state updated. UI should now appear.")

            } catch (e: Exception) {
                // Checkpoint X: If any step in the 'try' block fails, we end up here.
                Log.e("PAYMENT_DEBUG", "Step X: CATCH BLOCK EXECUTED! The error is:", e)
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
    fun manuallyFulfillOrderForDebug(coralId: Int, locationId: Int, amount: Int, nickname: String ,message: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                Log.d("test value",nickname)
                transactionRepository.fulfillOrderForDebug(coralId, locationId, amount,nickname,message)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}