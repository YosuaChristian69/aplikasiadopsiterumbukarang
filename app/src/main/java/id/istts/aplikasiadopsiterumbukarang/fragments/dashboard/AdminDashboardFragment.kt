package id.istts.aplikasiadopsiterumbukarang.fragments.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.fragments.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.fragments.models.CoralItem

class AdminDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: ImageButton  // Changed from Button to ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
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
        if (userStatus != "admin") {
            navigateToLogin()
            return false
        }

        return true
    }

    private fun setupViews(view: View) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)  // Now correctly casting to ImageButton

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
                findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
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
        fun newInstance() = AdminDashboardFragment()
    }
}