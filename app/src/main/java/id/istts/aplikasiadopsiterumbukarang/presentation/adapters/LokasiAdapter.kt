package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.GeocodingService
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi



class LokasiAdapter(
    private val onEditClick: (Lokasi) -> Unit,
    private val onDeleteClick: (Lokasi) -> Unit
) : ListAdapter<Lokasi, LokasiAdapter.LokasiViewHolder>(LokasiDiffCallback()) {

    private lateinit var context: Context
    private lateinit var geocodingService: GeocodingService
    private val addressCache = mutableMapOf<String, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LokasiViewHolder {
        context = parent.context

        // Initialize Geocoding service
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        geocodingService = retrofit.create(GeocodingService::class.java)

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_card, parent, false)
        return LokasiViewHolder(view)
    }

    override fun onBindViewHolder(holder: LokasiViewHolder, position: Int) {
        val lokasi = getItem(position)
        holder.bind(lokasi)
    }

    inner class LokasiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeName: TextView = itemView.findViewById(R.id.tv_place_name)
        private val placeId: TextView = itemView.findViewById(R.id.tv_place_id)
        private val description: TextView = itemView.findViewById(R.id.tv_description)
        private val latitude: TextView = itemView.findViewById(R.id.tv_latitude)
        private val longitude: TextView = itemView.findViewById(R.id.tv_longitude)
        private val preciseCoordinates: TextView = itemView.findViewById(R.id.tv_precise_coordinates)
        private val populationStatus: TextView = itemView.findViewById(R.id.tv_population_status)
        private val deleteButton: CardView = itemView.findViewById(R.id.btn_delete)
        private val viewMapsButton: CardView = itemView.findViewById(R.id.btn_view_maps)

        fun bind(lokasi: Lokasi) {
            placeName.text = lokasi.lokasiName
            placeId.text = "ID: ${lokasi.idLokasi}"
            description.text = lokasi.description
            latitude.text = lokasi.latitude.toString()
            longitude.text = lokasi.longitude.toString()
            populationStatus.text = "Population: ${lokasi.initialTkPopulation ?: 0}"

            // Load precise location using reverse geocoding
            loadPreciseLocation(lokasi.latitude, lokasi.longitude)

            deleteButton.setOnClickListener { onDeleteClick(lokasi) }
            viewMapsButton.setOnClickListener {
                openGoogleEarth(lokasi.latitude, lokasi.longitude, lokasi.lokasiName)
            }
        }

        private fun loadPreciseLocation(lat: Double, lng: Double) {
            val cacheKey = "$lat,$lng"

            // Check cache first
            addressCache[cacheKey]?.let { cachedAddress ->
                preciseCoordinates.text = cachedAddress
                return
            }

            preciseCoordinates.text = "Loading precise location..."

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiKey = context.getString(R.string.google_maps_key)
                    val response = geocodingService.reverseGeocode("$lat,$lng", apiKey)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val geocodingResponse = response.body()!!

                            if (geocodingResponse.results.isNotEmpty()) {
                                val address = geocodingResponse.results[0].formatted_address

                                // Extract meaningful location info
                                val shortAddress = extractShortAddress(address)

                                // Cache the result
                                addressCache[cacheKey] = shortAddress
                                preciseCoordinates.text = shortAddress
                            } else {
                                preciseCoordinates.text = "Location not found"
                            }
                        } else {
                            preciseCoordinates.text = "Unable to load location"
                            Log.e("LokasiAdapter", "Geocoding API error: ${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        preciseCoordinates.text = "Location unavailable"
                        Log.e("LokasiAdapter", "Error loading precise location", e)
                    }
                }
            }
        }

        private fun extractShortAddress(fullAddress: String): String {
            // Extract meaningful parts of the address
            val parts = fullAddress.split(", ")
            return when {
                parts.size >= 3 -> {
                    // Take first 2-3 meaningful parts
                    "${parts[0]}, ${parts[1]}"
                }
                parts.size >= 2 -> {
                    "${parts[0]}, ${parts[1]}"
                }
                else -> fullAddress
            }.take(50) // Limit length for UI
        }

        private fun openGoogleEarth(lat: Double, lng: Double, placeName: String) {
            try {
                // Try to open Google Earth first
                val earthIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("google.earth://tour/?latitude=$lat&longitude=$lng&altitude=1000")
                    setPackage("com.google.earth")
                }

                if (earthIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(earthIntent)
                } else {
                    // Fallback to Google Maps with satellite view
                    openGoogleMapsSatellite(lat, lng, placeName)
                }
            } catch (e: Exception) {
                // Final fallback to regular Google Maps
                openGoogleMaps(lat, lng, placeName)
            }
        }

        private fun openGoogleMapsSatellite(lat: Double, lng: Double, placeName: String) {
            try {
                // Open Google Maps with satellite view and 3D tilt
                val mapsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("geo:$lat,$lng?q=$lat,$lng($placeName)&z=18&t=k")
                    setPackage("com.google.android.apps.maps")
                }

                if (mapsIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapsIntent)
                } else {
                    // If Google Maps not installed, open in browser
                    openInBrowser(lat, lng, placeName)
                }
            } catch (e: Exception) {
                openInBrowser(lat, lng, placeName)
            }
        }

        private fun openGoogleMaps(lat: Double, lng: Double, placeName: String) {
            try {
                val mapsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("geo:$lat,$lng?q=$lat,$lng($placeName)")
                }
                context.startActivity(mapsIntent)
            } catch (e: Exception) {
                openInBrowser(lat, lng, placeName)
            }
        }

        private fun openInBrowser(lat: Double, lng: Double, placeName: String) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.google.com/maps/@$lat,$lng,18z/data=!3m1!1e3")
                }
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
                Log.e("LokasiAdapter", "Error opening maps", e)
            }
        }
    }

    class LokasiDiffCallback : DiffUtil.ItemCallback<Lokasi>() {
        override fun areItemsTheSame(oldItem: Lokasi, newItem: Lokasi): Boolean {
            return oldItem.idLokasi == newItem.idLokasi
        }

        override fun areContentsTheSame(oldItem: Lokasi, newItem: Lokasi): Boolean {
            return oldItem == newItem
        }
    }
}