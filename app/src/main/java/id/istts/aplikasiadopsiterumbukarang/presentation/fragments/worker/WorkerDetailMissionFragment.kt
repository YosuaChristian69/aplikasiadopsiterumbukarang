package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDetailMissionBinding
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class WorkerDetailMissionFragment : Fragment() {

    private var _binding: FragmentWorkerDetailMissionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkerPlantingViewModel by activityViewModels()
    private val args: WorkerDetailMissionFragmentArgs by navArgs()
    private lateinit var sessionManager: SessionManager

    private fun setupSessionManager() {
        sessionManager = SessionManager(requireContext())
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkerDetailMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadPlantingDetails(args.missionId)
        setupSessionManager()
        Log.d("session manager",sessionManager.fetchUserEmail().toString())
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            val action = WorkerDetailMissionFragmentDirections.actionWorkerDetailMissionToWorkerDoMission(args.missionId)
            findNavController().navigate(action)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // The reference to progressBar has been removed as per your request.
                    // You can manage the loading state differently, e.g., by disabling buttons.
                    binding.btnNext.isEnabled = !state.isLoading
                    binding.btnBack.isEnabled = !state.isLoading

                    binding.tvGreeting.text = "Hi, ${state.userName.uppercase()}"

                    state.selectedPlanting?.let { detail ->
                        val firstCoral = detail.detail_coral.firstOrNull()
                        binding.tvCoralNameTitle.text = firstCoral?.nama_coral ?: "N/A"
                        binding.tvCoralSpecies.text = firstCoral?.jenis ?: "N/A"
                        binding.tvOwnerName.text = detail.pembeli.nama
                    }

                    state.errorMessage?.let {
                        showToast("Error: $it")
                        viewModel.clearErrorMessage()
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}