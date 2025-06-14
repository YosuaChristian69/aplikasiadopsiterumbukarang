package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAddWorkerBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AddWorkerViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils

class AddWorkerFragment : Fragment() {
    private var _binding: FragmentAddWorkerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var fileUtils: FileUtils

    // Initialize ViewModel
    private val viewModel: AddWorkerViewModel by viewModels()

    // Image handling variables
    private var selectedImageUri: Uri? = null

    // Modern Photo Picker (Android 13+)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            loadImageIntoView(uri)
        }
    }

    // Fallback Gallery launcher for older Android versions
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImageIntoView(uri)
            }
        }
    }

    // Camera permission request
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera launcher
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { bitmap ->
                // Convert bitmap to URI using FileUtils
                selectedImageUri = fileUtils.saveBitmapToFile(bitmap)
                selectedImageUri?.let { uri ->
                    loadImageIntoView(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWorkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        fileUtils = FileUtils(requireContext())
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.selectImageButton.setOnClickListener {
            showImagePickerDialog()
        }

        binding.profileImageCard.setOnClickListener {
            showImagePickerDialog()
        }

        binding.addWorkerButton.setOnClickListener {
            if (validateInputs()) {
                addWorker()
            }
        }

        binding.cancelButton.setOnClickListener {
            clearInputs()
            findNavController().navigateUp()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGalleryModern()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(intent)
    }

    private fun openGalleryModern() {
        // Use modern photo picker for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            // Fallback to legacy method for older versions
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }
    }

    private fun loadImageIntoView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .placeholder(R.drawable.ic_person_placeholder)
            .error(R.drawable.ic_person_placeholder)
            .into(binding.profileImageView)

        // Update button text
        binding.selectImageButton.text = "Change Photo"
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoadingState(isLoading)
        }

        // Observe add worker result
        viewModel.addWorkerResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { message ->
                    showSuccessDialog(message)
                },
                onFailure = { exception ->
                    showError(exception.message ?: "An error occurred")
                }
            )
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Reset previous errors
        binding.fullNameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null

        var isValid = true

        // Validate full name
        if (fullName.isEmpty()) {
            binding.fullNameInputLayout.error = "Full name is required"
            isValid = false
        }

        // Validate email
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Please enter a valid email address"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    private fun addWorker() {
        val fullName = binding.fullNameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        val request = AddWorkerRequest(
            name = fullName,
            email = email,
            password = password
        )

        // Get token from session manager
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            showError("Authentication token not found. Please login again.")
            return
        }

        // Debug logs
        Log.d("AddWorkerFragment", "Adding worker: $fullName, $email")
        Log.d("AddWorkerFragment", "Selected image URI: $selectedImageUri")
        Log.d("AddWorkerFragment", "Has image: ${selectedImageUri != null}")

        // Call ViewModel to add worker with image and context
        viewModel.addWorker(token, request, selectedImageUri, requireContext())
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.addWorkerButton.isEnabled = !isLoading
        binding.cancelButton.isEnabled = !isLoading
        binding.selectImageButton.isEnabled = !isLoading
        binding.profileImageCard.isClickable = !isLoading
        binding.fullNameEditText.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading

        if (isLoading) {
            binding.addWorkerButton.text = "Adding..."
        } else {
            binding.addWorkerButton.text = "ADD WORKER"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                clearInputs()
                findNavController().navigate(R.id.action_addWorkerFragment_to_adminWorkerDashboardFragment)
            }
            .setCancelable(false)
            .show()
    }

    private fun clearInputs() {
        binding.fullNameEditText.text?.clear()
        binding.emailEditText.text?.clear()
        binding.passwordEditText.text?.clear()

        binding.fullNameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null

        // Reset profile image
        selectedImageUri = null
        binding.profileImageView.setImageResource(R.drawable.ic_person_placeholder)
        binding.selectImageButton.text = "Select Photo"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}