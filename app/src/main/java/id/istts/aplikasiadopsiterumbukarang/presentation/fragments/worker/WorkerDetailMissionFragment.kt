package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDetailMissionBinding
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel

class WorkerDetailMissionFragment : Fragment() {

    private var _binding: FragmentWorkerDetailMissionBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: WorkerPlantingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDetailMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()

        // Get mission_id from arguments manually
        val missionId = arguments?.getInt("mission_id", 0) ?: 0
        if (missionId != 0) {
            viewModel.loadPlantingDetails(missionId)
        }
    }

    private fun setupObservers() {
        // Observe UI state
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            // Update greeting message
            binding.tvGreeting.text = uiState.welcomeMessage ?: "Hi, Worker"
        }

        // Observe planting detail
        viewModel.plantingDetail.observe(viewLifecycleOwner) { plantingDetail ->
            plantingDetail?.let { detail ->
                // Update UI with planting details
                // Get the first coral detail if available, or show default values
                val firstCoral = detail.detail_coral.firstOrNull()

                binding.tvCoralNameTitle.text = firstCoral?.nama_coral ?: "Coral Name"
                binding.tvCoralSpecies.text = firstCoral?.jenis ?: "CORAL Species"
                binding.tvOwnerName.text = detail.pembeli.nama ?: "Owner"

                // Optional: If you want to show additional information
                // binding.tvPlantingStatus.text = detail.status_penanaman
                // binding.tvPurchaseDate.text = detail.tanggal_pembelian
                // binding.tvTotalPrice.text = "Rp ${detail.total_harga}"
                // binding.tvPlantingLocation.text = detail.lokasi_penanaman.nama_lokasi

                // If you have multiple corals and want to show them all:
                // val coralNames = detail.detail_coral.joinToString(", ") { it.nama_coral }
                // binding.tvCoralNameTitle.text = coralNames

                // Load coral image if available
                // binding.ivCoralImage.load(detail.coralImageUrl)
            }
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state - you can show/hide progress bar here
            binding.btnNext.isEnabled = !isLoading
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Show error message - you can use Snackbar or Toast
                // Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Observe navigation to login
        viewModel.shouldNavigateToLogin.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_workerDetailMission_to_workerDashboard)
                viewModel.onNavigatedToLogin()
            }
        }
    }

    private fun setupClickListeners() {
        // Next button - navigate to Do Mission
        binding.btnNext.setOnClickListener {
            val missionId = arguments?.getInt("mission_id", 0) ?: 0
            if (missionId != 0) {
                // Navigate manually using Bundle
                val bundle = Bundle().apply {
                    putInt("planting_id", missionId)
                }
                findNavController().navigate(
                    R.id.action_workerDetailMission_to_workerDoMission,
                    bundle
                )
            }
        }

        // Back button - navigate back to dashboard
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_workerDetailMission_to_workerDashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}