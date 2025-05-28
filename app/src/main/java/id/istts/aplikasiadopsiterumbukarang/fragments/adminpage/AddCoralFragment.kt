package id.istts.aplikasiadopsiterumbukarang.fragments.adminpage

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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddCoralFragment : Fragment() {

    // UI Components
    private lateinit var backButton: ImageButton
    private lateinit var coralNameEditText: TextInputEditText
    private lateinit var coralTypeEditText: TextInputEditText
    private lateinit var coralDescriptionEditText: TextInputEditText
    private lateinit var coralNameInput: TextInputLayout
    private lateinit var coralTypeInput: TextInputLayout
    private lateinit var coralDescriptionInput: TextInputLayout
    private lateinit var uploadImageButton: MaterialButton
    private lateinit var imagePreview: ImageView
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private lateinit var sessionManager: SessionManager
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

    // Legacy gallery picker for older Android versions
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
                selectedImageUri = saveBitmapToFile(bitmap)
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
        initViews(view)
        return view
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

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

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            Log.e("AddCoralFragment", "Error saving bitmap", e)
            null
        }
    }

    private fun validateInputs(): Boolean {
        val name = coralNameEditText.text.toString().trim()
        val type = coralTypeEditText.text.toString().trim()
        val description = coralDescriptionEditText.text.toString().trim()

        when {
            name.isEmpty() -> {
                coralNameInput.error = "Coral name is required"
                return false
            }
            type.isEmpty() -> {
                coralTypeInput.error = "Coral species is required"
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
        val description = coralDescriptionEditText.text.toString().trim()

        // For this example, using default values for harga and stok
        // You might want to add these fields to your form
        val harga = "100000" // Default price
        val stok = "10" // Default stock

        selectedImageUri?.let { uri ->
            try {
                val file = getFileFromUri(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("profile_picture", file.name, requestFile)

                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val jenisBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
                val hargaBody = harga.toRequestBody("text/plain".toMediaTypeOrNull())
                val stokBody = stok.toRequestBody("text/plain".toMediaTypeOrNull())

                setLoading(true)

                RetrofitClient.instance.addTerumbuKarang(
                    token = token,
                    name = nameBody,
                    jenis = jenisBody,
                    harga = hargaBody,
                    stok = stokBody,
                    profile_picture = body
                ).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        setLoading(false)
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Coral added successfully!", Toast.LENGTH_SHORT).show()
                            clearForm()
                            findNavController().navigateUp()
                        } else {
                            val errorMessage = when (response.code()) {
                                400 -> "Invalid data or unauthorized access"
                                401 -> "Session expired. Please login again."
                                else -> "Failed to add coral. Error: ${response.code()}"
                            }
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        setLoading(false)
                        Log.e("AddCoralFragment", "Network error", t)
                        Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })

            } catch (e: Exception) {
                setLoading(false)
                Log.e("AddCoralFragment", "Error preparing file", e)
                Toast.makeText(requireContext(), "Error preparing image file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "coral_image_${System.currentTimeMillis()}.png")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return file
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