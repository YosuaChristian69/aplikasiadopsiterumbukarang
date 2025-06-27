package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import kotlinx.coroutines.launch

class EditCoralViewModel(private val repository: CoralRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData ini akan kita isi dari Parcelable, bukan dari fetch
    private val _coral = MutableLiveData<Coral>()
    val coral: LiveData<Coral> = _coral

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    // HAPUS FUNGSI INI! Ini adalah sumber masalah.
    /*
    fun fetchCoralData(id: Int, token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // ...
        }
    }
    */

    // BUAT FUNGSI BARU INI untuk menerima data dari Fragment
    fun setInitialCoralData(coral: Coral) {
        _coral.value = coral
    }

    // Fungsi updateCoral tetap sama, ini sudah benar.
    fun updateCoral(id: Int, token: String, editRequest: EditCoralRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.editCoral(id, token, editRequest)
                .onSuccess {
                    _updateSuccess.value = true
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update coral"
                }
            _isLoading.value = false
        }
    }

    // Fungsi validasi tetap sama, ini sudah benar.
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
        } else if (price.toIntOrNull() == null) {
            errors["price"] = "Price must be a valid number"
        }
        if (stock.trim().isEmpty()) {
            errors["stock"] = "Stock is required"
        } else if (stock.toIntOrNull() == null) {
            errors["stock"] = "Stock must be a valid number"
        }
        return Pair(errors.isEmpty(), errors)
    }
}