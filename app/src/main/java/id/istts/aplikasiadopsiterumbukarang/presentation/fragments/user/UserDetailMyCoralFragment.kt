
// Update your UserDetailMyCoralFragment.kt file with this corrected version
package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.CoralDetailResponse
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserCoralDetailViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

class UserDetailMyCoralFragment : Fragment(), OnMapReadyCallback {
    private val viewModel: UserCoralDetailViewModel by viewModels()
    private val args: UserDetailMyCoralFragmentArgs by navArgs()
    private lateinit var sessionManager: SessionManager

    private var googleMap: GoogleMap? = null
    private var locationLatLng: LatLng? = null
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_detail_my_coral, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        observeViewModel()

        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            viewModel.fetchCoralDetail(token, args.ownershipId)
        }

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.coralDetail.observe(viewLifecycleOwner) { detail ->
            if (detail?.ownershipDetails != null) {
                view?.let { populateUi(it, detail) }
            } else {
                Log.e("UserDetailFragment", "Received null or incomplete detail object.")
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun populateUi(view: View, detail: CoralDetailResponse) {
        try {
            // Get a reference to the nested ownership details for cleaner access
            val ownership = detail.ownershipDetails!!

            // Find views
            val tvCoralName: TextView = view.findViewById(R.id.tv_coral_name)
            val ivCoralImage: ImageView = view.findViewById(R.id.iv_coral_image)
            val tvSpeciesName: TextView = view.findViewById(R.id.tv_species_name)
            val ivPlanterPhoto: ImageView = view.findViewById(R.id.iv_planter_photo)
            val tvPlanterName: TextView = view.findViewById(R.id.tv_planter_name)
            val tvPlanterEmail: TextView = view.findViewById(R.id.tv_planter_email)
            val tvPlanterPhone: TextView = view.findViewById(R.id.tv_planter_phone)
            val tvAddress: TextView = view.findViewById(R.id.tv_address)
            val tvCoralId: TextView = view.findViewById(R.id.tv_coral_id)
            val tvDop: TextView = view.findViewById(R.id.tv_dop)
            val tvIdSpecies: TextView = view.findViewById(R.id.tv_id_species)
            val tvIdPlace: TextView = view.findViewById(R.id.tv_id_place)
            val tvCoordinates: TextView = view.findViewById(R.id.tv_coordinates)
            val tvOwnerName: TextView = view.findViewById(R.id.tv_owner_name)
            val tvOwnerEmail: TextView = view.findViewById(R.id.tv_owner_email)
            val tvOwnerPhone: TextView = view.findViewById(R.id.tv_owner_phone)
            val ivIdCoralImage: ImageView = view.findViewById(R.id.iv_id_coral_image)

            // Populate views with safe calls and defaults
            tvCoralName.text = ownership.coralNickname ?: "Unnamed Coral"
            tvSpeciesName.text = ownership.species?.scientificName ?: "Unknown Species"
            tvAddress.text = detail.location?.address ?: "Address not available"
            tvCoralId.text = ownership.ownershipId.toString()
            tvDop.text = formatDate(ownership.adoptedAt)
            tvIdSpecies.text = ownership.species?.name ?: "N/A"
            tvIdPlace.text = detail.location?.name ?: "N/A"
            tvCoordinates.text = "lat: ${detail.location?.latitude ?: 0.0}, lng: ${detail.location?.longitude ?: 0.0}"
            tvOwnerName.text = ownership.owner?.name ?: "Unknown Owner"
            tvOwnerEmail.text = ownership.owner?.email ?: "N/A" // CORRECTED: Use owner's email
            tvOwnerPhone.text = ownership.owner?.phone ?: "Not available"

            Glide.with(this).load(ownership.species?.imagePath).placeholder(R.drawable.ic_image_placeholder).into(ivCoralImage)
            Glide.with(this).load(ownership.species?.imagePath).placeholder(R.drawable.ic_image_placeholder).into(ivIdCoralImage)

            if (detail.planter != null) {
                tvPlanterName.text = detail.planter.name
                tvPlanterEmail.text = "Email : ${detail.planter.email}"
                tvPlanterPhone.text = "Phone : ${detail.planter.phone ?: "Not available"}"
                Glide.with(this).load(detail.planter.imagePath).circleCrop().placeholder(R.drawable.ic_person_placeholder).into(ivPlanterPhoto)
            } else {
                tvPlanterName.text = "Not Yet Assigned"
                tvPlanterEmail.visibility = View.GONE
                tvPlanterPhone.visibility = View.GONE
                ivPlanterPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }

            detail.location?.let {
                locationLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.let { map -> moveMap(map) }
            }
        } catch (e: Exception) {
            Log.e("UserDetailFragment", "Error populating UI", e)
        }
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState) }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        locationLatLng?.let { moveMap(map) }
    }

    private fun moveMap(map: GoogleMap) {
        locationLatLng?.let {
            map.addMarker(MarkerOptions().position(it).title("Planting Location"))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateString))
        } catch (e: Exception) {
            dateString
        }
    }
}
