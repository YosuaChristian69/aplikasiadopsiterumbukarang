package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editUser.EditUserViewModel
import java.text.SimpleDateFormat
import java.util.*

class EditUserFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: EditUserViewModel

    // UI Elements
    private lateinit var backButton: ImageButton
    private lateinit var userIdText: TextView
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText // Changed from TextView to TextInputEditText
    private lateinit var balanceText: TextView // Changed from TextInputEditText to TextView
    private lateinit var userStatusDropdown: AutoCompleteTextView
    private lateinit var joinedAtText: TextView
    private lateinit var updateButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var loadingContainer: View

    // Data
    private var workerId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[EditUserViewModel::class.java]

        if (validateAccess()) {
            setupViews(view)
            setupDropdowns()
            setupObservers()
            loadUserData()
        }
    }

    private fun validateAccess(): Boolean {
        if (!isAdded || isDetached) return false

        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "admin") {
            navigateToLogin()
            return false
        }
        return true
    }

    private fun setupViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        userIdText = view.findViewById(R.id.userIdText)
        fullNameEditText = view.findViewById(R.id.fullNameEditText)
        emailEditText = view.findViewById(R.id.emailEditText) // Now editable
        balanceText = view.findViewById(R.id.balanceText) // Now read-only
        userStatusDropdown = view.findViewById(R.id.userStatusDropdown)
        joinedAtText = view.findViewById(R.id.joinedAtText)
        updateButton = view.findViewById(R.id.updateButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        loadingContainer = view.findViewById(R.id.loadingContainer)

        // Get worker ID from arguments
        workerId = arguments?.getString("workerId")

        // Setup click listeners
        backButton.setOnClickListener {
            navigateBack()
        }

        cancelButton.setOnClickListener {
            navigateBack()
        }

        updateButton.setOnClickListener {
            updateUser()
        }
    }

    private fun setupDropdowns() {
        // Setup User Status dropdown
        val userStatusOptions = arrayOf("active", "inactive")
        val userStatusAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, userStatusOptions)
        userStatusDropdown.setAdapter(userStatusAdapter)
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                populateUserData(user)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showError(error)
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSuccess("User updated successfully")
                navigateBack()
            }
        }
    }

    private fun loadUserData() {
        workerId?.let { id ->
            viewModel.fetchUserById(id, sessionManager)
        } ?: run {
            showError("User ID not found")
        }
    }

    private fun populateUserData(user: Worker) {
        userIdText.text = "#${user.id_user}"
        fullNameEditText.setText(user.full_name)
        emailEditText.setText(user.email) // Now editable - use setText for TextInputEditText

        // Set balance as read-only text
        balanceText.text = "Rp ${user.balance}"

        // Set user status dropdown
        userStatusDropdown.setText(user.user_status, false)

        // Format and set joined date - FIXED: Handle null date properly
        if (user.joined_at != null) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                joinedAtText.text = dateFormat.format(user.joined_at)
            } catch (e: Exception) {
                joinedAtText.text = user.joined_at.toString()
                Log.w("EditUserFragment", "Failed to format date: ${e.message}")
            }
        } else {
            joinedAtText.text = "N/A" // or any default text you prefer
            Log.w("EditUserFragment", "joined_at is null for user ${user.id_user}")
        }
    }

    private fun updateUser() {
        if (!validateInputs()) return

        // Update data now includes email instead of balance
        val updateData = mapOf(
            "full_name" to fullNameEditText.text.toString().trim(),
            "email" to emailEditText.text.toString().trim(), // Now email is editable
            "user_status" to userStatusDropdown.text.toString()
        )

        workerId?.let { id ->
            viewModel.updateUser(id, updateData, sessionManager)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate full name
        if (fullNameEditText.text.toString().trim().isEmpty()) {
            fullNameEditText.error = "Full name is required"
            isValid = false
        }

        // Validate email
        val emailText = emailEditText.text.toString().trim()
        if (emailText.isEmpty()) {
            emailEditText.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailEditText.error = "Please enter a valid email address"
            isValid = false
        }

        // Validate user status
        if (userStatusDropdown.text.toString().isEmpty()) {
            userStatusDropdown.error = "User status is required"
            isValid = false
        }

        return isValid
    }

    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
        Log.e("EditUserFragment", message)
    }

    private fun showSuccess(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navigateBack() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_editUserFragment_to_loginFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EditUserFragment()
    }
}