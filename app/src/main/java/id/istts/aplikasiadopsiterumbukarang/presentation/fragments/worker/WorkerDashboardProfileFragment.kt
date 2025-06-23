package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardProfileBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerProfile
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository

import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerDashboardProfileFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkerProfileViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDashboardProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSessionManager()
        setupViewModel()
        setupObservers()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupSessionManager() {
        sessionManager = SessionManager(requireContext())
    }

    private fun setupViewModel() {
        // Use your existing RetrofitClient to get the ApiService
        val apiService = RetrofitClient.instance

        // Create SessionManager instance
        val sessionManager = SessionManager(requireContext())

        // Create the repository instance with the ApiService
        val userRepository = UserRepository(apiService)

        // Pass sessionManager to the factory
        val factory = WorkerProfileViewModelFactory(userRepository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[WorkerProfileViewModel::class.java]
    }

    private fun setupObservers() {
        // Observe worker profile data
        viewModel.workerProfile.observe(viewLifecycleOwner) { profile ->
            updateUI(profile)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show/hide a progress bar here
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        // Edit profile button - navigate to update profile fragment
        binding.btnEditProfile.setOnClickListener {
            navigateToUpdateProfile()
        }

        // Profile photo is not clickable in view mode since editing is in separate fragment
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = binding.bottomNavigation
        bottomNavigation.selectedItemId = R.id.navigation_profile // Assuming this is the profile item ID

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mission -> {
                    // Navigate to worker dashboard
                    findNavController().navigate(R.id.action_workerProfile_to_workerDashboard)
                    true
                }
                R.id.navigation_profile -> {
                    // Already on profile
                    true
                }
                else -> false
            }
        }
    }

    private fun updateUI(profile: WorkerProfile) {
        binding.apply {
            tvName.text = profile.name
            tvJobTitle.text = profile.jobTitle
            tvDob.text = ": ${profile.dateOfBirth}"
            tvEmail.text = ": ${profile.email}"
            tvPhone.text = ": ${profile.phone}"
            tvJoinedDate.text = ": ${profile.joinedDate}"
            tvWorkerId.text = "ID: ${profile.workerId}"

            // Load profile image
            loadProfileImage(profile.profilePhotoUrl)
        }
    }

    private fun loadProfileImage(imageUrl: String?) {
        Glide.with(this)
            .load(imageUrl ?: R.drawable.ic_person_placeholder) // You'll need a default avatar drawable
            .transform(CircleCrop())
            .placeholder(R.drawable.ic_person_placeholder)
            .error(R.drawable.ic_person_placeholder)
            .into(binding.ivProfilePhoto)
    }

    private fun navigateToUpdateProfile() {
        // Navigate to update profile fragment
        // You'll need to add this action to your navigation graph
        findNavController().navigate(R.id.action_workerProfile_to_workerUpdateProfile)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_workerProfile_to_login)
    }

    private fun navigateToWorkerDashboard() {
        findNavController().navigate(R.id.action_workerProfile_to_workerDashboard)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}