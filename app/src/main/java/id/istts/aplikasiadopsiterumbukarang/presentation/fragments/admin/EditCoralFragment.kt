package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModelFactory

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
    private lateinit var viewModel: EditCoralViewModel

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
        val repository = CoralRepositoryImpl()
        val factory = EditCoralViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EditCoralViewModel::class.java]
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
        observeViewModel()
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

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.coral.observe(viewLifecycleOwner) { coral ->
            populateFields(coral)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Coral updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun fetchCoralData() {
        val token = sessionManager.fetchAuthToken().toString()
        viewModel.fetchCoralData(coralId, token)
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
        val name = coralNameEditText.text.toString()
        val species = coralSpeciesEditText.text.toString()
        val price = priceEditText.text.toString()
        val stock = stockEditText.text.toString()
        val description = coralDescriptionEditText.text.toString()

        // Validate fields using ViewModel
        val (isValid, errors) = viewModel.validateFields(name, species, price, stock)

        if (!isValid) {
            // Show validation errors
            errors["name"]?.let { coralNameEditText.error = it }
            errors["species"]?.let { coralSpeciesEditText.error = it }
            errors["price"]?.let { priceEditText.error = it }
            errors["stock"]?.let { stockEditText.error = it }
            return
        }

        // Clear previous errors
        coralNameEditText.error = null
        coralSpeciesEditText.error = null
        priceEditText.error = null
        stockEditText.error = null

        val editRequest = EditCoralRequest(
            name = name.trim(),
            jenis = species.trim(),
            harga = price.toInt(),
            stok = stock.toInt(),
            description = description.trim()
        )

        val token = sessionManager.fetchAuthToken().toString()
        viewModel.updateCoral(coralId, token, editRequest)
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