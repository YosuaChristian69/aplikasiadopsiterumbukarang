package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentLocationSelectionBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
//import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.LocationSelectionViewModel
//import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.LocationSelectionViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.LocationSelectionViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.LocationSelectionViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.repositories.LocationRepositoryImpl // Use your repository implementation
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationSelectionFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentLocationSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LocationSelectionViewModel
    private val args: LocationSelectionFragmentArgs by navArgs()

    private lateinit var googleMap: GoogleMap
    private var isMapReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupMap()
        setupClickListeners()
        observeViewModel()
    }

// ... inside onViewCreated()

    private fun setupViewModel() {
        // 1. Create the dependencies needed by the repository
        val apiService = RetrofitClient.instance // Assuming you have a RetrofitClient singleton
        val sessionManager = SessionManager(requireContext())

        // 2. Create the repository implementation
        val repository = LocationRepositoryImpl(apiService, sessionManager)

        // 3. Create the factory, passing the repository to it
        val viewModelFactory = LocationSelectionViewModelFactory(args.coralId, repository)

        // 4. Create the ViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)[LocationSelectionViewModel::class.java]
    }
    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Handle marker clicks
        googleMap.setOnMarkerClickListener { marker ->
            val location = marker.tag as? Lokasi
            location?.let { viewModel.onLocationSelected(it) }
            false // Return false to allow default behavior (camera center, info window)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnConfirmLocation.setOnClickListener {
            viewModel.uiState.value.selectedLocation?.let { location ->
                // Use the Fragment Result API to send data back
                setFragmentResult("locationSelectionRequest", bundleOf(
                    "selectedLocationId" to location.idLokasi,
                    "selectedLocationName" to location.lokasiName
                ))
                findNavController().popBackStack()
            } ?: Toast.makeText(requireContext(), "Please select a location from the map.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                if (isMapReady && state.availableLocations.isNotEmpty()) {
                    addMarkersToMap(state.availableLocations)
                }

                // Update UI based on whether a location is selected
                state.selectedLocation?.let { location ->
                    binding.selectedLocationInfoPanel.visibility = View.VISIBLE
                    binding.tvSelectedLocationName.text = location.lokasiName
                    binding.btnConfirmLocation.isEnabled = true
                    binding.btnConfirmLocation.alpha = 1.0f
                } ?: run {
                    binding.selectedLocationInfoPanel.visibility = View.INVISIBLE
                    binding.btnConfirmLocation.isEnabled = false
                    binding.btnConfirmLocation.alpha = 0.5f
                }

                state.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun addMarkersToMap(locations: List<Lokasi>) {
        googleMap.clear()
        if (locations.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        locations.forEach { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            val marker = googleMap.addMarker(
                MarkerOptions().position(latLng).title(location.lokasiName)
            )
            // Store the entire Lokasi object in the marker tag. This is key for the click listener.
            marker?.tag = location
            boundsBuilder.include(latLng)
        }

        // Animate the camera to fit all markers on screen
        val bounds = boundsBuilder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)) // 150 is padding in pixels
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}