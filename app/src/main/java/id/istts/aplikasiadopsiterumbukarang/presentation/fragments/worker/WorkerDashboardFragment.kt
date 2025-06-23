package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlanting
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SharedPreferenceManager
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class WorkerDashboardFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    // ViewModel initialization using RetrofitClient and SharedPreferences
    private val plantingViewModel: WorkerPlantingViewModel by viewModels {
        val sharedPrefs = requireContext().getSharedPreferences("worker_prefs", android.content.Context.MODE_PRIVATE)
        val sessionManager = SessionManager(requireContext()) // Use SessionManager, not SharedPreferenceManager
        WorkerPlantingViewModelFactory(
            WorkerPlantingRepository<Any?>(RetrofitClient.instance, sharedPrefs), // Only 2 parameters
            sessionManager // Pass SessionManager to factory
        )
    }

    private var currentPhotoPath: String = ""
    private var currentPlantingId: Int = 0

    // Camera launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageFile = File(currentPhotoPath)
            if (imageFile.exists() && currentPlantingId > 0) {
                completePlanting(imageFile)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSharedPreferences()
        setupUI()
        setupObservers()
        setupClickListeners()
        setupCardClickListener()

        // Fetch pending plantings when view is created
        plantingViewModel.loadPendingPlantings()
    }

    private fun setupSharedPreferences() {
        sharedPreferenceManager = SharedPreferenceManager(requireContext())
    }

    private fun setupUI() {
        // Set greeting with worker name
        val workerName = sharedPreferenceManager.getWorkerName() ?: "DIVER"
        binding.tvGreeting.text = "HI, $workerName"

        // Initial mission card state
        updateMissionCardState(false, 0)
    }

    private fun setupObservers() {
        // Observe pending plantings
        plantingViewModel.pendingPlantings.observe(viewLifecycleOwner) { plantings ->
            if (plantings != null && plantings.isNotEmpty()) {
                // Show mission available
                updateMissionCardState(true, plantings.size)
            } else {
                // No missions available (either null or empty)
                updateMissionCardState(false, 0)
            }
        }

        // Observe loading state
        plantingViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        // Observe errors
        plantingViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun setupClickListeners() {
        // Logout button
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        // Bottom navigation - Using your existing menu item IDs
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mission -> {
                    // Already on dashboard (mission screen)
                    true
                }
                R.id.navigation_profile -> {
                    // Navigate to profile fragment
                    findNavController().navigate(R.id.action_workerDashboard_to_workerDashboardProfile)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupCardClickListener() {
        binding.cardMission.setOnClickListener {
            plantingViewModel.pendingPlantings.value?.let { plantings ->
                if (plantings.isNotEmpty()) {
                    // If there's only one mission, navigate directly to detail
                    if (plantings.size == 1) {
                        val missionId = plantings[0].id_htrans
                        navigateToWorkerDetailMission(missionId)
                    } else {
                        // If multiple missions, show selection dialog first
                        showPlantingSelectionDialogForDetail(plantings)
                    }
                } else {
                    // No missions available
                    Toast.makeText(requireContext(), "No missions available", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                // If plantings data is null, try to load it
                plantingViewModel.loadPendingPlantings()
                Toast.makeText(requireContext(), "Loading missions...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPlantingSelectionDialogForDetail(plantings: List<PendingPlanting>) {
        val plantingItems = plantings.map { planting ->
            "Mission #${planting.id_htrans}\n" +
                    "Buyer: ${planting.nama_pembeli}\n" +
                    "Location: ${planting.lokasi_penanaman}\n" +
                    "Total: Rp ${NumberFormat.getInstance().format(planting.total_harga.toLong())}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Mission to View Details")
            .setItems(plantingItems) { _, which ->
                val selectedPlanting = plantings[which]
                navigateToWorkerDetailMission(selectedPlanting.id_htrans)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToWorkerDetailMission(missionId: Int) {
        // Create bundle with mission ID matching the navigation argument
        val bundle = Bundle().apply {
            putInt("mission_id", missionId)
        }

        // Navigate to worker detail mission fragment
        findNavController().navigate(R.id.action_workerDashboard_to_workerDetailMission, bundle)
    }

    private fun updateMissionCardState(hasPlantings: Boolean, plantingCount: Int) {
        if (hasPlantings) {
            binding.tvMissionTitle.text = "CORAL PLANTINGS!"
            binding.cardMission.isClickable = true
            binding.cardMission.isFocusable = true
            binding.cardMission.alpha = 1.0f
        } else {
            binding.tvMissionTitle.text = "NO PLANTINGS"
            binding.cardMission.isClickable = false
            binding.cardMission.isFocusable = false
            binding.cardMission.alpha = 0.6f
        }
    }

    private fun showPlantingSelectionDialog(plantings: List<PendingPlanting>) {
        val plantingItems = plantings.map { planting ->
            "Planting #${planting.id_htrans}\n" +
                    "Buyer: ${planting.nama_pembeli}\n" +
                    "Location: ${planting.lokasi_penanaman}\n" +
                    "Total: Rp ${NumberFormat.getInstance().format(planting.total_harga.toLong())}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Coral Planting to Complete")
            .setItems(plantingItems) { _, which ->
                val selectedPlanting = plantings[which]
                currentPlantingId = selectedPlanting.id_htrans
                showPlantingCompletionOptions(selectedPlanting)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlantingCompletionOptions(planting: PendingPlanting) {
        val message = buildString {
            append("Complete coral planting for:\n\n")
            append("Buyer: ${planting.nama_pembeli}\n")
            append("Email: ${planting.email_pembeli}\n")
            append("Location: ${planting.lokasi_penanaman}\n")
            append("Coordinates: ${planting.koordinat}\n")
            append("Corals: ${planting.ringkasan_coral}\n")
            append("Total Types: ${planting.jumlah_jenis_coral}\n")
            append("Total: Rp ${NumberFormat.getInstance().format(planting.total_harga.toLong())}\n\n")
            append("Take a photo to confirm completion.")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Complete Planting #${planting.id_htrans}")
            .setMessage(message)
            .setPositiveButton("Take Photo") { _, _ ->
                takePhoto()
            }
            .setNeutralButton("View Details") { _, _ ->
                navigateToPlantingDetails(planting.id_htrans)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToPlantingDetails(plantingId: Int) {
        val bundle = Bundle().apply {
            putInt("planting_id", plantingId)
        }
        findNavController().navigate(R.id.action_workerDashboard_to_workerDetailMission, bundle)
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                showError("Error creating image file")
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }
        } else {
            showError("Camera not available")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "CORAL_PLANTING_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun completePlanting(imageFile: File) {
        if (currentPlantingId > 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Completion")
                .setMessage("Are you sure this coral planting has been completed successfully?")
                .setPositiveButton("Yes, Complete") { _, _ ->
                    val workerId = sharedPreferenceManager.getWorkerId() ?: 0
                    plantingViewModel.finishPlanting(currentPlantingId, workerId)
                    currentPlantingId = 0
                    showSuccess("Coral planting completed successfully! 🐠")
                    plantingViewModel.loadPendingPlantings()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            sharedPreferenceManager.clearUserSession()
            findNavController().navigate(R.id.action_workerDashboard_to_login)
        }
    }

    private fun showLoading() {
        binding.cardMission.alpha = 0.5f
    }

    private fun hideLoading() {
        binding.cardMission.alpha = 1.0f
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}