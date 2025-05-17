package id.istts.aplikasiadopsiterumbukarang.fragments

import android.app.AlertDialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RegisterRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RegisterResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RequestVerificationRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.RequestVerificationResponse
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.VerifyAndRegisterRequest
import id.istts.aplikasiadopsiterumbukarang.RegisterLogic.VerifyAndRegisterResponse
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    // Declare the videoView property with lateinit
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

    // Countdown timer for verification code
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

        // Initialize views
        initializeViews(view)

        // Set up video background
        setupVideoBackground(view)

        // Animate register card
        animateRegisterCard(view)

        // Set up click listeners
        setupClickListeners()
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
    }

    private fun setupVideoBackground(view: View) {
        // Initialize the videoView property
        videoView = view.findViewById(R.id.videoBackground)

        // Use a video from raw resource folder
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.coral_background_video
        val uri = Uri.parse(videoPath)

        videoView.setVideoURI(uri)

        // Set on prepared listener to start the video when ready
        videoView.setOnPreparedListener { mp ->
            // Configure video properties
            mp.isLooping = true

            // Mute the video (for background videos)
            mp.setVolume(0f, 0f)

            // Optional: If you want to scale the video to cover the entire view
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

            // Start playback
            videoView.start()
        }

        // Handle any errors that might occur
        videoView.setOnErrorListener { _, _, _ ->
            // Log error or handle it appropriately
            // For now, we'll just return true to prevent crash
            true
        }
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
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        getCodeButton.setOnClickListener {
            if (validateFieldsForVerification()) {
                requestVerificationCode()
            }
        }

        registerButton.setOnClickListener {
            if (validateAllInputs()) {
                verifyAndRegister()
            }
        }
    }

    private fun validateFieldsForVerification(): Boolean {
        // Get input values
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Validate full name
        if (fullName.isEmpty()) {
            fullNameEditText.error = "Full name is required"
            fullNameEditText.requestFocus()
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            emailEditText.requestFocus()
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm your password"
            confirmPasswordEditText.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            confirmPasswordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun validateAllInputs(): Boolean {
        if (!validateFieldsForVerification()) {
            return false
        }

        // Validate verification code
        val verificationCode = verificationCodeEditText.text.toString().trim()
        if (verificationCode.isEmpty()) {
            verificationCodeEditText.error = "Verification code is required"
            verificationCodeEditText.requestFocus()
            return false
        }

        if (verificationCode.length != 5) {
            verificationCodeEditText.error = "Verification code must be 5 digits"
            verificationCodeEditText.requestFocus()
            return false
        }

        // Validate terms and conditions
        if (!termsCheckbox.isChecked) {
            Toast.makeText(context, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun requestVerificationCode() {
        // Show loading indicator on get code button
        setGetCodeButtonLoading(true)

        // Create verification request
        val request = RequestVerificationRequest(
            name = fullNameEditText.text.toString().trim(),
            email = emailEditText.text.toString().trim(),
            password = passwordEditText.text.toString()
        )

        // Make API call to request verification code
        RetrofitClient.instance.requestVerification(request).enqueue(object : Callback<RequestVerificationResponse> {
            override fun onResponse(call: Call<RequestVerificationResponse>, response: Response<RequestVerificationResponse>) {
                setGetCodeButtonLoading(false)

                if (response.isSuccessful) {
                    val requestResponse = response.body()
                    Toast.makeText(context, requestResponse?.msg ?: "Verification code sent", Toast.LENGTH_LONG).show()

                    // Disable fields and start countdown timer
                    disableInputFieldsAndStartTimer()
                } else {
                    // Handle error response
                    Toast.makeText(context, "Failed to send verification code. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RequestVerificationResponse>, t: Throwable) {
                setGetCodeButtonLoading(false)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun verifyAndRegister() {
        // Show loading indicator
        showLoading(true)

        // Create verification request
        val request = VerifyAndRegisterRequest(
            email = emailEditText.text.toString().trim(),
            verificationCode = verificationCodeEditText.text.toString()
        )

        // Make API call to verify and register
        RetrofitClient.instance.verifyAndRegister(request).enqueue(object : Callback<VerifyAndRegisterResponse> {
            override fun onResponse(call: Call<VerifyAndRegisterResponse>, response: Response<VerifyAndRegisterResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.msg == "Registration successful") {
                        // Registration successful
                        showSuccessDialog()
                    } else {
                        // Show error message from server
                        Toast.makeText(context, registerResponse?.msg ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Handle error response
                    Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<VerifyAndRegisterResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun disableInputFieldsAndStartTimer() {
        // Disable input fields temporarily
        fullNameEditText.isEnabled = false
        emailEditText.isEnabled = false
        passwordEditText.isEnabled = false
        confirmPasswordEditText.isEnabled = false

        // Start a countdown timer (10 minutes = 600000 milliseconds)
        startTimer(600000)
    }

    private fun startTimer(milliseconds: Long) {
        // Cancel existing timer if any
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
                isTimerRunning = false
                getCodeButton.text = "Get Code"
                getCodeButton.isEnabled = true

                // Re-enable input fields
                fullNameEditText.isEnabled = true
                emailEditText.isEnabled = true
                passwordEditText.isEnabled = true
                confirmPasswordEditText.isEnabled = true
            }
        }.start()
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            registerButton.isEnabled = false
            registerButton.text = "REGISTERING..."
        } else {
            registerButton.isEnabled = true
            registerButton.text = "REGISTER"
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Registration Successful")
            .setMessage("Your account has been created successfully. Please login to continue.")
            .setPositiveButton("Login") { _, _ ->
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            .setCancelable(false)
            .show()
    }

    // Handle lifecycle events to properly manage video playback and timer
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
        // Cancel timer if running
        timer?.cancel()
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }
}