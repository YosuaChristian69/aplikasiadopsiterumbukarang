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
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_worker_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Gunakan lifecycleScope untuk memastikan navigation aman
        lifecycleScope.launch {
            if (!validateWorkerAccess()) {
                return@launch
            }

            setupViews(view)
        }
    }

    private fun validateWorkerAccess(): Boolean {
        // Cek apakah fragment masih aktif sebelum navigation
        if (!isAdded || isDetached) {
            return false
        }

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return false
        }

        val userStatus = sessionManager.fetchUserStatus()
        if (userStatus != "worker") {
            navigateToLogin()
            return false
        }

        return true
    }

    private fun setupViews(view: View) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        val userName = sessionManager.fetchUserName() ?: "Worker"
        welcomeTextView.text = "Welcome, $userName!"

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun navigateToLogin() {
        // Pastikan fragment masih aktif dan navigation controller tersedia
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_workerDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                // Log error jika perlu, tapi jangan crash
                e.printStackTrace()
            }
        }
    }

    private fun performLogout() {
        if (!isAdded || isDetached) {
            return
        }

        sessionManager.clearSession()

        Toast.makeText(requireContext(), "Anda berhasil Logout", Toast.LENGTH_SHORT).show()

        navigateToLogin()
    }

    override fun onResume() {
        super.onResume()
        if (!validateWorkerAccess()) {
            return
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = WorkerDashboardFragment()
    }
}