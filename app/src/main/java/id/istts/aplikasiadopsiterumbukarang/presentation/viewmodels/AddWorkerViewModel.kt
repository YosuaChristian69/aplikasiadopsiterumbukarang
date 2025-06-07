package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.AddWorkerRepository
import kotlinx.coroutines.launch

class AddWorkerViewModel(
    private val repository: AddWorkerRepository = AddWorkerRepository()
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addWorkerResult = MutableLiveData<Result<String>>()
    val addWorkerResult: LiveData<Result<String>> = _addWorkerResult

    fun addWorker(token: String, request: AddWorkerRequest) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.addWorker(token, request)

                if (response.isSuccessful) {
                    val message = response.body()?.msg ?: "Worker added successfully!"
                    _addWorkerResult.value = Result.success(message)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorJson = com.google.gson.JsonParser.parseString(errorBody)
                        errorJson.asJsonObject.get("msg").asString
                    } catch (e: Exception) {
                        "Failed to add worker. Please try again."
                    }
                    _addWorkerResult.value = Result.failure(Exception(errorMessage))
                }

            } catch (e: Exception) {
                _addWorkerResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}