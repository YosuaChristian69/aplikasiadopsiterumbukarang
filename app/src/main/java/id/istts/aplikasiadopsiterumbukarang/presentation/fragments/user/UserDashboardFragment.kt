package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar // Import ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Import for by viewModels()
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.presentation.SelectionMode
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.UserCoralAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserDashboardViewModel// You'll need to create this ViewModel
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class UserDashboardFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var userGreetingTextView: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var logoutButton: Button

    // --- NEW: Add properties for RecyclerView and its components ---
    private lateinit var myCoralsRecyclerView: RecyclerView
    private lateinit var userCoralAdapter: UserCoralAdapter
    private lateinit var loadingProgressBar: ProgressBar // For loading state

    // --- NEW: Instantiate the ViewModel using KTX delegate ---
    // Make sure to add the 'androidx.fragment:fragment-ktx' dependency
    private val viewModel: UserDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Find all views in the layout
        initializeViews(view)

        // Setup UI components that don't depend on user login state
        setupLogoutButton()
        setupRecyclerView()

        // Observe changes from the ViewModel
        observeViewModel()

        // Validate user session and fetch data
        lifecycleScope.launch {
            if (validateUserAccess()) {
                setupUserSpecificViews()
                // Trigger data fetching from the ViewModel
                viewModel.fetchUserCorals(sessionManager.fetchAuthToken()!!)
            }
        }
    }

    private fun initializeViews(view: View) {
        userGreetingTextView = view.findViewById(R.id.userGreetingTextView)
        bottomNavigation = view.findViewById(R.id.bottom_navigation)
        logoutButton = view.findViewById(R.id.logoutButton)
        // --- NEW: Initialize new views ---
        myCoralsRecyclerView = view.findViewById(R.id.myCoralsRecyclerView)
        // You might need to add a ProgressBar to your XML layout
        // loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        Log.d("UserDashboard", "All views initialized successfully")
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            Log.d("UserDashboard", "Logout button clicked!")
            performLogout()
        }
    }

    // --- NEW: Function to set up the RecyclerView ---
    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list
//        userCoralAdapter = UserCoralAdapter(emptyList())
        userCoralAdapter = UserCoralAdapter(emptyList()) { ownershipId ->
            Log.d("UserDashboard", "Clicked on coral with ownershipId: $ownershipId")

            // Navigate using the new action and pass the ID
            val action = UserDashboardFragmentDirections
                .actionUserDashboardFragmentToUserDetailMyCoralFragment(ownershipId)
            findNavController().navigate(action)
        }
        myCoralsRecyclerView.apply {
            // Set the layout manager to a 2-column grid
            layoutManager = GridLayoutManager(context, 2)
            // Set the adapter
            adapter = userCoralAdapter
        }
        Log.d("UserDashboard", "RecyclerView setup complete")
    }

    // --- NEW: Function to observe LiveData from the ViewModel ---
    private fun observeViewModel() {
        viewModel.userCorals.observe(viewLifecycleOwner) { corals ->
            // When the list of corals is updated, submit it to the adapter
            Log.d("UserDashboard", "Observed ${corals.size} corals. Updating adapter.")
            userCoralAdapter.updateData(corals)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show or hide the progress bar based on loading state
            // loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d("UserDashboard", "isLoading state changed: $isLoading")
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            // If there's an error, show a toast message
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                Log.e("UserDashboard", "Error observed: $it")
            }
        }
    }

    private fun validateUserAccess(): Boolean {
        if (!isAdded) return false

        if (!sessionManager.isLoggedIn()) {
            Log.w("UserDashboard", "User not logged in, navigating to login.")
            navigateToLogin()
            return false
        }

        val userStatus = sessionManager.fetchUserStatus()
        if (userStatus != "user") {
            Log.w("UserDashboard", "Invalid user status: $userStatus, navigating to login.")
            navigateToLogin()
            return false
        }
        return true
    }

    private fun setupUserSpecificViews() {
        val userName = sessionManager.fetchUserName() ?: "User"
        userGreetingTextView.text = "Hi, $userName"

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_adopt_coral -> {
                    val action = UserDashboardFragmentDirections
                        .actionUserDashboardFragmentToUserSelectSpeciesFragment(SelectionMode.USER_SINGLE_SELECTION)
                    findNavController().navigate(action)
                    true
                }
                // Add other navigation cases here
                else -> false
            }
        }
        Log.d("UserDashboard", "User-specific views setup complete for $userName")
    }

    private fun navigateToLogin() {
        if (isAdded) {
            try {
                findNavController().navigate(R.id.action_userDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                Log.e("UserDashboard", "Navigation to login failed: ${e.message}")
            }
        }
    }

    private fun performLogout() {
        if (!isAdded) return

        Log.d("UserDashboard", "Performing logout...")
        sessionManager.clearSession()
        Toast.makeText(requireContext(), "Anda berhasil Logout", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }
}
