package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCase
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class AddCoralFragment : Fragment() {

    // UI Components
    private lateinit var backButton: ImageButton
    private lateinit var coralNameEditText: TextInputEditText
    private lateinit var coralTypeEditText: TextInputEditText
    private lateinit var coralDescriptionEditText: TextInputEditText
    private lateinit var coralTotalEditText: EditText
    private lateinit var coralhargaEditText: EditText
    private lateinit var coralNameInput: TextInputLayout
    private lateinit var coralTypeInput: TextInputLayout
    private lateinit var coralDescriptionInput: TextInputLayout
    private lateinit var uploadImageButton: MaterialButton
    private lateinit var imagePreview: ImageView
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    // Dependencies
    private lateinit var sessionManager: SessionManager
    private lateinit var addCoralUseCase: AddCoralUseCase
    private lateinit var fileUtils: FileUtils

    private var selectedImageUri: Uri? = null
    private var isLoading = false

    // Modern Photo Picker (Android 13+) and fallback
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imagePreview.setImageURI(uri)
            imagePreview.visibility = View.VISIBLE
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imagePreview.setImageURI(uri)
                imagePreview.visibility = View.VISIBLE
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

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { bitmap ->
                selectedImageUri = fileUtils.saveBitmapToFile(bitmap)
                imagePreview.setImageBitmap(bitmap)
                imagePreview.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_coral, container, false)
        initDependencies()
        initViews(view)
        return view
    }

    private fun initDependencies() {
        sessionManager = SessionManager(requireContext())
        fileUtils = FileUtils(requireContext())
        val coralRepository = CoralRepositoryImpl()
        addCoralUseCase = AddCoralUseCase(coralRepository, fileUtils)
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        coralNameEditText = view.findViewById(R.id.coralNameEditText)
        coralTypeEditText = view.findViewById(R.id.coralTypeEditText)
        coralDescriptionEditText = view.findViewById(R.id.coralDescriptionEditText)
        coralNameInput = view.findViewById(R.id.coralNameInput)
        coralTypeInput = view.findViewById(R.id.coralTypeInput)
        coralDescriptionInput = view.findViewById(R.id.coralDescriptionInput)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        imagePreview = view.findViewById(R.id.imagePreview)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        coralTotalEditText = view.findViewById(R.id.totalEditText)
        coralhargaEditText = view.findViewById(R.id.hargaEditText)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupUI()
    }

    private fun setupUI() {
        imagePreview.visibility = View.GONE
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        uploadImageButton.setOnClickListener {
            showImagePickerDialog()
        }

        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveCoral()
            }
        }

        cancelButton.setOnClickListener {
            showCancelDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGalleryModern()
                }
            }
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
            pickImageLauncher.launch(intent)
        }
    }

    private fun validateInputs(): Boolean {
        val name = coralNameEditText.text.toString().trim()
        val type = coralTypeEditText.text.toString().trim()
        val description = coralDescriptionEditText.text.toString().trim()
        val total = coralTotalEditText.text.toString().trim()
        val harga = coralhargaEditText.text.toString().trim()

        when {
            name.isEmpty() -> {
                coralNameInput.error = "Coral name is required"
                return false
            }
            type.isEmpty() -> {
                coralTypeInput.error = "Coral species is required"
                return false
            }
            total.isEmpty() -> {
                coralTotalEditText.error = "Total is required"
                return false
            }
            harga.isEmpty() -> {
                coralhargaEditText.error = "Harga is required"
                return false
            }
            description.isEmpty() -> {
                coralDescriptionInput.error = "Description is required"
                return false
            }
            selectedImageUri == null -> {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                // Clear errors
                coralNameInput.error = null
                coralTypeInput.error = null
                coralDescriptionInput.error = null
                return true
            }
        }
    }

    private fun saveCoral() {
        if (isLoading) return

        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        val name = coralNameEditText.text.toString().trim()
        val type = coralTypeEditText.text.toString().trim()
        val harga = coralhargaEditText.text.toString().trim()
        val stok = coralTotalEditText.text.toString().trim()

        selectedImageUri?.let { uri ->
            setLoading(true)

            val params = AddCoralUseCase.AddCoralParams(
                token = token,
                name = name,
                jenis = type,
                harga = harga,
                stok = stok,
                imageUri = uri
            )

            lifecycleScope.launch {
                try {
                    val result = addCoralUseCase.execute(params)

                    result.fold(
                        onSuccess = { message ->
                            setLoading(false)
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            clearForm()
                            findNavController().navigateUp()
                        },
                        onFailure = { exception ->
                            setLoading(false)
                            Log.e("AddCoralFragment", "Error adding coral", exception)
                            Toast.makeText(requireContext(), exception.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                        }
                    )
                } catch (e: Exception) {
                    setLoading(false)
                    Log.e("AddCoralFragment", "Unexpected error", e)
                    Toast.makeText(requireContext(), "Error preparing image file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        saveButton.isEnabled = !loading
        saveButton.text = if (loading) "Saving..." else "Save Coral"
    }

    private fun clearForm() {
        coralNameEditText.text?.clear()
        coralTypeEditText.text?.clear()
        coralDescriptionEditText.text?.clear()
        coralhargaEditText.text?.clear()
        coralTotalEditText.text?.clear()
        imagePreview.setImageResource(0)
        imagePreview.visibility = View.GONE
        selectedImageUri = null
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Add Coral")
            .setMessage("Are you sure you want to cancel? All entered data will be lost.")
            .setPositiveButton("Yes") { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton("No", null)
            .show()
    }
}