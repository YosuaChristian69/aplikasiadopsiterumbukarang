package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardBinding
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.WorkerPlantingRepository
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class WorkerDashboardFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private fun setupSessionManager() {
        sessionManager = SessionManager(requireContext())
    }
    private val viewModel: WorkerPlantingViewModel by activityViewModels {
        WorkerPlantingViewModelFactory(
            WorkerPlantingRepository(RetrofitClient.instance, requireContext().getSharedPreferences("app_prefs", 0)),
            SessionManager(requireContext())
        )
    }

    private lateinit var missionAdapter: MissionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSessionManager()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Explicitly tell the ViewModel to load the data now that the view is ready.
        viewModel.loadPendingPlantings()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter and set what happens when a mission is clicked
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
                R.id.navigation_mission -> {
                    // User is already on the mission screen, do nothing.
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state.isLoading
                        binding.tvGreeting.text = "HI, ${state.userName.uppercase()}"

                        // Submit the list of missions to the adapter
                        missionAdapter.submitList(state.pendingPlantings)

                        // Show/hide the list or the "no missions" text based on data
                        binding.rvMissions.isVisible = state.pendingPlantings.isNotEmpty()
                        binding.tvNoMissions.isVisible = state.pendingPlantings.isEmpty() && !state.isLoading

                        state.errorMessage?.let {
                            showToast("Error: $it")
                            viewModel.clearErrorMessage()
                        }

                        if (state.shouldNavigateToLogin) {
                            navigateToLogin()
                            viewModel.onNavigatedToLogin()
                        }
                    }
                }

                launch {
                    viewModel.eventFlow.collect { event ->
                        when(event) {
                            is WorkerPlantingViewModel.ViewEvent.ShowToast -> showToast(event.message)
                            else -> {} // Other events can be handled here
                        }
                    }
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