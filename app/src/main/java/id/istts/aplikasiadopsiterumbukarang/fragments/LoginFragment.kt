package id.istts.aplikasiadopsiterumbukarang.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class LoginFragment : Fragment() {

    private lateinit var videoView: VideoView
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var sessionManager: SessionManager

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
        if (sessionManager.isLoggedIn()) {
            navigateToUserDashboard()
        }

        // Initialize views
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)

        // Set up video background
        setupVideoBackground(view)

        // Animate login card
        animateLoginCard(view)

        // Set up click listeners
        setupClickListeners(view)
    }

    private fun setupVideoBackground(view: View) {
        videoView = view.findViewById(R.id.videoBackground)

        // Use a video from raw resource folder
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.coral_background_video
        val uri = Uri.parse(videoPath)

        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true

            mp.setVolume(0f, 0f)

            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

            videoView.start()
        }

        videoView.setOnErrorListener { mp, what, extra ->
            true
        }
    }

    private fun animateLoginCard(view: View) {
        val loginCard = view.findViewById<CardView>(R.id.loginCard)

        loginCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupClickListeners(view: View) {
        val tvRegister = view.findViewById<TextView>(R.id.registerLink)
        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        val btnLogin = view.findViewById<MaterialButton>(R.id.loginButton)
        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }

        showLoading(true)

        val loginRequest = LoginRequest(email, password)

        RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse> {
            override fun onResponse(call: Call<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>, response: Response<id.istts.aplikasiadopsiterumbukarang.LoginLogic.LoginResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse?.msg?.equals("success login", ignoreCase = true) == true) {
                        Log.d("NamaTagAnda", "Token: ${loginResponse.token}")
                        // Save token to session
                        loginResponse.token?.let { token ->
                            sessionManager.saveAuthToken(token)

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

                                    sessionManager.saveUserDetails(name, userEmail, status)
                                    
                                    Toast.makeText(requireContext(), "Anda berhasil Login", Toast.LENGTH_SHORT).show()
                                    navigateToUserDashboard()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }


                    } else {
                        Toast.makeText(requireContext(), loginResponse?.msg ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
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
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun normalizeBase64(input: String): String {
        // Add padding if needed
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
        view?.findViewById<MaterialButton>(R.id.loginButton)?.isEnabled = !isLoading
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
    private fun naivgateToAdminDashboard() {
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
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}