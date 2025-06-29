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
import com.bumptech.glide.Glide // ADDED: Import Glide
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

    // In WorkerDetailMissionFragment.kt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sambungkan ViewModel dan LifecycleOwner ke binding
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner // <-- INI SANGAT PENTING

        viewModel.loadPlantingDetails(args.missionId)
        setupSessionManager()
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

    // In WorkerDetailMissionFragment.kt

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observer untuk event satu kali seperti Toast atau Navigasi
                launch {
                    viewModel.eventFlow.collect { event ->
                        when (event) {
                            is WorkerPlantingViewModel.ViewEvent.ShowToast -> {
                                showToast(event.message)
                            }
                            is WorkerPlantingViewModel.ViewEvent.NavigateBack -> {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }

                // Observer untuk state UI yang tidak bisa di-handle di XML
                launch {
                    viewModel.uiState.collect { state ->
                        // HAPUS SEMUA KODE MANUAL UNTUK SET TEXT DAN IMAGE DARI SINI
                        // Biarkan hanya logika yang tidak terkait langsung dengan data,
                        // seperti menampilkan error message.

                        state.errorMessage?.let {
                            showToast("Error: $it")
                            viewModel.clearErrorMessage()
                        }
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