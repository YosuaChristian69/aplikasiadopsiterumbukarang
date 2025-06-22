package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch


class UserAddCoralFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var userGreetingTextView: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var btnBack: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_add_coral, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Initialize views first
        initializeViews(view)

        // Setup logout button immediately (tidak tergantung validasi)
//        setupLogoutButton()

        lifecycleScope.launch {
            if (!validateUserAccess()) {
                return@launch
            }
            setupViews()
        }
    }
    private fun setupViews() {
        try {
            val userName = sessionManager.fetchUserName() ?: "User"
            userGreetingTextView.text = "Hi, $userName"

            // Setup bottom navigation if needed\
            btnBack.setOnClickListener{
                Log.d("UserAddCoral", "btn back pressed")
                findNavController().navigate(R.id.action_userAddCoralFragment_to_userDashboardFragment)
//                true
            }
//            bottomNavigation.setOnItemSelectedListener { menuItem ->
//                when (menuItem.itemId) {
                    // Handle navigation items based on your menu
//                     R.id.nav_my_coral -> {
//                         findNavController().navigate(R.id.action_userAddCoralFragment_to_userDashboardFragment)
//                         true
//                     }
//                    R.id.nav_adopt_coral ->{
//                        findNavController().navigate(R.id.action_userDashboardFragment_to_userAddCoralFragment)
//                        true
//                    }
//                    else -> false
//                }
//            }

            Log.d("UserDashboard", "Views setup complete")
        } catch (e: Exception) {
            Log.e("UserDashboard", "Error setting up views: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                Log.d("UserDashboard", "Navigating to login")
                findNavController().navigate(R.id.action_userDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                Log.e("UserDashboard", "Navigation error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun performLogout() {
        if (!isAdded || isDetached) {
            Log.w("UserDashboard", "Cannot perform logout - fragment not attached")
            return
        }

        try {
            Log.d("UserDashboard", "Performing logout...")

            // Clear session data
            sessionManager.clearSession()

            // Show logout confirmation message
            Toast.makeText(requireContext(), "Anda berhasil Logout", Toast.LENGTH_SHORT).show()

            // Navigate to login screen
            navigateToLogin()
        } catch (e: Exception) {
            Log.e("UserDashboard", "Error during logout: ${e.message}")
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error during logout", Toast.LENGTH_SHORT).show()
        }
    }
    private fun validateUserAccess(): Boolean {
        if (!isAdded || isDetached) {
            Log.w("UserDashboard", "Fragment not properly attached")
            return false
        }

        if (!sessionManager.isLoggedIn()) {
            Log.w("UserDashboard", "User not logged in")
            navigateToLogin()
            return false
        }

        val userStatus = sessionManager.fetchUserStatus()
        if (userStatus != "user") {
            Log.w("UserDashboard", "Invalid user status: $userStatus")
            navigateToLogin()
            return false
        }

        Log.d("UserDashboard", "User access validated successfully")
        return true
    }
    override fun onResume() {
        super.onResume()
        Log.d("UserDashboard", "Fragment resumed")
        if (!validateUserAccess()) {
            return
        }
    }
    private fun initializeViews(view: View) {
        try {
            // Find the TextView that shows "Hi, USER" in your layout
            userGreetingTextView = view.findViewById<TextView>(R.id.userGreetingTextView)
                ?: throw IllegalStateException("userGreetingTextView not found in layout")

//            bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
//                ?: throw IllegalStateException("bottom_navigation not found in layout")

            btnBack = view.findViewById<Button>(R.id.btnBack)
                ?: throw IllegalStateException("btnBack not found in layout")

            Log.d("UserDashboard", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("UserDashboard", "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }
    companion object {
        @JvmStatic
        fun newInstance() = UserDashboardFragment()
    }

}