package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import android.widget.ImageButton
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.WorkerAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AdminWorkerDashboardViewModel

class AdminWorkerDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AdminWorkerDashboardViewModel
    private var bottomNavigation: BottomNavigationView? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var logoutButton: ImageButton
    private lateinit var workerAdapter: WorkerAdapter
    private lateinit var totalWorkersCount: TextView
    private lateinit var activeWorkersCount: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var fabAddWorker: FloatingActionButton
    private lateinit var userGreeting: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_worker_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[AdminWorkerDashboardViewModel::class.java]

        if (validateAccess()) {
            setupViews(view)
            setupUserGreeting()
            observeViewModel()
            loadWorkers()
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
        bottomNavigation = view.findViewById(R.id.bottom_navigation_view)
        recyclerView = view.findViewById(R.id.rv_workers)
        logoutButton = view.findViewById(R.id.logoutButton)
        totalWorkersCount = view.findViewById(R.id.totalWorkersCount)
        activeWorkersCount = view.findViewById(R.id.activeWorkersCount)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        searchEditText = view.findViewById(R.id.searchEditText)
        fabAddWorker = view.findViewById(R.id.fab_add_worker)
        userGreeting = view.findViewById(R.id.tv_user_greeting)

        logoutButton.setOnClickListener {
            performLogout()
        }

        // Setup FAB click listener
        fabAddWorker.setOnClickListener {
            // TODO: Navigate to add worker screen
            // navigateToAddWorker()
        }

        setupRecyclerView()
        setupSearchFunctionality()

        if (bottomNavigation != null) {
            setupBottomNavigation()
        }
    }

    private fun setupUserGreeting() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        userGreeting.text = "Hi, $userName"
    }

    private fun setupRecyclerView() {
        workerAdapter = WorkerAdapter { worker ->
            navigateToEditWorker(worker)
        }
            recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workerAdapter
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener { editable ->
            val query = editable?.toString() ?: ""

            // Debounce search untuk menghindari terlalu banyak request
            lifecycleScope.launch {
                delay(300) // Wait 300ms after user stops typing
                if (query == searchEditText.text.toString()) {
                    viewModel.searchWorkers(query, sessionManager)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.workers.observe(viewLifecycleOwner, Observer { workers ->
            updateUI(workers)
        })

        viewModel.totalWorkersCount.observe(viewLifecycleOwner, Observer { count ->
            totalWorkersCount.text = count.toString()
        })

        viewModel.activeWorkersCount.observe(viewLifecycleOwner, Observer { count ->
            activeWorkersCount.text = count.toString()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // You can add loading indicators here if needed
            if (isLoading) {
                // Show loading indicator
                Log.d(TAG, "Loading workers...")
            } else {
                // Hide loading indicator
                Log.d(TAG, "Finished loading workers")
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                Log.e(TAG, "Error loading workers: $error")

                // Show error message to user
                if (error.contains("authentication") || error.contains("token")) {
                    navigateToLogin()
                } else {
                    // You can show a toast or snackbar here
                    showEmptyState()
                }
            }
        })
    }

    private fun loadWorkers() {
        viewModel.loadWorkers(sessionManager)
    }

    private fun updateUI(workers: List<Worker>) {
        if (workers.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            workerAdapter.updateWorkers(workers)
        }
    }

    private fun showEmptyState() {
        emptyStateLayout.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE

        // Update stats to show 0 when no workers
        if (searchEditText.text.toString().isNotEmpty()) {
            // If searching and no results, don't reset total counts
            // Just show empty state for filtered results
        }
    }

    private fun hideEmptyState() {
        emptyStateLayout.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
    }

    private fun setupBottomNavigation() {
        bottomNavigation?.let { bottomNav ->
            bottomNav.selectedItemId = R.id.nav_worker

            bottomNav.post {
                bottomNav.selectedItemId = R.id.nav_worker
            }

            bottomNav.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_coral_seed -> {
                        animateBottomNavClick(menuItem) {
                            navigateToAdminDashboard()
                        }
                        true
                    }
                    R.id.nav_place -> {
                        animateBottomNavClick(menuItem) {
                            navigateToAdminPlace()
                        }
                        true
                    }
                    R.id.nav_worker -> {
                        true
                    }
                    else -> false
                }
            }

            bottomNav.setOnItemReselectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_coral_seed -> {
                        navigateToAdminDashboard()
                    }
                    R.id.nav_place -> {
                        navigateToAdminPlace()
                    }
                    R.id.nav_worker -> {
                        recyclerView?.smoothScrollToPosition(0)
                        // Clear search when reselecting
                        searchEditText.setText("")
                    }
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

    private fun performLogout() {
        if (isAdded && !isDetached) {
            sessionManager.clearSession()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun navigateToEditWorker(worker: Worker) {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                // Create bundle to pass worker data
                val bundle = Bundle().apply {
                    putString("workerId", worker.id_user)
                    putString("workerName", worker.full_name)
                    putString("workerEmail", worker.email)
                    putString("workerRole",worker.status)
                    putString("workerBalance", worker.balance)
                    putString("workerStatus", worker.user_status)
                }

                // Show feedback to user
                view?.let {
                    Snackbar.make(it, "Opening edit for ${worker.full_name}", Snackbar.LENGTH_SHORT).show()
                }

                // Navigate to edit worker fragment
                // Replace with your actual navigation action
                findNavController().navigate(
                    R.id.action_adminWorkerDashboardFragment_to_editUserFragment,
                    bundle
                )
            } catch (e: Exception) {
                e.printStackTrace()
                view?.let {
                    Snackbar.make(it, "Error opening edit screen", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun navigateToAdminDashboard() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_adminDashboardFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToAdminPlace() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_adminPlaceDashboardFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (validateAccess()) {
            bottomNavigation?.selectedItemId = R.id.nav_worker
            loadWorkers()
            setupUserGreeting()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminWorkerDashboardFragment()
    }
}