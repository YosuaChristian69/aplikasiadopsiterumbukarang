package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerUpdateProfileBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.WorkerProfile
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.EditProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.EditProfileViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class WorkerUpdateProfileFragment : Fragment() {

    private var _binding: FragmentWorkerUpdateProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkerProfileViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var editProfileViewModel: EditProfileViewModel
    private var currentWorkerProfile: WorkerProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerUpdateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSessionManager()
        setupViewModels()
        setupObservers()
        setupClickListeners()
        loadCurrentProfile()
    }

    private fun setupSessionManager() {
        sessionManager = SessionManager(requireContext())
    }

    private fun setupViewModels() {
        // Create UserRepository instance first
        val repository = UserRepository(RetrofitClient.instance)
//        val factory = WorkerProfileViewModelFactory(userRepository, sessionManager)
//        viewModel = ViewModelProvider(this, factory)[WorkerProfileViewModel::class.java]

        // FIXED: Setup WorkerProfileViewModel with UserRepository (not SessionManager)
        val factory = WorkerProfileViewModelFactory(repository,sessionManager) // Changed from sessionManager to repository
        viewModel = ViewModelProvider(this, factory)[WorkerProfileViewModel::class.java]

        // Setup EditProfileViewModel with the same repository
        val editProfileFactory = EditProfileViewModelFactory(repository)
        editProfileViewModel = ViewModelProvider(this, editProfileFactory)[EditProfileViewModel::class.java]
    }

    private fun setupObservers() {
        // Observe worker profile data
        viewModel.workerProfile.observe(viewLifecycleOwner) { profile ->
            currentWorkerProfile = profile
            // Populate the form with current WorkerProfile data
            binding.apply {
                etName.setText(profile.name)
                etEmail.setText(profile.email)
                etDateOfBirth.setText(profile.dateOfBirth)
                etPhoneNumber.setText(profile.phone)
            }
        }

        // Observe loading state from existing viewModel
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnUpdate.isEnabled = !isLoading
        }

        // Observe error messages from existing viewModel
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe edit profile API state
        lifecycleScope.launch {
            editProfileViewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        binding.btnUpdate.isEnabled = false
                        binding.btnUpdate.text = "UPDATING..."
                    }
                    state.isSuccess -> {
                        binding.btnUpdate.isEnabled = true
                        binding.btnUpdate.text = "UPDATE"
                        Toast.makeText(requireContext(),
                            state.successMessage ?: "Profile updated successfully",
                            Toast.LENGTH_SHORT).show()

                        updateSessionManagerWithNewData()
                        viewModel.refreshProfile()
                        navigateBackToProfile()
                        editProfileViewModel.clearState()
                    }
                    state.error != null -> {
                        binding.btnUpdate.isEnabled = true
                        binding.btnUpdate.text = "UPDATE"

                        val errorMessage = parseErrorMessage(state.error)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        editProfileViewModel.clearState()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            navigateBackToProfile()
        }

        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }

        binding.btnUploadProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Upload profile picture functionality", Toast.LENGTH_SHORT).show()
        }

        binding.etDateOfBirth.setOnClickListener {
            Toast.makeText(requireContext(), "Date picker functionality", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCurrentProfile() {
        // This will trigger the observer to populate the UI
        // Make sure to call the method that loads the worker profile
        // For example, if your WorkerProfileViewModel has a loadProfile() method:
        // viewModel.loadProfile()
    }

    private fun updateProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val dateOfBirth = binding.etDateOfBirth.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()

        if (!validateInput(name, email, phoneNumber)) {
            return
        }

        val currentProfile = currentWorkerProfile
        if (currentProfile == null) {
            Toast.makeText(requireContext(), "Profile data not loaded. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val nameChanged = currentProfile.name != name
        val emailChanged = currentProfile.email != email

        val updatedName = if (nameChanged) name else null
        val updatedEmail = if (emailChanged) email else null

        if (updatedName == null && updatedEmail == null) {
            Toast.makeText(requireContext(), "No changes detected", Toast.LENGTH_SHORT).show()
            return
        }

        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Authentication required. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        editProfileViewModel.editProfile(token, updatedEmail, updatedName)
    }

    private fun updateSessionManagerWithNewData() {
        val updatedName = binding.etName.text.toString().trim()
        val updatedEmail = binding.etEmail.text.toString().trim()

        sessionManager.saveUserDetails(updatedName, updatedEmail, sessionManager.fetchUserStatus() ?: "")
    }

    private fun validateInput(name: String, email: String, phoneNumber: String): Boolean {
        var isValid = true

        binding.etName.error = null
        binding.etEmail.error = null
        binding.etPhoneNumber.error = null

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            isValid = false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            if (isValid) binding.etEmail.requestFocus()
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email address"
            if (isValid) binding.etEmail.requestFocus()
            isValid = false
        }

        if (phoneNumber.isEmpty()) {
            binding.etPhoneNumber.error = "Phone number is required"
            if (isValid) binding.etPhoneNumber.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun parseErrorMessage(error: String): String {
        return when {
            error.contains("Token Spoofing") -> "Authentication error. Please login again."
            error.contains("similar email") || error.contains("Email already exists") -> "This email is already in use. Please choose a different email."
            error.contains("Invalid email") -> "Please enter a valid email address."
            error.contains("Unauthorized") -> "Your session has expired. Please login again."
            error.contains("Network") || error.contains("timeout") -> "Network error. Please check your connection and try again."
            else -> error
        }
    }

    private fun navigateBackToProfile() {
        findNavController().navigate(R.id.action_workerUpdateProfile_to_workerProfile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}