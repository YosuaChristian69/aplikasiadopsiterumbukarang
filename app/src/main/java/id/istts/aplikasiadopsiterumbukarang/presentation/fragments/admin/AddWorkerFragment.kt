package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAddWorkerBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AddWorkerViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AddWorkerFragment : Fragment() {
    private var _binding: FragmentAddWorkerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    // Initialize ViewModel
    private val viewModel: AddWorkerViewModel by viewModels()

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
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            // TODO: Navigate back to previous fragment
            // findNavController().navigateUp()
        }

        binding.addWorkerButton.setOnClickListener {
            if (validateInputs()) {
                addWorker()
            }
        }

        binding.cancelButton.setOnClickListener {
            clearInputs()
            // TODO: Navigate back or show confirmation dialog
        }
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

        // Call ViewModel to add worker
        viewModel.addWorker(token, request)
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.addWorkerButton.isEnabled = !isLoading
        binding.cancelButton.isEnabled = !isLoading
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}