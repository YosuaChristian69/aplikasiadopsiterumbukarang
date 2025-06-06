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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.LokasiAdapter
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminPlaceDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var fabAddPlace: FloatingActionButton
    private lateinit var totalPlacesCount: TextView
    private lateinit var activePlacesCount: TextView
    private lateinit var greetingText: TextView

    private lateinit var lokasiAdapter: LokasiAdapter
    private var allLokasi = listOf<Lokasi>()
    private var filteredLokasi = listOf<Lokasi>()

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
            setupRecyclerView()
            setupSearch()
            loadLokasi()
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
        recyclerView = view.findViewById(R.id.recycler_view_places)
        searchEditText = view.findViewById(R.id.searchEditText)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        fabAddPlace = view.findViewById(R.id.fab_add_place)
        totalPlacesCount = view.findViewById(R.id.totalPlacesCount)
        activePlacesCount = view.findViewById(R.id.activePlacesCount)
        greetingText = view.findViewById(R.id.greeting_text)

        setupBottomNavigation()
        setupFab()
        setupGreeting()
    }

    private fun setupGreeting() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        greetingText.text = "Hi, $userName"
    }

    private fun setupRecyclerView() {
        lokasiAdapter = LokasiAdapter(
            onEditClick = { lokasi ->
                // TODO: Implement edit functionality
                Toast.makeText(context, "Edit ${lokasi.lokasiName}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { lokasi ->
                // TODO: Implement delete functionality
                Toast.makeText(context, "Delete ${lokasi.lokasiName}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lokasiAdapter
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener { text ->
            val query = text.toString().trim()
            filterLokasi(query)
        }
    }

    private fun setupFab() {
        fabAddPlace.setOnClickListener {
            Toast.makeText(context, "Add new place", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLokasi() {
        lifecycleScope.launch {
            try {
                val token = sessionManager.fetchAuthToken().toString()
                if (token.isNullOrEmpty()) {
                    navigateToLogin()
                    return@launch
                }
                Log.d(TAG, "token: ${token }")
                val response = RetrofitClient.instance.getLokasi("Bearer $token")
                Log.d(TAG, "loadLokasi: ${response}")

                if (response.isSuccessful) {
                    val lokasiResponse = response.body()
                    if (lokasiResponse != null) {
                        allLokasi = lokasiResponse.res
                        filteredLokasi = allLokasi
                        lokasiAdapter.submitList(filteredLokasi)
                        updateStats()
                        updateEmptyState()
                    }
                } else {
                    when (response.code()) {
                        400, 401 -> {
                            Toast.makeText(context, "Session expired", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        }
                        404 -> {
                            allLokasi = emptyList()
                            filteredLokasi = emptyList()
                            lokasiAdapter.submitList(filteredLokasi)
                            updateStats()
                            updateEmptyState()
                        }
                        else -> {
                            Toast.makeText(context, "Failed to load places", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Error nya: {${e.message.toString()}")
            }
        }
    }

    private fun filterLokasi(query: String) {
        filteredLokasi = if (query.isEmpty()) {
            allLokasi
        } else {
            allLokasi.filter {
                it.lokasiName.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        lokasiAdapter.submitList(filteredLokasi)
        updateEmptyState()
    }

    private fun updateStats() {
        totalPlacesCount.text = allLokasi.size.toString()
        // Menganggap semua places aktif untuk sekarang
        activePlacesCount.text = allLokasi.size.toString()
    }

    private fun updateEmptyState() {
        if (filteredLokasi.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
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
        if (validateAccess()) {
            loadLokasi() // Refresh data when returning to fragment
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminPlaceDashboardFragment()
    }
}