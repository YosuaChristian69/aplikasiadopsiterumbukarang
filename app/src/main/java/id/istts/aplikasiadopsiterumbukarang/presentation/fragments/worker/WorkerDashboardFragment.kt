package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardBinding
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerDashboardFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkerPlantingViewModel by activityViewModels()
    private lateinit var missionAdapter: MissionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_worker_dashboard, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        viewModel.loadPendingPlantings()
    }

    private fun setupRecyclerView() {
        missionAdapter = MissionAdapter { mission ->
            navigateToWorkerDetailMission(mission.id_htrans)
        }
        binding.rvMissions.adapter = missionAdapter
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener { showLogoutConfirmation() }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    findNavController().navigate(R.id.action_workerDashboard_to_workerDashboardProfile)
                    true
                }
                R.id.navigation_mission -> true
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.shouldNavigateToLogin) {
                navigateToLogin()
                viewModel.onNavigatedToLogin()
            }
            state.errorMessage?.let {
                showToast("Error: $it")
                viewModel.clearErrorMessage()
            }
        }

        viewModel.viewEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { viewEvent ->
                when(viewEvent) {
                    is WorkerPlantingViewModel.ViewEvent.ShowToast -> showToast(viewEvent.message)
                    else -> {}
                }
            }
        }
    }

    private fun navigateToWorkerDetailMission(missionId: Int) {
        val action = WorkerDashboardFragmentDirections.actionWorkerDashboardToWorkerDetailMission(missionId)
        findNavController().navigate(action)
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                SessionManager(requireContext()).clearSession()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        if (findNavController().currentDestination?.id == R.id.workerDashboardFragment) {
            findNavController().navigate(R.id.action_workerDashboardFragment_to_loginFragment)
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