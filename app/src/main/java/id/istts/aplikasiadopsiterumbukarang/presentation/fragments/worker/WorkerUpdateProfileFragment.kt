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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerUpdateProfileBinding
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.worker.UserRepository
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.EditProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.EditProfileViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerProfileViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class WorkerUpdateProfileFragment : Fragment() {

    private var _binding: FragmentWorkerUpdateProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WorkerProfileViewModel
    private lateinit var editProfileViewModel: EditProfileViewModel
    private lateinit var sessionManager: SessionManager
    private var selectedImageFile: File? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleImageSelection(uri) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_worker_update_profile, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupViewModels()

        binding.profileViewModel = viewModel
        binding.editViewModel = editProfileViewModel

        setupObservers()
        setupClickListeners()
    }

    private fun setupViewModels() {
        val repository = UserRepository(RetrofitClient.instance)
        val sessionManager = SessionManager(requireContext())
        val factory = WorkerProfileViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[WorkerProfileViewModel::class.java]

        val editProfileFactory = EditProfileViewModelFactory(repository)
        editProfileViewModel = ViewModelProvider(this, editProfileFactory)[EditProfileViewModel::class.java]
    }

    private fun setupObservers() {
        // Data is now directly bound to the views. We just observe for events.
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        editProfileViewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.isSuccess) {
                val message = if (state.photoUpdated) {
                    "Profile and photo updated successfully"
                } else {
                    state.successMessage ?: "Profile updated successfully"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

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

            state.error?.let {
                val errorMessage = parseErrorMessage(it)
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                editProfileViewModel.clearState()
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
            openImagePicker()
        }

        binding.ivProfileImage.setOnClickListener {
            openImagePicker()
        }

        binding.etDateOfBirth.setOnClickListener {
            Toast.makeText(requireContext(), "Date picker functionality", Toast.LENGTH_SHORT).show()
        }
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
        // Read data directly from ViewModel fields, which are updated via two-way databinding
        val name = viewModel.name.value?.trim() ?: ""
        val email = viewModel.email.value?.trim() ?: ""
        val phoneNumber = viewModel.phone.value?.trim() ?: ""

        if (!validateInput(name, email, phoneNumber)) {
            return
        }

        val currentProfile = viewModel.workerProfile.value
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
        // It seems there was a typo in the original file's navigation action.
        // Assuming it should navigate back to the main profile view.
        // If your navigation graph has a different action name, replace it here.
        findNavController().popBackStack()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up temporary files
        selectedImageFile?.delete()
        _binding = null
    }
}