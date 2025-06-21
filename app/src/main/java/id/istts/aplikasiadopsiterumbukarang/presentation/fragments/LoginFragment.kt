package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.data.repositories.LoginRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.login.LoginViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.login.LoginViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager

    // UI Components
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton
    private var videoView: VideoView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents(view)
        initializeViewModel()
        setupVideoBackground(view)
        setupClickListeners(view)
        observeViewModel()

        // Check existing session
        viewModel.checkExistingSession()
    }

    private fun initializeComponents(view: View) {
        sessionManager = SessionManager(requireContext())
        emailInput = view.findViewById(R.id.emailEditText)
        passwordInput = view.findViewById(R.id.passwordEditText)
        emailLayout = view.findViewById(R.id.emailInputLayout)
        passwordLayout = view.findViewById(R.id.passwordInputLayout)
        loginButton = view.findViewById(R.id.loginButton)
        videoView = view.findViewById(R.id.videoBackground)
    }

    private fun initializeViewModel() {
        // Create repository instance
        val loginRepository = LoginRepositoryImpl(RetrofitClient.instance)

        // Create ViewModel with factory
        val factory = LoginViewModelFactory(loginRepository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        // Initialize session manager
        viewModel.initSessionManager(sessionManager)
    }

    private fun setupVideoBackground(view: View) {
        val videoPath = "android.resource://${requireActivity().packageName}/${R.raw.coral_background_video}"
        videoView?.apply {
            setVideoURI(Uri.parse(videoPath))
            setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.setVolume(0f, 0f)
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                start()
            }
            setOnErrorListener { _, _, _ -> true }
        }
    }

    private fun setupClickListeners(view: View) {
        // Login button
        loginButton.setOnClickListener {
            val email = emailInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""
            viewModel.performLogin(email, password)
        }

        // Register link
        view.findViewById<View>(R.id.registerLink).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Input field focus animations
        setupInputAnimations()
    }

    private fun setupInputAnimations() {
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            animateInputFocus(emailLayout, hasFocus)
        }
        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            animateInputFocus(passwordLayout, hasFocus)
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoadingState(isLoading)
        }

        // Observe login state
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Idle -> {
                    // Reset UI state
                }
                is LoginViewModel.LoginState.Loading -> {
                    clearFieldErrors()
                }
                is LoginViewModel.LoginState.Success -> {
                    // Success handled by successMessage observer
                }
                is LoginViewModel.LoginState.Error -> {
                    // Error handled by errorMessage observer
                }
            }
        }

        // Observe field errors
        viewModel.fieldError.observe(viewLifecycleOwner) { fieldError ->
            when (fieldError.field) {
                LoginViewModel.FieldType.EMAIL -> {
                    showFieldError(emailLayout, emailInput, fieldError.message)
                }
                LoginViewModel.FieldType.PASSWORD -> {
                    showFieldError(passwordLayout, passwordInput, fieldError.message)
                }
            }
        }

        // Observe success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            showSuccessMessage(message)
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showErrorMessage(message)
        }

        // Observe navigation events
        viewModel.navigationEvent.observe(viewLifecycleOwner) { navigationEvent ->
            handleNavigation(navigationEvent.target)
        }
    }

    private fun handleNavigation(target: LoginViewModel.NavigationTarget) {
        val actionId = when (target) {
            LoginViewModel.NavigationTarget.USER_DASHBOARD ->
                R.id.action_loginFragment_to_userDashboardFragment
            LoginViewModel.NavigationTarget.ADMIN_DASHBOARD ->
                R.id.action_loginFragment_to_adminDashboardFragment
            LoginViewModel.NavigationTarget.WORKER_DASHBOARD ->
                R.id.action_loginFragment_to_workerDashboardFragment
        }

        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            Log.e("LoginFragment", "Navigation error", e)
            showErrorMessage("Navigation error: ${e.message}")
        }
    }

    private fun animateInputFocus(layout: TextInputLayout, hasFocus: Boolean) {
        val scale = if (hasFocus) 1.02f else 1f
        val elevation = if (hasFocus) 8f else 4f

        ObjectAnimator.ofFloat(layout, "scaleX", scale).setDuration(200).start()
        ObjectAnimator.ofFloat(layout, "scaleY", scale).setDuration(200).start()
        ObjectAnimator.ofFloat(layout, "elevation", elevation).setDuration(200).start()
    }

    private fun showFieldError(layout: TextInputLayout, input: TextInputEditText, message: String) {
        layout.error = message
        input.requestFocus()
        shakeAnimation(layout)
    }

    private fun clearFieldErrors() {
        emailLayout.error = null
        passwordLayout.error = null
    }

    private fun shakeAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 15f, -15f, 0f)
            .setDuration(500).start()
    }

    private fun setLoadingState(loading: Boolean) {
        loginButton.apply {
            isEnabled = !loading
            text = if (loading) "LOGGING IN..." else "LOGIN"
        }
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        shakeAnimation(loginButton)
    }

    override fun onResume() {
        super.onResume()
        videoView?.takeIf { !it.isPlaying }?.start()
    }

    override fun onPause() {
        super.onPause()
        videoView?.takeIf { it.isPlaying }?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoView?.let {
            try {
                it.stopPlayback()
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error stopping video", e)
            }
        }
        videoView = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}