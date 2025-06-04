package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class EditCoralFragment : Fragment() {
    private lateinit var backButton: ImageButton
    private lateinit var coralNameEditText: TextInputEditText
    private lateinit var coralSpeciesEditText: TextInputEditText
    private lateinit var coralDescriptionEditText: TextInputEditText
    private lateinit var stockEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var updateButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var currentImageView: ImageView
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    private var currentCoral: Coral? = null
    private var coralId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_coral, container, false)
        initDependencies()
        initViews(view)
        getArgumentsData()
        return view
    }

    private fun initDependencies() {
        sessionManager = SessionManager(requireContext())
        apiService = RetrofitClient.instance
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        coralNameEditText = view.findViewById(R.id.coralNameEditText)
        coralSpeciesEditText = view.findViewById(R.id.coralSpeciesEditText)
        coralDescriptionEditText = view.findViewById(R.id.descriptionEditText)
        stockEditText = view.findViewById(R.id.stockEditText)
        priceEditText = view.findViewById(R.id.priceEditText)
        updateButton = view.findViewById(R.id.updateButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        currentImageView = view.findViewById(R.id.currentImageView)
    }

    private fun getArgumentsData() {
        arguments?.let { bundle ->
            coralId = bundle.getInt("coral_id", -1)
            if (coralId != -1) {
                fetchCoralData()
            } else {
                Toast.makeText(requireContext(), "Invalid coral data", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        cancelButton.setOnClickListener {
            showCancelDialog()
        }

        updateButton.setOnClickListener {
            updateCoral()
        }

    }

    private fun fetchCoralData() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken().toString()
                val response = apiService.getSingleTerumbuKarang(coralId, token)

                if (response.isSuccessful) {
                    response.body()?.let { singleCoralResponse ->
                        currentCoral = singleCoralResponse.corral
                        populateFields(singleCoralResponse.corral)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load coral data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun populateFields(coral: Coral) {
        coralNameEditText.setText(coral.tk_name)
        coralSpeciesEditText.setText(coral.tk_jenis)
        priceEditText.setText(coral.harga_tk.toString())
        stockEditText.setText(coral.stok_tersedia.toString())
        coralDescriptionEditText.setText(coral.description)

        // Load image using Glide
        loadCoralImage(coral.img_path)
    }

    private fun loadCoralImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_image_placeholder) // placeholder saat loading
                        .error(R.drawable.ic_image_error) // gambar error jika gagal load
                        .fitCenter() // Menggunakan fitCenter agar gambar tampil utuh
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(currentImageView)
        } else {
            // Set default image jika URL kosong
            currentImageView.setImageResource(R.drawable.ic_image_placeholder)
        }
    }

    private fun updateCoral() {
        if (!validateFields()) return

        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken().toString()
                val editRequest = EditCoralRequest(
                    name = coralNameEditText.text.toString().trim(),
                    jenis = coralSpeciesEditText.text.toString().trim(),
                    harga = priceEditText.text.toString().toIntOrNull(),
                    stok = stockEditText.text.toString().toIntOrNull(),
                    description = coralDescriptionEditText.text.toString().trim()
                )

                val response = apiService.editTerumbuKarang(coralId, token, editRequest)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Coral updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to update coral", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (coralNameEditText.text.toString().trim().isEmpty()) {
            coralNameEditText.error = "Coral name is required"
            isValid = false
        }

        if (coralSpeciesEditText.text.toString().trim().isEmpty()) {
            coralSpeciesEditText.error = "Coral species is required"
            isValid = false
        }

        if (priceEditText.text.toString().trim().isEmpty()) {
            priceEditText.error = "Price is required"
            isValid = false
        }

        if (stockEditText.text.toString().trim().isEmpty()) {
            stockEditText.error = "Stock is required"
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        updateButton.isEnabled = !show
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Edit")
            .setMessage("Are you sure you want to cancel? All changes will be lost.")
            .setPositiveButton("Yes") { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton("No", null)
            .show()
    }
}