package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.Worker

//package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application.Applicatione
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import kotlinx.coroutines.launch

class EditCorralViewModelRepo2(private val coralRepository: RepostioryCorral,private val repository: CoralRepository): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _coral = MutableLiveData<Coral>()
    val coral: LiveData<Coral> = _coral

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    var local = Applicatione.db
//    fun fetchCoralData(id: Int, token: String) {
//        _isLoading.value = true
//        viewModelScope.launch {
//            repository.getSingleCoral(id, token)
//                .onSuccess { coral ->
//                    _coral.value = coral
//                }
//                .onFailure { exception ->
//                    _errorMessage.value = exception.message ?: "Failed to load coral data"
//                }
//            _isLoading.value = false
//        }
//    }

    fun fetchCoralData(id: Int, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            var corralData = local.CorralDAO().getTerumbuKarangById(id)
            if (corralData != null) {
                _coral.value = corralData.toCorral()
            }else{
                _errorMessage.value = "Coral Data not found"
            }
//            repository.getSingleCoral(id, token)
//                .onSuccess { coral ->
//                    _coral.value = coral
//                }
//                .onFailure { exception ->
//                    _errorMessage.value = exception.message ?: "Failed to load coral data"
//                }
            _isLoading.value = false
        }
    }

    fun updateCoral(id: Int, token: String, editRequest: EditCoralRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            var result = coralRepository.updateHybridly(id, token, editRequest)
            if(result == "Update Success"){
                _updateSuccess.value = true
            }else{
                _errorMessage.value = "No Data Fetched"
            }
//            repository.editCoral(id, token, editRequest)
//                .onSuccess {
//                    _updateSuccess.value = true
//                }
//                .onFailure { exception ->
//                    _errorMessage.value = exception.message ?: "Failed to update coral"
//                }
            _isLoading.value = false
        }
    }

//    fun updateCoral(id: Int, token: String, editRequest: EditCoralRequest) {
//        _isLoading.value = true
//        viewModelScope.launch {
//            repository.editCoral(id, token, editRequest)
//                .onSuccess {
//                    _updateSuccess.value = true
//                }
//                .onFailure { exception ->
//                    _errorMessage.value = exception.message ?: "Failed to update coral"
//                }
//            _isLoading.value = false
//        }
//    }

    fun validateFields(
        name: String,
        species: String,
        price: String,
        stock: String
    ): Pair<Boolean, Map<String, String>> {
        val errors = mutableMapOf<String, String>()

        if (name.trim().isEmpty()) {
            errors["name"] = "Coral name is required"
        }

        if (species.trim().isEmpty()) {
            errors["species"] = "Coral species is required"
        }

        if (price.trim().isEmpty()) {
            errors["price"] = "Price is required"
        } else {
            try {
                price.toInt()
            } catch (e: NumberFormatException) {
                errors["price"] = "Price must be a valid number"
            }
        }

        if (stock.trim().isEmpty()) {
            errors["stock"] = "Stock is required"
        } else {
            try {
                stock.toInt()
            } catch (e: NumberFormatException) {
                errors["stock"] = "Stock must be a valid number"
            }
        }

        return Pair(errors.isEmpty(), errors)
    }
}