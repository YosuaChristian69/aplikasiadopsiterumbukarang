package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAddWorkerBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddWorkerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddWorkerFragment : Fragment() {
    private var _binding: FragmentAddWorkerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

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

        // Show loading state
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken().toString()
                if (token.isNullOrEmpty()) {
                    showError("Authentication token not found. Please login again.")
                    setLoadingState(false)
                    return@launch
                }

                val response = RetrofitClient.instance.addUserWorker(token, request)

                setLoadingState(false)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    showSuccessDialog(responseBody?.msg ?: "Worker added successfully!")

                } else {
                    val errorBody = response.errorBody()?.string()
                    // Try to parse error message from backend
                    val errorMessage = try {
                        val errorJson = com.google.gson.JsonParser.parseString(errorBody)
                        errorJson.asJsonObject.get("msg").asString
                    } catch (e: Exception) {
                        "Failed to add worker. Please try again."
                    }
                    showError(errorMessage)
                }

            } catch (e: Exception) {
                setLoadingState(false)
                showError("Network error: ${e.localizedMessage}")
            }
        }
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

        // Clear any existing errors
        binding.fullNameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}