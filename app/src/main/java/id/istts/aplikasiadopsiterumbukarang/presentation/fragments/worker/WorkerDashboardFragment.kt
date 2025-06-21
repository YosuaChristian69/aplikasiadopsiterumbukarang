import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
// Ganti com.example.app dengan package name aplikasi Anda
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardBinding
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class WorkerDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private var _binding: FragmentWorkerDashboardBinding? = null
    // Properti ini hanya valid antara onCreateView dan onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        // MotionLayout akan memulai animasi secara otomatis karena
        // app:autoTransition="animateToEnd" di file scene.
        // Anda tidak perlu menambahkan kode Kotlin khusus untuk memulai animasi utama.

        // Setup listener atau logic lain di sini jika diperlukan
        setupBottomNavigation()
        setupLogoutButton()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
//                R.id.navigation_mission -> {
//                    // Handle klik "My Mission"
//                    true
//                }
//                R.id.navigation_profile -> {
//                    // Handle klik "My Profile"
//                    true
//                }
                else -> false
            }
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                Log.d("UserDashboard", "Navigating to login")
                findNavController().navigate(R.id.action_workerDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                Log.e("UserDashboard", "Navigation error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        sessionManager.clearSession()

        // Show logout confirmation message
        Toast.makeText(requireContext(), "Anda berhasil Logout", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        navigateToLogin()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}