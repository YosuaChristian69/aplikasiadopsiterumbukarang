package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditCoralFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditCoralFragment : Fragment() {
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
    private lateinit var sessionManager: SessionManager



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_coral, container, false)
        initDependencies()
        initViews(view)
        return view
    }

    private fun initDependencies() {
        sessionManager = SessionManager(requireContext())
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
        cancelButton.setOnClickListener {
            showCancelDialog()
        }
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