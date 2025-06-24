package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_detail_my_coral, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        observeViewModel()

        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            viewModel.fetchCoralDetail(token, args.ownershipId)
        } else {
            // Handle user not logged in case
        }
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.coralDetail.observe(viewLifecycleOwner) { detail ->
            view?.let { populateUi(it, detail) }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
        // Observe loading state to show a ProgressBar if you have one
    }

    private fun populateUi(view: View, detail: CoralDetailResponse) {
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

        // Populate views with data
        tvCoralName.text = detail.ownershipDetails!!.coralNickname
        tvSpeciesName.text = detail.ownershipDetails!!.species.toString()
        tvAddress.text = detail.location!!.address
        tvCoralId.text = detail.ownershipDetails!!.ownershipId.toString()
        tvDop.text = formatDate(detail.ownershipDetails!!.adoptedAt.toString())
        tvIdSpecies.text = detail.ownershipDetails!!.species!!.scientificName
        tvIdPlace.text = detail.location.name
        tvCoordinates.text = "lat: ${detail.location.latitude}, lng: ${detail.location.longitude}"
        tvOwnerName.text = detail.ownershipDetails!!.owner!!.name
        tvOwnerEmail.text =  detail.ownershipDetails!!.owner!!.name
        tvOwnerPhone.text =  detail.ownershipDetails!!.owner!!.phone ?: "Not available"

        // Load images using Glide
        Glide.with(this).load(detail.ownershipDetails!!.species!!.imagePath).into(ivCoralImage)
        Glide.with(this).load(detail.ownershipDetails!!.species!!.imagePath).into(ivIdCoralImage) // Also load image into the ID card

        if (detail.planter != null) {
            tvPlanterName.text = detail.planter.name
            tvPlanterEmail.text = "Email : ${detail.planter.email}"
            tvPlanterPhone.text = "Phone : ${detail.planter.phone ?: "Not available"}"
            Glide.with(this).load(detail.planter.imagePath).circleCrop().into(ivPlanterPhoto)
        } else {
            tvPlanterName.text = "Not Yet Assigned"
            tvPlanterEmail.visibility = View.GONE
            tvPlanterPhone.visibility = View.GONE
        }

        // Set map location
        locationLatLng = LatLng(detail.location.latitude, detail.location.longitude)
        googleMap?.let { moveMap(it) }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Move map only if location data is already available
        locationLatLng?.let { moveMap(map) }
    }

    private fun moveMap(map: GoogleMap) {
        locationLatLng?.let {
            map.addMarker(MarkerOptions().position(it).title("Planting Location"))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateString))
        } catch (e: Exception) {
            dateString
        }
    }
}