package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDoMissionBinding
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel

class WorkerDoMissionFragment : Fragment() {

    private var _binding: FragmentWorkerDoMissionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkerPlantingViewModel by activityViewModels()
    private val args: WorkerDoMissionFragmentArgs by navArgs()

    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivCoralPreview.setImageURI(it)
            binding.ivCoralPreview.isVisible = true
            binding.tvNoImage.isVisible = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_worker_do_mission, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvGreeting.text = "Mission #${args.plantingId}"
        setupListeners()
        observeViewModel()
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
            // Note: The original code asks for an image but the refactored ViewModel/Repo
            // does not upload it. This logic is preserved as per the provided files.
            viewModel.finishPlanting(args.plantingId)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            state.errorMessage?.let {
                showToast("Error: $it")
                viewModel.clearErrorMessage()
            }
        }

        viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { viewEvent ->
                when (viewEvent) {
                    is WorkerPlantingViewModel.ViewEvent.ShowToast -> showToast(viewEvent.message)
                    is WorkerPlantingViewModel.ViewEvent.NavigateBack -> {
                        // Navigate back to the main dashboard, not just one step
                        findNavController().popBackStack(R.id.workerDashboardFragment, false)
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