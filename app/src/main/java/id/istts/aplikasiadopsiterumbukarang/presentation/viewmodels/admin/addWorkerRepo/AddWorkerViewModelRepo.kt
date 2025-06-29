package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addWorkerRepo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepositoryWorker
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.AddWorkerRepository
import kotlinx.coroutines.launch

class AddWorkerViewModelRepo(private val repository: AddWorkerRepository?= AddWorkerRepository(),private val repo: RepositoryWorker):
    ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addWorkerResult = MutableLiveData<Result<String>>()
    val addWorkerResult: LiveData<Result<String>> = _addWorkerResult

    fun addWorker(token: String, request: AddWorkerRequest, imageUri: Uri? = null, context: Context? = null) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                var response2 = repo.insertHybridly(token, request, imageUri, context)
                if(response2 == "Worker added successfully"){
                    _addWorkerResult.value = Result.success(response2)
                }else{
                    _addWorkerResult.value = Result.success(response2)
                }
//                val response = repository.addWorker(token, request, imageUri, context)
//
//                if (response.isSuccessful) {
//                    val message = response.body()?.msg ?: "Worker added successfully!"
//                    _addWorkerResult.value = Result.success(message)
//                } else {
//                    // Ambil body error sebagai string
//                    val errorBody = response.errorBody()?.string()
//                    val errorMessage = try {
//                        // Parsing JSON untuk mendapatkan pesan dari field "msg"
//                        val errorJson = JsonParser.parseString(errorBody).asJsonObject
//                        errorJson.get("msg").asString
//                    } catch (e: Exception) {
//                        // Fallback jika parsing gagal atau body kosong
//                        "Failed to add worker. Unknown error."
//                    }
//                    // Update LiveData dengan hasil kegagalan
//                    _addWorkerResult.value = Result.failure(Exception(errorMessage))
//                }
            } catch (e: Exception) {
                _addWorkerResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}