package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminPlaceDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_place_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Validate access before setting up views
        if (validateAccess()) {
            setupViews(view)
        }
    }

    private fun validateAccess(): Boolean {
        if (!isAdded || isDetached) return false

        if (!sessionManager.isLoggedIn() || sessionManager.fetchUserStatus() != "admin") {
            navigateToLogin()
            return false
        }
        return true
    }

    private fun setupViews(view: View) {
        // Initialize views
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Set current selected item for place dashboard
        bottomNavigation.selectedItemId = R.id.nav_place

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_coral_seed -> {
                    animateBottomNavClick(menuItem) {
                        navigateToAdminDashboard()
                    }
                    true
                }
                R.id.nav_place -> {
                    true
                }
                R.id.nav_worker -> {
                    animateBottomNavClick(menuItem) {
                        navigateToAdminWorker()
                    }
                    true
                }
                else -> false
            }
        }

        bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_coral_seed -> {
                    navigateToAdminDashboard()
                }
                R.id.nav_place -> {
                    if (::recyclerView.isInitialized) {
                        recyclerView.smoothScrollToPosition(0)
                    }
                }
                R.id.nav_worker -> {
                    // Navigate to worker dashboard
                    navigateToAdminWorker()
                }
            }
        }
    }

    private fun animateBottomNavClick(menuItem: android.view.MenuItem, action: () -> Unit) {
        val scaleAnimation = ObjectAnimator.ofFloat(bottomNavigation, "scaleY", 1f, 0.95f, 1f)
        scaleAnimation.apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        lifecycleScope.launch {
            delay(100)
            action()
        }
    }

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToAdminDashboard() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_adminDashboardFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToAdminWorker() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_adminWorkerDashboardFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        validateAccess()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminPlaceDashboardFragment()
    }
}