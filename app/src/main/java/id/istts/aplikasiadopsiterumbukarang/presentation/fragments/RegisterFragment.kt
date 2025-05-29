package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.app.AlertDialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.RegisterViewModel

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    // UI Components
    private lateinit var videoView: VideoView
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var verificationCodeEditText: TextInputEditText
    private lateinit var getCodeButton: MaterialButton
    private lateinit var termsCheckbox: CheckBox
    private lateinit var registerButton: MaterialButton
    private lateinit var loginLink: TextView

    // Timer for countdown
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        initializeViewModel()
        setupVideoBackground(view)
        animateRegisterCard(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        fullNameEditText = view.findViewById(R.id.fullNameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        verificationCodeEditText = view.findViewById(R.id.verificationCodeEditText)
        getCodeButton = view.findViewById(R.id.getCodeButton)
        termsCheckbox = view.findViewById(R.id.termsCheckbox)
        registerButton = view.findViewById(R.id.registerButton)
        loginLink = view.findViewById(R.id.loginLink)
        videoView = view.findViewById(R.id.videoBackground)
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
    }

    private fun setupVideoBackground(view: View) {
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.coral_background_video
        val uri = Uri.parse(videoPath)

        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            videoView.start()
        }

        videoView.setOnErrorListener { _, _, _ -> true }
    }

    private fun animateRegisterCard(view: View) {
        val registerCard = view.findViewById<CardView>(R.id.registerCard)

        registerCard.alpha = 0f
        registerCard.translationY = 100f

        registerCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupClickListeners() {
        loginLink.setOnClickListener {
            viewModel.navigateToLogin()
        }

        getCodeButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            viewModel.requestVerificationCode(fullName, email, password, confirmPassword)
        }

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val verificationCode = verificationCodeEditText.text.toString().trim()
            val termsAccepted = termsCheckbox.isChecked

            if (viewModel.validateAllInputs(fullName, email, password, confirmPassword, verificationCode, termsAccepted)) {
                viewModel.verifyAndRegister(email, verificationCode)
            }
        }
    }

    private fun observeViewModel() {
        // Observe get code loading state
        viewModel.isGetCodeLoading.observe(viewLifecycleOwner) { isLoading ->
            setGetCodeButtonLoading(isLoading)
        }

        // Observe register loading state
        viewModel.isRegisterLoading.observe(viewLifecycleOwner) { isLoading ->
            setRegisterButtonLoading(isLoading)
        }

        // Observe verification state
        viewModel.verificationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.VerificationState.Idle -> {
                    // Reset state
                }
                is RegisterViewModel.VerificationState.Loading -> {
                    clearFieldErrors()
                }
                is RegisterViewModel.VerificationState.Success -> {
                    // Success handled by other observers
                }
                is RegisterViewModel.VerificationState.Error -> {
                    // Error handled by errorMessage observer
                }
            }
        }

        // Observe register state
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegisterState.Idle -> {
                    // Reset state
                }
                is RegisterViewModel.RegisterState.Loading -> {
                    clearFieldErrors()
                }
                is RegisterViewModel.RegisterState.Success -> {
                    // Success handled by navigationEvent observer
                }
                is RegisterViewModel.RegisterState.Error -> {
                    // Error handled by errorMessage observer
                }
            }
        }

        // Observe field errors
        viewModel.fieldError.observe(viewLifecycleOwner) { fieldError ->
            showFieldError(fieldError)
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
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            handleNavigationEvent(event)
        }

        // Observe timer events
        viewModel.timerEvent.observe(viewLifecycleOwner) { event ->
            handleTimerEvent(event)
        }

        // Observe fields enabled state
        viewModel.fieldsEnabledState.observe(viewLifecycleOwner) { enabled ->
            setFieldsEnabled(enabled)
        }
    }

    private fun showFieldError(fieldError: RegisterViewModel.FieldError) {
        when (fieldError.field) {
            RegisterViewModel.FieldType.FULL_NAME -> {
                fullNameEditText.error = fieldError.message
                fullNameEditText.requestFocus()
            }
            RegisterViewModel.FieldType.EMAIL -> {
                emailEditText.error = fieldError.message
                emailEditText.requestFocus()
            }
            RegisterViewModel.FieldType.PASSWORD -> {
                passwordEditText.error = fieldError.message
                passwordEditText.requestFocus()
            }
            RegisterViewModel.FieldType.CONFIRM_PASSWORD -> {
                confirmPasswordEditText.error = fieldError.message
                confirmPasswordEditText.requestFocus()
            }
            RegisterViewModel.FieldType.VERIFICATION_CODE -> {
                verificationCodeEditText.error = fieldError.message
                verificationCodeEditText.requestFocus()
            }
        }
    }

    private fun clearFieldErrors() {
        fullNameEditText.error = null
        emailEditText.error = null
        passwordEditText.error = null
        confirmPasswordEditText.error = null
        verificationCodeEditText.error = null
    }

    private fun handleNavigationEvent(event: RegisterViewModel.NavigationEvent) {
        when (event) {
            is RegisterViewModel.NavigationEvent.NavigateToLogin -> {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            is RegisterViewModel.NavigationEvent.ShowSuccessDialog -> {
                showSuccessDialog()
            }
        }
    }

    private fun handleTimerEvent(event: RegisterViewModel.TimerEvent) {
        when (event) {
            is RegisterViewModel.TimerEvent.Start -> {
                startTimer(event.milliseconds)
            }
            is RegisterViewModel.TimerEvent.Finished -> {
                onTimerFinished()
            }
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        fullNameEditText.isEnabled = enabled
        emailEditText.isEnabled = enabled
        passwordEditText.isEnabled = enabled
        confirmPasswordEditText.isEnabled = enabled
    }

    private fun startTimer(milliseconds: Long) {
        timer?.cancel()

        timer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                isTimerRunning = true
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                getCodeButton.text = String.format("%02d:%02d", minutes, seconds)
                getCodeButton.isEnabled = false
            }

            override fun onFinish() {
                onTimerFinished()
                viewModel.onTimerFinished()
            }
        }.start()
    }

    private fun onTimerFinished() {
        isTimerRunning = false
        getCodeButton.text = "Get Code"
        getCodeButton.isEnabled = true
    }

    private fun setGetCodeButtonLoading(isLoading: Boolean) {
        if (isLoading) {
            getCodeButton.isEnabled = false
            getCodeButton.text = "Sending..."
        } else if (!isTimerRunning) {
            getCodeButton.isEnabled = true
            getCodeButton.text = "Get Code"
        }
    }

    private fun setRegisterButtonLoading(isLoading: Boolean) {
        if (isLoading) {
            registerButton.isEnabled = false
            registerButton.text = "REGISTERING..."
        } else {
            registerButton.isEnabled = true
            registerButton.text = "REGISTER"
        }
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Registration Successful")
            .setMessage("Your account has been created successfully. Please login to continue.")
            .setPositiveButton("Login") { _, _ ->
                viewModel.navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::videoView.isInitialized && !videoView.isPlaying) {
            videoView.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::videoView.isInitialized && videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }
}