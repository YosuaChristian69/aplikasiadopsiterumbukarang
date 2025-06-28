package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory.ViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.AddCorralViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelWIthRepo.EditCorralViewModelRepo
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addCoral.AddCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.addCoral.AddCoralEvent
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCase
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory.ViewModelFactoryAdd

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

    // ViewModel
    private lateinit var viewModel: AddCorralViewModelRepo
//    private lateinit var viewModel: AddCoralViewModel

    // Modern Photo Picker (Android 13+) and fallback
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setSelectedImageUri(uri)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setSelectedImageUri(uri)
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
                viewModel.setSelectedImageBitmap(bitmap)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_coral, container, false)
        initViewModel()
        initViews(view)
        return view
    }

    private fun initViewModel() {
        // 1. Buat semua dependencies yang dibutuhkan oleh ViewModel
        val sessionManager = SessionManager(requireContext())
        val fileUtils = FileUtils(requireContext())
        val coralRepository = CoralRepositoryImpl() // UseCase butuh ini
        val addCoralUseCase = AddCoralUseCase(coralRepository, fileUtils)

        // 2. Berikan semua dependencies tersebut ke constructor ViewModel
//        viewModel = AddCoralViewModel(
//            sessionManager = sessionManager,
//            fileUtils = fileUtils,
//            addCoralUseCase = addCoralUseCase
//        )
        val factory = ViewModelFactoryAdd(RepostioryCorral(), sessionManager, fileUtils, addCoralUseCase, requireContext())
        viewModel = ViewModelProvider(this, factory).get(AddCorralViewModelRepo::class.java)
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
        observeViewModel()
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
            val name = coralNameEditText.text.toString()
            val type = coralTypeEditText.text.toString()
            val description = coralDescriptionEditText.text.toString()
            val total = coralTotalEditText.text.toString()
            val harga = coralhargaEditText.text.toString()

            if (viewModel.validateInputs(name, type, description, total, harga)) {
                viewModel.saveCoralRepo(name, type, description, total, harga)
            }
        }

        cancelButton.setOnClickListener {
            showCancelDialog()
        }
    }

    private fun observeViewModel() {
        // Observe UI State
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                coralNameInput.error = state.nameError
                coralTypeInput.error = state.typeError
                coralDescriptionInput.error = state.descriptionError

                if (state.totalError != null) {
                    coralTotalEditText.error = state.totalError
                } else {
                    coralTotalEditText.error = null
                }

                if (state.hargaError != null) {
                    coralhargaEditText.error = state.hargaError
                } else {
                    coralhargaEditText.error = null
                }
            }
        }

        // Observe Loading State
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                saveButton.isEnabled = !isLoading
                saveButton.text = if (isLoading) "Saving..." else "Save Coral"
            }
        }

        // Observe Selected Image
        lifecycleScope.launch {
            viewModel.selectedImageUri.collect { uri ->
                if (uri != null) {
                    imagePreview.setImageURI(uri)
                    imagePreview.visibility = View.VISIBLE
                } else {
                    imagePreview.setImageResource(0)
                    imagePreview.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is AddCoralEvent.ShowError -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        viewModel.clearEvent()
                    }
                    // --- PERUBAHAN DI SINI ---
                    // Kita tangani event baru 'SuccessAndNavigate'
                    is AddCoralEvent.SuccessAndNavigate -> {
                        // 1. Tampilkan pesan sukses dari properti event yang baru
                        Toast.makeText(requireContext(), event.successMessage, Toast.LENGTH_SHORT).show()

                        // 2. Langsung navigasi kembali ke halaman sebelumnya
                        findNavController().navigateUp()

                        // 3. Hapus event agar tidak dieksekusi lagi jika layar berotasi
                        viewModel.clearEvent()
                    }
                    null -> {
                        // Tidak ada event, tidak perlu melakukan apa-apa
                    }
                }
            }
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
//        val contentResolver = context?.contentResolver
//        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        contentResolver.takePersistableUriPermission(uri, takeFlags)
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

    // File: AddCoralFragment.kt

    private fun openGalleryModern() {
        // Prioritas 1: Coba gunakan Photo Picker modern jika tersedia.
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(requireContext())) {
            try {
                // Lakukan peluncuran di dalam try-catch sebagai pengaman utama.
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                // Jika berhasil, fungsi berhenti di sini.
                return
            } catch (e: ActivityNotFoundException) {
                // Kasus langka: isAvailable() true tapi launch() gagal.
                // Biarkan kode melanjutkan ke metode fallback di bawah.
                Log.e("AddCoralFragment", "Photo Picker tersedia tapi gagal diluncurkan.", e)
            }
        }

        // Prioritas 2 (Fallback): Jika Photo Picker modern tidak tersedia atau gagal, gunakan Intent galeri lama.
        // Ini akan dieksekusi jika blok 'if' di atas tidak berhasil 'return'.
        try {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*" // Eksplisit tentukan tipe untuk dicari
            pickImageLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            // Penanganan Final: Jika cara lama pun gagal, berarti tidak ada aplikasi galeri sama sekali.
            // Tampilkan pesan ke pengguna dan jangan buat aplikasi crash.
            Toast.makeText(
                requireContext(),
                "Tidak ditemukan aplikasi Galeri di perangkat ini.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun clearForm() {
        coralNameEditText.text?.clear()
        coralTypeEditText.text?.clear()
        coralDescriptionEditText.text?.clear()
        coralhargaEditText.text?.clear()
        coralTotalEditText.text?.clear()
        viewModel.setSelectedImageUri(null)
        viewModel.clearValidationErrors()
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