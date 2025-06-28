package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardProfileBinding
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerDashboardProfileFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkerProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_worker_dashboard_profile, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        binding.viewModel = viewModel
        setupObservers()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.instance
        val sessionManager = SessionManager(requireContext())
        val userRepository = UserRepository(apiService)
        // Note: The factory was updated to pass sessionManager to WorkerProfileViewModel
        val factory = WorkerProfileViewModelFactory(userRepository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[WorkerProfileViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_workerProfile_to_workerUpdateProfile)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = binding.bottomNavigation
        bottomNavigation.selectedItemId = R.id.navigation_profile
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mission -> {
                    findNavController().navigate(R.id.action_workerProfile_to_workerDashboard)
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}