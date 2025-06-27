package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.placeDashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.LokasiRepository
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.placeDashboard.AdminPlaceDashboardUiState
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class AdminPlaceDashboardViewModel(
    private val lokasiRepository: LokasiRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableLiveData(AdminPlaceDashboardUiState())
    val uiState: LiveData<AdminPlaceDashboardUiState> = _uiState

    // LiveData untuk navigasi
    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _navigateToAddPlace = MutableLiveData<Boolean>()
    val navigateToAddPlace: LiveData<Boolean> = _navigateToAddPlace

    private val _navigateToLogout = MutableLiveData<Boolean>()
    val navigateToLogout: LiveData<Boolean> = _navigateToLogout

    fun onLogoutClicked() {
        sessionManager.clearSession()
        _navigateToLogout.value = true
    }

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        // Ambil token dari session
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            _navigateToLogin.value = true
            return
        }

        // Set state awal
        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            userName = sessionManager.fetchUserName() ?: "Admin"
        )

        viewModelScope.launch {
            val result = lokasiRepository.getLokasi(token)

            result.onSuccess { lokasiList ->
                // Jika sukses, update state dengan data yang diterima
                _uiState.postValue(_uiState.value?.copy(
                    isLoading = false,
                    allLokasi = lokasiList,
                    displayedLokasi = lokasiList,
                    totalPlacesCount = lokasiList.size,
                    activePlacesCount = lokasiList.size, // Asumsi semua aktif untuk saat ini
                    errorMessage = null
                ))
            }.onFailure { exception ->
                // Jika gagal, update state dengan pesan error
                _uiState.postValue(_uiState.value?.copy(
                    isLoading = false,
                    errorMessage = "Failed to load places: ${exception.message}"
                ))
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentState = _uiState.value ?: return

        val filteredList = if (query.isBlank()) {
            currentState.allLokasi
        } else {
            currentState.allLokasi.filter { lokasi ->
                lokasi.lokasiName.contains(query, ignoreCase = true) ||
                        lokasi.description?.contains(query, ignoreCase = true) == true
            }
        }

        _uiState.value = currentState.copy(displayedLokasi = filteredList)
    }

    // Fungsi untuk handle klik
    fun onAddPlaceClicked() {
        _navigateToAddPlace.value = true
    }

    fun onNavigationHandled() {
        _navigateToLogin.value = false
        _navigateToAddPlace.value = false
        _navigateToLogout.value = false // <- Tambahkan ini
    }
}