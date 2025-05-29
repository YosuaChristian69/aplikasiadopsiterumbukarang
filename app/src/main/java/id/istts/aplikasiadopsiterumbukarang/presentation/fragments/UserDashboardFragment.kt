package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.R

class UserDashboardFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Gunakan lifecycleScope untuk memastikan navigation aman
        lifecycleScope.launch {
            if (!validateUserAccess()) {
                return@launch
            }

            setupViews(view)
        }
    }

    private fun validateUserAccess(): Boolean {
        // Cek apakah fragment masih aktif sebelum navigation
        if (!isAdded || isDetached) {
            return false
        }

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return false
        }

        val userStatus = sessionManager.fetchUserStatus()
        if (userStatus != "user") {
            navigateToLogin()
            return false
        }

        return true
    }

    private fun setupViews(view: View) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        val userName = sessionManager.fetchUserName() ?: "User"
        welcomeTextView.text = "Welcome, $userName!"

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun navigateToLogin() {
        // Pastikan fragment masih aktif dan navigation controller tersedia
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_userDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                // Log error jika perlu, tapi jangan crash
                e.printStackTrace()
            }
        }
    }

    private fun performLogout() {
        // Pastikan fragment masih aktif sebelum logout
        if (!isAdded || isDetached) {
            return
        }

        // Clear user session
        sessionManager.clearSession()

        Toast.makeText(requireContext(), "Anda berhasil Logout", Toast.LENGTH_SHORT).show()

        navigateToLogin()
    }

    override fun onResume() {
        super.onResume()
        // Validasi ulang saat fragment resume (kembali dari background)
        if (!validateUserAccess()) {
            return
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserDashboardFragment()
    }
}