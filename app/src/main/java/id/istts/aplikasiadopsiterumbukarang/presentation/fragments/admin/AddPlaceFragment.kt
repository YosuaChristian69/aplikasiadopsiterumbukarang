package id.istts.aplikasiadopsiterumbukarang.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAddPlaceBinding
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addPlace.AddPlaceViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.SelectionMode

class AddPlaceFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddPlaceViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[AddPlaceViewModel::class.java]
        // Initialize services
        initializeServices()
        setupUI()
        setupObservers()
        setupGoogleMaps()
        setupCoralSelectionResultListener()
    }

    private fun initializeServices() {
        try {
            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize services", e)
            Toast.makeText(requireContext(), "Failed to initialize services: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Add button
        binding.btnAddPlace.setOnClickListener {
            viewModel.addLocationToBackend()
        }
        binding.btnCancelAdd.setOnClickListener {
            findNavController().navigate(R.id.action_addPlaceFragment4_to_adminPlaceDashboardFragment)
        }
        binding.btnChooseCorals.setOnClickListener {
            // Define the navigation action, passing the ADMIN mode as an argument.
            val action = AddPlaceFragmentDirections
                .actionAddPlaceFragment4ToUserSelectSpeciesFragment(SelectionMode.ADMIN_MULTI_SELECTION)
            findNavController().navigate(action)
        }

        setupSearchFunctionality()
        setupDescriptionFunctionality()
    }

    private fun setupObservers() {
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.selectedCoralIds.observe(viewLifecycleOwner) { coralIds ->
            // We could update the UI here as well, but the primary update
            // happens via the fragment result listener for immediate feedback.
            Log.d(TAG, "ViewModel now holds ${coralIds.size} selected coral IDs.")
            // You could call a function here to fetch coral names and update the ChipGroup
            // updateCoralsChipGroup(coralIds)
        }

        // Add button state
        viewModel.isAddButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnAddPlace.isEnabled = isEnabled
            binding.btnAddPlace.alpha = if (isEnabled) 1.0f else 0.6f
        }

        // Selected address
        viewModel.selectedAddress.observe(viewLifecycleOwner) { address ->
            if (!address.isNullOrEmpty()) {
                updateAddressOverlay(address)
            }
        }

        // Move map to location (from search)
        viewModel.moveMapToLocation.observe(viewLifecycleOwner) { locationData ->
            locationData?.let { (latLng, address) ->
                if (isMapReady && ::googleMap.isInitialized) {
                    moveMapToLocation(latLng, address)
                    viewModel.setLocationFromSearch(latLng, address)
                }
                viewModel.clearMoveMapEvent()
            }
        }

        // Selected location (for map updates)
        viewModel.selectedLocation.observe(viewLifecycleOwner) { latLng ->
            latLng?.let {
                if (isMapReady && ::googleMap.isInitialized) {
                    updateMapMarker(it)
                }
            }
        }

        // Error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        // Success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        // Places API errors
        viewModel.placesApiError.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        // Finish activity
        viewModel.shouldFinish.observe(viewLifecycleOwner) { shouldFinish ->
            if (shouldFinish) {
                requireActivity().supportFragmentManager.popBackStack()
                viewModel.clearFinishEvent()
            }
        }
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
                        viewModel.searchPlaces(query)
                    }
                    binding.root.postDelayed(searchRunnable!!, 500)
                }
            }
        })
    }

    private fun setupDescriptionFunctionality() {
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val description = s.toString().trim()
                viewModel.onDescriptionChanged(description)
            }
        })
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
            Toast.makeText(requireContext(), "Failed to load map: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        isMapReady = true

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
                viewModel.onMapLocationSelected(latLng)
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

    private fun updateMapMarker(latLng: LatLng) {
        // Clear previous markers
        googleMap.clear()

        // Add marker at selected location
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
        )

        // Move camera to selected location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun moveMapToLocation(latLng: LatLng, address: String) {
        Log.d(TAG, "Moving map to: $latLng with address: $address")

        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        updateAddressOverlay(address)
    }

    private fun updateAddressOverlay(address: String) {
        binding.addressOverlay.visibility = View.VISIBLE
        binding.tvSelectedAddress.text = address
        Log.d(TAG, "Address overlay updated: $address")
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

    // NEW: This function sets up the listener that waits for data to be sent back.
    private fun setupCoralSelectionResultListener() {
        setFragmentResultListener("coralSelectionRequest") { requestKey, bundle ->
            val selectedIds = bundle.getIntegerArrayList("selectedCoralIds")
            if (selectedIds != null) {
                Log.d(TAG, "Received result: $selectedIds")
                // Update the ViewModel with the new list of IDs
                viewModel.onCoralsSelected(selectedIds)
                // Update the UI to show the number of selected corals.
                // For a better UX, you would fetch the coral names and display them as chips.
                updateCoralsChipGroupWithCount(selectedIds.size)
            }
        }
    }

    // NEW: A simple function to update the UI based on the result.
    // A more advanced version would fetch coral names to create named chips.
    private fun updateCoralsChipGroupWithCount(count: Int) {
        val chipGroup = binding.chipGroupSelectedCorals
        chipGroup.removeAllViews() // Clear previous chips

        if (count > 0) {
            binding.tvNoCoralsSelected.visibility = View.GONE
            chipGroup.visibility = View.VISIBLE

            // For now, we just show a summary chip.
            val summaryChip = Chip(requireContext()).apply {
                text = "$count corals selected"
                isCloseIconVisible = true // Allow user to clear the selection
            }
            summaryChip.setOnCloseIconClickListener {
                viewModel.onCoralsSelected(emptyList()) // Clear selection in ViewModel
                updateCoralsChipGroupWithCount(0) // Update UI
            }
            chipGroup.addView(summaryChip)
        } else {
            binding.tvNoCoralsSelected.visibility = View.VISIBLE
            chipGroup.visibility = View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}