package id.istts.aplikasiadopsiterumbukarang.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAddPlaceBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddLokasiRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AddPlaceFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var placesClient: PlacesClient? = null
    private lateinit var geocoder: Geocoder
    private lateinit var sessionManager: SessionManager
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String = ""
    private var isMapReady = false
    private var mapFragment: SupportMapFragment? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "AddPlaceFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading initially
        binding.progressBar.visibility = View.VISIBLE

        // Initialize services
        initializeServices()
        setupUI()
        setupGoogleMaps()
    }

    private fun initializeServices() {
        try {
            // Initialize session manager
            sessionManager = SessionManager(requireContext())

            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            geocoder = Geocoder(requireContext(), Locale.getDefault())

            // Initialize Places API with better error handling
            initializePlacesAPI()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize services", e)
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Failed to initialize services: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializePlacesAPI() {
        try {
            val apiKey = getString(R.string.google_maps_key)
            Log.d(TAG, "API Key length: ${apiKey.length}")

            if (apiKey.isBlank() || apiKey.contains("YOUR_API_KEY")) {
                Log.e(TAG, "Invalid Google Maps API key")
                Toast.makeText(requireContext(), "Google Maps API key not configured properly", Toast.LENGTH_LONG).show()
                return
            }

            if (!Places.isInitialized()) {
                Places.initialize(requireContext(), apiKey)
                Log.d(TAG, "Places API initialized successfully")
            }

            placesClient = Places.createClient(requireContext())
            Log.d(TAG, "Places client created successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Places API", e)
            Toast.makeText(requireContext(), "Places API initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Add button - initially disabled
        binding.btnAddPlace.isEnabled = false
        binding.btnAddPlace.alpha = 0.6f

        binding.btnAddPlace.setOnClickListener {
            Log.d(TAG, "Add button clicked")
            Log.d(TAG, "Selected LatLng: $selectedLatLng")
            Log.d(TAG, "Selected Address: '$selectedAddress'")

            if (selectedLatLng != null && selectedAddress.isNotEmpty()) {
                addLocationToBackend()
            } else {
                Toast.makeText(requireContext(), "Please select a location first", Toast.LENGTH_SHORT).show()
            }
        }

        // Search functionality with debounce
        setupSearchFunctionality()
    }

    private fun setupSearchFunctionality() {
        binding.etSearchPlaces.addTextChangedListener(object : TextWatcher {
            private var searchRunnable: Runnable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                // Cancel previous search
                searchRunnable?.let { binding.root.removeCallbacks(it) }

                if (query.length > 2) {
                    // Debounce search by 500ms
                    searchRunnable = Runnable {
                        searchPlaces(query)
                    }
                    binding.root.postDelayed(searchRunnable!!, 500)
                }
            }
        })
    }

    private fun searchPlaces(query: String) {
        Log.d(TAG, "Searching for: $query")

        placesClient?.let { client ->
            lifecycleScope.launch {
                try {
                    val token = AutocompleteSessionToken.newInstance()

                    // Updated request builder with proper configuration
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
                                // Use the Places API to get place details
                                fetchPlaceDetails(firstPrediction.placeId)
                            } else {
                                Log.d(TAG, "No predictions found")
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "No places found for: $query", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Places search failed", exception)

                            // Handle specific error codes
                            if (exception.message?.contains("9011") == true) {
                                Log.e(TAG, "Legacy API error - need to enable Places API (New)")
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Please enable Places API (New) in Google Cloud Console", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Search failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in searchPlaces", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "Places client is null")
            Toast.makeText(requireContext(), "Search service not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        placesClient?.let { client ->
            // Use compatible place fields
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
                        // Use available address fields
                        val address = place.address
                            ?: place.name
                            ?: "Unknown location"

                        Log.d(TAG, "Place details: $address at $latLng")

                        if (isMapReady && ::googleMap.isInitialized) {
                            moveMapToLocation(latLng, address)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to fetch place details", exception)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to get place details", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setupGoogleMaps() {
        Log.d(TAG, "Setting up Google Maps")
        try {
            // Remove existing fragment if any
            mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment

            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance()
                childFragmentManager.beginTransaction()
                    .replace(R.id.map_container, mapFragment!!)
                    .commitAllowingStateLoss()
            }

            mapFragment?.getMapAsync(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Google Maps", e)
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Failed to load map: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        isMapReady = true

        // Hide loading indicator
        binding.progressBar.visibility = View.GONE

        try {
            // Configure map settings
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            // Enable location if permission granted
            enableMyLocation()

            // Set map click listener
            googleMap.setOnMapClickListener { latLng ->
                Log.d(TAG, "Map clicked at: $latLng")
                onMapLocationSelected(latLng)
            }

            // Set initial camera position (Surabaya, Indonesia)
            val initialLocation = LatLng(-7.2575, 112.7521)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12f))

            Log.d(TAG, "Map setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring map", e)
            Toast.makeText(requireContext(), "Error configuring map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.isMyLocationEnabled = true
                getCurrentLocation()
                Log.d(TAG, "My location enabled")
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception enabling location", e)
            }
        } else {
            Log.d(TAG, "Requesting location permission")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                Log.d(TAG, "Current location: $currentLatLng")
                if (isMapReady && ::googleMap.isInitialized) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to get current location", e)
        }
    }

    private fun onMapLocationSelected(latLng: LatLng) {
        selectedLatLng = latLng
        Log.d(TAG, "Location selected: $latLng")

        // Clear previous markers
        googleMap.clear()

        // Add marker at selected location
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
        )

        // Get address from coordinates asynchronously
        getAddressFromLatLngAsync(latLng)

        // Move camera to selected location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun moveMapToLocation(latLng: LatLng, address: String) {
        selectedLatLng = latLng
        selectedAddress = address
        Log.d(TAG, "Moving map to: $latLng with address: $address")

        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

        updateAddressOverlay(address)
        enableAddButton()
    }

    private fun getAddressFromLatLngAsync(latLng: LatLng) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val address = if (addresses?.isNotEmpty() == true) {
                    addresses[0].getAddressLine(0) ?: "${latLng.latitude}, ${latLng.longitude}"
                } else {
                    "${latLng.latitude}, ${latLng.longitude}"
                }

                withContext(Dispatchers.Main) {
                    selectedAddress = address
                    Log.d(TAG, "Address resolved: $selectedAddress")
                    updateAddressOverlay(selectedAddress)
                    enableAddButton()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting address from coordinates", e)
                withContext(Dispatchers.Main) {
                    selectedAddress = "${latLng.latitude}, ${latLng.longitude}"
                    Log.d(TAG, "Using coordinates as address: $selectedAddress")
                    updateAddressOverlay(selectedAddress)
                    enableAddButton()
                }
            }
        }
    }

    private fun updateAddressOverlay(address: String) {
        binding.addressOverlay.visibility = View.VISIBLE
        binding.tvSelectedAddress.text = address
        Log.d(TAG, "Address overlay updated: $address")
    }

    private fun enableAddButton() {
        binding.btnAddPlace.isEnabled = true
        binding.btnAddPlace.alpha = 1.0f
        Log.d(TAG, "Add button enabled")
    }

    private fun addLocationToBackend() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken().toString()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d(TAG, "Adding location to backend")
                Log.d(TAG, "Address to send: '$selectedAddress'")
                Log.d(TAG, "Coordinates: $selectedLatLng")

                binding.progressBar.visibility = View.VISIBLE
                binding.btnAddPlace.isEnabled = false

                val request = AddLokasiRequest(address = selectedAddress)
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
                    Toast.makeText(requireContext(), "Location added successfully!", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error response: $errorBody")
                    Toast.makeText(requireContext(), "Failed to add location: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding location to backend", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnAddPlace.isEnabled = true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Log.d(TAG, "Location permission denied")
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}