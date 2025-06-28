package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.istts.aplikasiadopsiterumbukarang.R // ADDED: This import is required
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDoMissionBinding
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class WorkerDoMissionFragment : Fragment() {

    private var _binding: FragmentWorkerDoMissionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkerPlantingViewModel by activityViewModels()
    private val args: WorkerDoMissionFragmentArgs by navArgs()

    private var imageUri: Uri? = null
    private lateinit var sessionManager: SessionManager

    private fun setupSessionManager() {
        sessionManager = SessionManager(requireContext())
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivCoralPreview.setImageURI(it)
            binding.ivCoralPreview.isVisible = true
            binding.tvNoImage.isVisible = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkerDoMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
        setupSessionManager()
        Log.d("session manager",sessionManager.fetchUserId().toString())
    }

    private fun setupListeners() {
        binding.btnUploadCoral.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDone.setOnClickListener {
            if (imageUri == null) {
                showToast("Please upload a picture of the planted coral.")
                return@setOnClickListener
            }

            viewModel.finishPlanting(args.plantingId)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state.isLoading
                        binding.btnDone.isEnabled = !state.isLoading
                        binding.tvGreeting.text = "Mission #${args.plantingId}"

                        state.errorMessage?.let {
                            showToast("Error: $it")
                            viewModel.clearErrorMessage()
                        }
                    }
                }
                launch {
                    viewModel.eventFlow.collect { event ->
                        when (event) {
                            is WorkerPlantingViewModel.ViewEvent.ShowToast -> showToast(event.message)
                            is WorkerPlantingViewModel.ViewEvent.NavigateBack -> {
                                findNavController().popBackStack(R.id.workerDashboardFragment, false)
                            }
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