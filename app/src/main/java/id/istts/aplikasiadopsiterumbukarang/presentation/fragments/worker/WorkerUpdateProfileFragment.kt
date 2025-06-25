package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class WorkerUpdateProfileFragment : Fragment() {

    private var _binding: FragmentWorkerUpdateProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkerProfileViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var editProfileViewModel: EditProfileViewModel
    private var currentWorkerProfile: WorkerProfile? = null
    private var selectedImageFile: File? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

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
        val repository = UserRepository(RetrofitClient.instance)

        val factory = WorkerProfileViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[WorkerProfileViewModel::class.java]

        val editProfileFactory = EditProfileViewModelFactory(repository)
        editProfileViewModel = ViewModelProvider(this, editProfileFactory)[EditProfileViewModel::class.java]
    }

    private fun setupObservers() {
        // Observe worker profile data
        viewModel.workerProfile.observe(viewLifecycleOwner) { profile ->
            currentWorkerProfile = profile
            binding.apply {
                etName.setText(profile.name)
                etEmail.setText(profile.email)
                etDateOfBirth.setText(profile.dateOfBirth)
                etPhoneNumber.setText(profile.phone)

                // Load current profile image - Fixed the property access and Glide usage
                loadProfileImage(profile)
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

        // Observe selected image
        lifecycleScope.launch {
            editProfileViewModel.selectedImageUri.collect { uri ->
                uri?.let {
                    binding.ivProfileImage.setImageURI(it)
                }
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

                        val message = if (state.photoUpdated) {
                            "Profile and photo updated successfully"
                        } else {
                            state.successMessage ?: "Profile updated successfully"
                        }

                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                        // Update session with new data if user data is available
                        state.updatedUser?.let { userData ->
                            sessionManager.updateUserDetailsFromResponse(
                                userData.full_name,
                                userData.email,
                                userData.user_status,
                                userData.img_path
                            )
                        }

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

    private fun loadProfileImage(profile: WorkerProfile) {
        // First try to get image from WorkerProfile (assuming it has an image field)
        // Since profileImageUrl doesn't exist, you need to check what field contains the image
        val imageUrl: String? = when {
            // Check if WorkerProfile has an image field (you'll need to verify this)
            // profile.imageUrl != null -> profile.imageUrl
            // profile.imagePath != null -> profile.imagePath
            // For now, get from session
            else -> sessionManager.fetchUserImagePath()
        }

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl) // Explicitly specify String type
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivProfileImage)
        } else {
            // Set default image
            binding.ivProfileImage.setImageResource(R.drawable.ic_person)
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
            openImagePicker()
        }

        binding.ivProfileImage.setOnClickListener {
            openImagePicker()
        }

        binding.etDateOfBirth.setOnClickListener {
            Toast.makeText(requireContext(), "Date picker functionality", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCurrentProfile() {
        // Load the current profile - implement this based on your WorkerProfileViewModel
        // viewModel.loadProfile()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            editProfileViewModel.selectImage(uri)

            // Convert URI to File for upload
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            inputStream?.let { stream ->
                val file = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)

                stream.copyTo(outputStream)
                stream.close()
                outputStream.close()

                selectedImageFile = file
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error selecting image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
        val hasSelectedImage = selectedImageFile != null

        val updatedName = if (nameChanged) name else null
        val updatedEmail = if (emailChanged) email else null

        if (updatedName == null && updatedEmail == null && !hasSelectedImage) {
            Toast.makeText(requireContext(), "No changes detected", Toast.LENGTH_SHORT).show()
            return
        }

        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Authentication required. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        editProfileViewModel.editProfile(token, updatedEmail, updatedName, selectedImageFile)
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
            error.contains("Error uploading photo") -> "Failed to upload photo. Please try again."
            else -> error
        }
    }

    private fun navigateBackToProfile() {
        findNavController().navigate(R.id.action_workerUpdateProfile_to_workerProfile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up temporary files
        selectedImageFile?.delete()
        _binding = null
    }
}