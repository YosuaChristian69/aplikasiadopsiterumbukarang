package id.istts.aplikasiadopsiterumbukarang.fragments

import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
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

    private lateinit var sessionManager: SessionManager
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
        checkExistingSession()
        setupVideoBackground(view)
        setupClickListeners(view)
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

    private fun checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            val userStatus = sessionManager.fetchUserStatus()
            navigateBasedOnRole(userStatus)
        }
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
        loginButton.setOnClickListener { performLogin() }

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

    private fun animateInputFocus(layout: TextInputLayout, hasFocus: Boolean) {
        val scale = if (hasFocus) 1.02f else 1f
        val elevation = if (hasFocus) 8f else 4f

        ObjectAnimator.ofFloat(layout, "scaleX", scale).setDuration(200).start()
        ObjectAnimator.ofFloat(layout, "scaleY", scale).setDuration(200).start()
        ObjectAnimator.ofFloat(layout, "elevation", elevation).setDuration(200).start()
    }

    private fun performLogin() {
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString()?.trim() ?: ""

        // Validation
        if (email.isEmpty()) {
            showFieldError(emailLayout, emailInput, "Email is required")
            return
        }
        if (password.isEmpty()) {
            showFieldError(passwordLayout, passwordInput, "Password is required")
            return
        }

        // Show loading state
        setLoadingState(true)

        // API call
        RetrofitClient.instance.login(LoginRequest(email, password))
            .enqueue(object : Callback<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse> {
                override fun onResponse(call: Call<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>,
                                        response: Response<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>) {
                    setLoadingState(false)
                    handleLoginResponse(response)
                }

                override fun onFailure(call: Call<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>, t: Throwable) {
                    setLoadingState(false)
                    showErrorMessage("Network error: ${t.message}")
                }
            })
    }

    private fun handleLoginResponse(response: Response<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>) {
        if (response.isSuccessful && response.body()?.msg?.equals("success login", true) == true) {
            response.body()?.token?.let { token ->
                sessionManager.saveAuthToken(token)

                try {
                    val userDetails = decodeTokenPayload(token)
                    sessionManager.saveUserDetails(
                        userDetails.getString("full_name"),
                        userDetails.getString("email"),
                        userDetails.getString("status")
                    )

                    showSuccessMessage("Login successful")
                    navigateBasedOnRole(userDetails.getString("status"))

                } catch (e: Exception) {
                    Log.e("LoginFragment", "Token decode error", e)
                    showErrorMessage("Authentication error")
                }
            }
        } else {
            val errorMsg = try {
                val errorJson = JSONObject(response.errorBody()?.string() ?: "{}")
                errorJson.optString("msg", "Login failed")
            } catch (e: Exception) {
                "Login failed: ${response.code()}"
            }
            showErrorMessage(errorMsg)
        }
    }

    private fun decodeTokenPayload(token: String): JSONObject {
        val payload = token.split(".")[1]
        val decodedBytes = android.util.Base64.decode(
            normalizeBase64(payload),
            android.util.Base64.DEFAULT
        )
        return JSONObject(String(decodedBytes))
    }

    private fun normalizeBase64(input: String): String {
        val padding = when (input.length % 4) {
            1 -> "==="
            2 -> "=="
            3 -> "="
            else -> ""
        }
        return input + padding
    }

    private fun showFieldError(layout: TextInputLayout, input: TextInputEditText, message: String) {
        layout.error = message
        input.requestFocus()
        shakeAnimation(layout)
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

    private fun navigateBasedOnRole(status: String?) {
        val actionId = when (status) {
            "user" -> R.id.action_loginFragment_to_userDashboardFragment
            "admin" -> R.id.action_loginFragment_to_adminDashboardFragment
            else -> R.id.action_loginFragment_to_workerDashboardFragment
        }

        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            Log.e("LoginFragment", "Navigation error", e)
            showErrorMessage("Navigation error: ${e.message}")
        }
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