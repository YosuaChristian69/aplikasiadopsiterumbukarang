package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addPlace

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddLokasiRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AddPlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val sessionManager = SessionManager(context)
    private val geocoder = Geocoder(context, Locale.getDefault())
    private var placesClient: PlacesClient? = null

    companion object {
        private const val TAG = "AddPlaceViewModel"
    }

    // UI States
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // Map States
    private val _selectedLocation = MutableLiveData<LatLng?>()
    val selectedLocation: LiveData<LatLng?> = _selectedLocation

    private val _selectedAddress = MutableLiveData<String>()
    val selectedAddress: LiveData<String> = _selectedAddress

    // Description State
    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _isAddButtonEnabled = MutableLiveData<Boolean>()
    val isAddButtonEnabled: LiveData<Boolean> = _isAddButtonEnabled

    private val _moveMapToLocation = MutableLiveData<Pair<LatLng, String>?>()
    val moveMapToLocation: LiveData<Pair<LatLng, String>?> = _moveMapToLocation

    private val _shouldFinish = MutableLiveData<Boolean>()
    val shouldFinish: LiveData<Boolean> = _shouldFinish

    // Places API States
    private val _placesApiError = MutableLiveData<String>()
    val placesApiError: LiveData<String> = _placesApiError

    init {
        _isLoading.value = true
        _isAddButtonEnabled.value = false
        _description.value = ""
        initializePlacesAPI()
    }

    private fun initializePlacesAPI() {
        viewModelScope.launch {
            try {
                val apiKey = context.getString(R.string.google_maps_key)
                Log.d(TAG, "API Key length: ${apiKey.length}")

                if (apiKey.isBlank() || apiKey.contains("YOUR_API_KEY")) {
                    Log.e(TAG, "Invalid Google Maps API key")
                    _placesApiError.value = "Google Maps API key not configured properly"
                    return@launch
                }

                if (!Places.isInitialized()) {
                    Places.initialize(context, apiKey)
                    Log.d(TAG, "Places API initialized successfully")
                }

                placesClient = Places.createClient(context)
                Log.d(TAG, "Places client created successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Places API", e)
                _placesApiError.value = "Places API initialization failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPlaces(query: String) {
        Log.d(TAG, "Searching for: $query")

        placesClient?.let { client ->
            viewModelScope.launch {
                try {
                    val token = AutocompleteSessionToken.newInstance()

                    val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(query)
                        .setCountries("ID") // Restrict to Indonesia
                        .build()

                    client.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            Log.d(TAG, "Places search success, found ${response.autocompletePredictions.size} results")
                            val predictions = response.autocompletePredictions
                            if (predictions.isNotEmpty()) {
                                val firstPrediction = predictions[0]
                                fetchPlaceDetails(firstPrediction.placeId)
                            } else {
                                Log.d(TAG, "No predictions found")
                                _errorMessage.value = "No places found for: $query"
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Places search failed", exception)
                            handlePlacesApiError(exception)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in searchPlaces", e)
                    _errorMessage.value = "Search error: ${e.message}"
                }
            }
        } ?: run {
            Log.e(TAG, "Places client is null")
            _errorMessage.value = "Search service not available"
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        placesClient?.let { client ->
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )

            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            client.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    place.latLng?.let { latLng ->
                        val address = place.address
                            ?: place.name
                            ?: "Unknown location"

                        Log.d(TAG, "Place details: $address at $latLng")
                        _moveMapToLocation.value = Pair(latLng, address)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to fetch place details", exception)
                    _errorMessage.value = "Failed to get place details"
                }
        }
    }

    private fun handlePlacesApiError(exception: Exception) {
        if (exception.message?.contains("9011") == true) {
            Log.e(TAG, "Legacy API error - need to enable Places API (New)")
            _placesApiError.value = "Please enable Places API (New) in Google Cloud Console"
        } else {
            _errorMessage.value = "Search failed: ${exception.message}"
        }
    }

    fun onMapLocationSelected(latLng: LatLng) {
        _selectedLocation.value = latLng
        Log.d(TAG, "Location selected: $latLng")

        // Get address from coordinates asynchronously
        getAddressFromLatLng(latLng)
    }

    private fun getAddressFromLatLng(latLng: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val address = if (addresses?.isNotEmpty() == true) {
                    addresses[0].getAddressLine(0) ?: "${latLng.latitude}, ${latLng.longitude}"
                } else {
                    "${latLng.latitude}, ${latLng.longitude}"
                }

                withContext(Dispatchers.Main) {
                    _selectedAddress.value = address
                    Log.d(TAG, "Address resolved: $address")
                    checkAddButtonEnabled()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting address from coordinates", e)
                withContext(Dispatchers.Main) {
                    val coordinateAddress = "${latLng.latitude}, ${latLng.longitude}"
                    _selectedAddress.value = coordinateAddress
                    Log.d(TAG, "Using coordinates as address: $coordinateAddress")
                    checkAddButtonEnabled()
                }
            }
        }
    }

    fun setLocationFromSearch(latLng: LatLng, address: String) {
        _selectedLocation.value = latLng
        _selectedAddress.value = address
        Log.d(TAG, "Location set from search: $latLng with address: $address")
        checkAddButtonEnabled()
    }

    fun onDescriptionChanged(description: String) {
        _description.value = description
        Log.d(TAG, "Description changed: $description")
        checkAddButtonEnabled()
    }

    private fun checkAddButtonEnabled() {
        val hasLocation = _selectedLocation.value != null
        val hasAddress = !_selectedAddress.value.isNullOrEmpty()
        val hasDescription = !_description.value.isNullOrEmpty()
        _isAddButtonEnabled.value = hasLocation && hasAddress && hasDescription
        Log.d(TAG, "Add button enabled: ${_isAddButtonEnabled.value} (location: $hasLocation, address: $hasAddress, description: $hasDescription)")
    }

    fun addLocationToBackend() {
        val currentLocation = _selectedLocation.value
        val currentAddress = _selectedAddress.value
        val currentDescription = _description.value

        if (currentLocation == null || currentAddress.isNullOrEmpty()) {
            _errorMessage.value = "Please select a location first"
            return
        }

        if (currentDescription.isNullOrEmpty()) {
            _errorMessage.value = "Please enter a description"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _isAddButtonEnabled.value = false

                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Authentication required"
                    return@launch
                }

                Log.d(TAG, "Adding location to backend")
                Log.d(TAG, "Address to send: '$currentAddress'")
                Log.d(TAG, "Description to send: '$currentDescription'")
                Log.d(TAG, "Coordinates: $currentLocation")

                // Perlu memodifikasi AddLokasiRequest untuk include description
                // Assuming AddLokasiRequest needs to be updated to include description field
                val request = AddLokasiRequest(
                    address = currentAddress,
                    description = currentDescription
                )
                Log.d(TAG, "Request object: $request")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.addLokasiGoogle(
                        token = " $token",
                        request = request
                    )
                }

                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response body: ${response.body()}")

                if (response.isSuccessful) {
                    _successMessage.value = "Location added successfully!"
                    _shouldFinish.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error response: $errorBody")
                    _errorMessage.value = "Failed to add location: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding location to backend", e)
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
                _isAddButtonEnabled.value = true
            }
        }
    }

    fun clearMoveMapEvent() {
        _moveMapToLocation.value = null
    }

    fun clearFinishEvent() {
        _shouldFinish.value = false
    }

    fun clearMessages() {
        _errorMessage.value = ""
        _successMessage.value = ""
        _placesApiError.value = ""
    }
}