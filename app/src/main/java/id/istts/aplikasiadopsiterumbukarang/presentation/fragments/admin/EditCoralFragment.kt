package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentEditCoralBinding // <-- IMPORT VIEW BINDING
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager


class EditCoralFragment : Fragment() {
    // Gunakan View Binding untuk menghindari findViewById
    private var _binding: FragmentEditCoralBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditCoralViewModel
    private lateinit var sessionManager: SessionManager
    private var currentCoral: Coral? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCoralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDependencies()
        getParcelableData()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        // Gunakan factory yang sesuai dengan ViewModel baru Anda
        val factory = EditCoralViewModelFactory(CoralRepositoryImpl())
        viewModel = ViewModelProvider(this, factory)[EditCoralViewModel::class.java]
    }

    private fun getParcelableData() {
        // Ambil seluruh objek dari arguments, bukan hanya ID
        currentCoral = arguments?.getParcelable("coral_object")

        if (currentCoral == null) {
            Toast.makeText(requireContext(), "Error: Could not load coral data.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        // Kirim data ke ViewModel dan langsung populate UI
        viewModel.setInitialCoralData(currentCoral!!)
    }

    private fun observeViewModel() {
        // Observer ini sekarang akan langsung terpanggil setelah setInitialCoralData
        viewModel.coral.observe(viewLifecycleOwner) { coral ->
            populateFields(coral)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.isVisible = isLoading
            binding.updateButton.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if(errorMessage.isNotBlank()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Coral updated successfully", Toast.LENGTH_SHORT).show()
                // Kembali ke dashboard dan dashboard akan refresh datanya sendiri
                findNavController().popBackStack()
            }
        }
    }

    private fun populateFields(coral: Coral) {
        binding.coralNameEditText.setText(coral.tk_name)
        binding.coralSpeciesEditText.setText(coral.tk_jenis)
        binding.priceEditText.setText(coral.harga_tk.toString())
        binding.stockEditText.setText(coral.stok_tersedia.toString())
        binding.descriptionEditText.setText(coral.description)

        Glide.with(this)
            .load(coral.img_path)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_error)
            .into(binding.currentImageView)
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.cancelButton.setOnClickListener { showCancelDialog() }
        binding.updateButton.setOnClickListener { updateCoral() }
    }

    private fun updateCoral() {
        val name = binding.coralNameEditText.text.toString()
        val species = binding.coralSpeciesEditText.text.toString()
        val price = binding.priceEditText.text.toString()
        val stock = binding.stockEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()

        val (isValid, errors) = viewModel.validateFields(name, species, price, stock)

        if (!isValid) {
            binding.coralNameEditText.error = errors["name"]
            binding.coralSpeciesEditText.error = errors["species"]
            binding.priceEditText.error = errors["price"]
            binding.stockEditText.error = errors["stock"]
            return
        }

        val editRequest = EditCoralRequest(
            name = name.trim(),
            jenis = species.trim(),
            harga = price.toInt(),
            stok = stock.toInt(),
            description = description.trim()
        )

        val token = sessionManager.fetchAuthToken() ?: ""
        // Pastikan currentCoral tidak null sebelum mengambil ID-nya
        currentCoral?.let {
            viewModel.updateCoral(it.id_tk, token, editRequest)
        }
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Edit")
            .setMessage("Are you sure you want to cancel? All changes will be lost.")
            .setPositiveButton("Yes") { _, _ -> findNavController().popBackStack() }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }
}