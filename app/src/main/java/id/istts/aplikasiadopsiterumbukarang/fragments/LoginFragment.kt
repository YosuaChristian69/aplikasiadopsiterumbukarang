package id.istts.aplikasiadopsiterumbukarang.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var videoView: VideoView? = null
    private var emailEditText: TextInputEditText? = null
    private var passwordEditText: TextInputEditText? = null
    private var emailInputLayout: TextInputLayout? = null
    private var passwordInputLayout: TextInputLayout? = null
    private var sessionManager: SessionManager? = null
    private var logoImage: ImageView? = null
    private var titleText: TextView? = null
    private var subtitleText: TextView? = null
    private var loginCard: CardView? = null
    private var loginButton: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())

        // Check if user is already logged in
        sessionManager?.let { sm ->
            if (sm.isLoggedIn()) {
                when(sm.fetchUserStatus()) {
                    "user" -> navigateToUserDashboard()
                    "admin" -> navigateToAdminDashboard()
                    else -> navigateToWorkerDashboard()
                }
                return
            }
        }

        // Initialize views
        initializeViews(view)

        // Set up video background
        setupVideoBackground(view)

        // Start entrance animations
        startEntranceAnimations()

        // Set up click listeners
        setupClickListeners(view)
    }

    private fun initializeViews(view: View) {
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        emailInputLayout = view.findViewById(R.id.emailInputLayout)
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout)
        logoImage = view.findViewById(R.id.logoImage)
        titleText = view.findViewById(R.id.titleText)
        subtitleText = view.findViewById(R.id.subtitleText)
        loginCard = view.findViewById(R.id.loginCard)
        loginButton = view.findViewById(R.id.loginButton)
    }

    private fun setupVideoBackground(view: View) {
        videoView = view.findViewById(R.id.videoBackground)

        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.coral_background_video
        val uri = Uri.parse(videoPath)

        videoView?.setVideoURI(uri)

        videoView?.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            videoView?.start()
        }

        videoView?.setOnErrorListener { mp, what, extra -> true }
    }

    private fun startEntranceAnimations() {
        val logo = logoImage ?: return
        val title = titleText ?: return
        val subtitle = subtitleText ?: return
        val card = loginCard ?: return

        // Hide all views initially
        logo.alpha = 0f
        logo.scaleX = 0.3f
        logo.scaleY = 0.3f

        title.alpha = 0f
        title.translationY = 50f

        subtitle.alpha = 0f
        subtitle.translationY = 50f

        card.alpha = 0f
        card.translationY = 200f
        card.scaleX = 0.8f
        card.scaleY = 0.8f

        // Logo animation with bounce effect
        val logoAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleX", 0.3f, 1.2f, 1f),
                ObjectAnimator.ofFloat(logo, "scaleY", 0.3f, 1.2f, 1f)
            )
            duration = 800
            interpolator = OvershootInterpolator(1.2f)
            startDelay = 200
        }

        // Title animation with slide up
        val titleAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(title, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(title, "translationY", 50f, 0f)
            )
            duration = 600
            interpolator = DecelerateInterpolator()
            startDelay = 600
        }

        // Subtitle animation
        val subtitleAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(subtitle, "translationY", 50f, 0f)
            )
            duration = 600
            interpolator = DecelerateInterpolator()
            startDelay = 800
        }

        // Login card animation with spring effect
        val cardAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(card, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(card, "translationY", 200f, 0f),
                ObjectAnimator.ofFloat(card, "scaleX", 0.8f, 1f),
                ObjectAnimator.ofFloat(card, "scaleY", 0.8f, 1f)
            )
            duration = 800
            interpolator = OvershootInterpolator(0.8f)
            startDelay = 1000
        }

        // Start all animations
        logoAnimator.start()
        titleAnimator.start()
        subtitleAnimator.start()
        cardAnimator.start()

        // Add floating animation to logo
        startLogoFloatingAnimation()
    }

    private fun startLogoFloatingAnimation() {
        val logo = logoImage ?: return

        val floatingAnimator = ObjectAnimator.ofFloat(logo, "translationY", 0f, -20f, 0f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        logo.postDelayed({
            floatingAnimator.start()
        }, 1500)
    }

    private fun setupClickListeners(view: View) {
        val tvRegister = view.findViewById<TextView>(R.id.registerLink)
        tvRegister.setOnClickListener {
            animateClickEffect(it) {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }

        loginButton?.setOnClickListener {
            animateLoginButton {
                performLogin()
            }
        }

        // Add focus animations for input fields
        setupInputFieldAnimations()
    }

    private fun setupInputFieldAnimations() {
        emailEditText?.setOnFocusChangeListener { _, hasFocus ->
            emailInputLayout?.let { animateInputField(it, hasFocus) }
        }

        passwordEditText?.setOnFocusChangeListener { _, hasFocus ->
            passwordInputLayout?.let { animateInputField(it, hasFocus) }
        }
    }

    private fun animateInputField(inputLayout: TextInputLayout, hasFocus: Boolean) {
        val scaleValue = if (hasFocus) 1.05f else 1f
        val elevation = if (hasFocus) 12f else 4f

        ObjectAnimator.ofFloat(inputLayout, "scaleX", scaleValue).apply {
            duration = 200
            start()
        }
        ObjectAnimator.ofFloat(inputLayout, "scaleY", scaleValue).apply {
            duration = 200
            start()
        }
        ObjectAnimator.ofFloat(inputLayout, "elevation", elevation).apply {
            duration = 200
            start()
        }
    }

    private fun animateClickEffect(view: View, onComplete: () -> Unit) {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f)
            )
            duration = 100
        }

        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f)
            )
            duration = 100
        }

        scaleDown.start()
        scaleDown.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                scaleUp.start()
                onComplete()
            }
        })
    }

    private fun animateLoginButton(onComplete: () -> Unit) {
        val button = loginButton ?: return

        // Create pulse effect
        val pulseAnimator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f, 1f).apply {
            duration = 200
        }
        val pulseAnimatorY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f, 1f).apply {
            duration = 200
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(pulseAnimator, pulseAnimatorY)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                }
            })
        }

        animatorSet.start()
    }

    private fun showLoadingAnimation(isLoading: Boolean) {
        val button = loginButton ?: return

        if (isLoading) {
            // Disable button and show loading state
            button.isEnabled = false
            button.text = "LOGGING IN..."

            // Create rotation animation for loading
            val rotationAnimator = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f).apply {
                duration = 1000
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
            rotationAnimator.start()
            button.tag = rotationAnimator
        } else {
            // Stop loading animation
            (button.tag as? ObjectAnimator)?.cancel()
            button.rotation = 0f
            button.isEnabled = true
            button.text = "LOGIN"
            button.tag = null
        }
    }

    private fun performLogin() {
        val email = emailEditText?.text?.toString()?.trim() ?: ""
        val password = passwordEditText?.text?.toString()?.trim() ?: ""

        if (email.isEmpty()) {
            emailInputLayout?.let { animateErrorField(it) }
            emailEditText?.error = "Email is required"
            emailEditText?.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInputLayout?.let { animateErrorField(it) }
            passwordEditText?.error = "Password is required"
            passwordEditText?.requestFocus()
            return
        }

        showLoadingAnimation(true)

        val loginRequest = LoginRequest(email, password)

        RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse> {
            override fun onResponse(call: Call<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>, response: Response<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>) {
                showLoadingAnimation(false)

                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse?.msg?.equals("success login", ignoreCase = true) == true) {
                        Log.d("NamaTagAnda", "Token: ${loginResponse.token}")
                        loginResponse.token?.let { token ->
                            sessionManager?.saveAuthToken(token)

                            try {
                                val splitToken = token.split(".")
                                if (splitToken.size >= 2) {
                                    val payload = splitToken[1]
                                    val decodedPayload = android.util.Base64.decode(
                                        normalizeBase64(payload),
                                        android.util.Base64.DEFAULT
                                    )
                                    val payloadJson = JSONObject(String(decodedPayload))

                                    val name = payloadJson.getString("full_name")
                                    val userEmail = payloadJson.getString("email")
                                    val status = payloadJson.getString("status")

                                    sessionManager?.saveUserDetails(name, userEmail, status)

                                    // Success animation
                                    animateSuccessLogin {
                                        Toast.makeText(requireContext(), "Anda berhasil Login", Toast.LENGTH_SHORT).show()
                                        when(status.toString()) {
                                            "user" -> navigateToUserDashboard()
                                            "admin" -> navigateToAdminDashboard()
                                            else -> navigateToWorkerDashboard()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        animateErrorLogin()
                        Toast.makeText(requireContext(), loginResponse?.msg ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    animateErrorLogin()
                    try {
                        val errorBody = response.errorBody()?.string()
                        val errorJson = JSONObject(errorBody ?: "{}")
                        val errorMsg = errorJson.optString("msg", "Login failed")
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>, t: Throwable) {
                showLoadingAnimation(false)
                animateErrorLogin()
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun animateErrorField(inputLayout: TextInputLayout) {
        val shakeAnimator = ObjectAnimator.ofFloat(inputLayout, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        shakeAnimator.start()
    }

    private fun animateErrorLogin() {
        val card = loginCard ?: return

        val shakeAnimator = ObjectAnimator.ofFloat(card, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        shakeAnimator.start()
    }

    private fun animateSuccessLogin(onComplete: () -> Unit) {
        val card = loginCard ?: return

        val successAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.1f, 1f),
                ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.1f, 1f),
                ObjectAnimator.ofFloat(card, "alpha", 1f, 0.8f, 1f)
            )
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                }
            })
        }
        successAnimator.start()
    }

    private fun normalizeBase64(input: String): String {
        val padding = when (input.length % 4) {
            0 -> ""
            1 -> "==="
            2 -> "=="
            3 -> "="
            else -> ""
        }
        return input + padding
    }

    private fun showLoading(isLoading: Boolean) {
        showLoadingAnimation(isLoading)
    }

    private fun navigateToUserDashboard() {
        Log.d("Navigation", "Attempting to navigate to HomeFragment")
        try {
            findNavController().navigate(R.id.action_loginFragment_to_userDashboardFragment)
            Log.d("Navigation", "Navigation command executed")
        } catch (e: Exception) {
            Log.e("Navigation", "Navigation failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToAdminDashboard() {
        Log.d("Navigation", "Attempting to navigate to HomeFragment")
        try {
            findNavController().navigate(R.id.action_loginFragment_to_adminDashboardFragment)
            Log.d("Navigation", "Navigation command executed")
        } catch (e: Exception) {
            Log.e("Navigation", "Navigation failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToWorkerDashboard() {
        Log.d("Navigation", "Attempting to navigate to HomeFragment")
        try {
            findNavController().navigate(R.id.action_loginFragment_to_workerDashboardFragment)
            Log.d("Navigation", "Navigation command executed")
        } catch (e: Exception) {
            Log.e("Navigation", "Navigation failed: ${e.message}", e)
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        videoView?.let { video ->
            if (!video.isPlaying) {
                video.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        videoView?.let { video ->
            if (video.isPlaying) {
                video.pause()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Stop video playback safely
        videoView?.let { video ->
            try {
                video.stopPlayback()
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error stopping video: ${e.message}")
            }
        }

        // Cancel any running animations safely
        loginButton?.let { button ->
            (button.tag as? ObjectAnimator)?.cancel()
            button.tag = null
        }

        videoView = null
        emailEditText = null
        passwordEditText = null
        emailInputLayout = null
        passwordInputLayout = null
        sessionManager = null
        logoImage = null
        titleText = null
        subtitleText = null
        loginCard = null
        loginButton = null

        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}