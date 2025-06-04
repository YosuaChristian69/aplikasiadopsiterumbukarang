package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AdminDashboardUiState
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AdminDashboardViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AdminDashboardViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.NavigationEvent
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl

class AdminDashboardFragment : Fragment() {

    private val viewModel: AdminDashboardViewModel by viewModels {
        AdminDashboardViewModelFactory(
            CoralRepositoryImpl(/* inject your API service here */),
            SessionManager(requireContext())
        )
    }

    private lateinit var coralAdapter: CoralAdapter

    // View references
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: ImageButton
    private lateinit var profileCard: CardView
    private lateinit var logoutCard: CardView
    private lateinit var collectionTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var headerLayout: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        lifecycleScope.launch {
            startAnimations()
        }
    }

    private fun setupViews(view: View) {
        // Initialize views
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        profileCard = view.findViewById(R.id.profileCard)
        logoutCard = view.findViewById(R.id.logoutCard)
        collectionTitle = view.findViewById(R.id.collectionTitle)
        recyclerView = view.findViewById(R.id.recyclerViewCorals)
        fabAdd = view.findViewById(R.id.fabAdd)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
        headerLayout = view.findViewById(R.id.headerLayout)

        // Set initial animation states
        setInitialStates()
    }

    private fun setupRecyclerView() {
        coralAdapter = CoralAdapter { coral ->
            viewModel.onCoralItemClick(coral)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = coralAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }

        // Observe navigation events
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                event?.let {
                    handleNavigationEvent(it)
                    viewModel.clearNavigationEvent()
                }
            }
        }
    }

    private fun updateUI(uiState: AdminDashboardUiState) {
        // Update welcome message
        if (uiState.welcomeMessage.isNotEmpty()) {
            welcomeTextView.text = uiState.welcomeMessage
        }

        // Update collection title
        collectionTitle.text = uiState.collectionTitle

        // Update coral list
        coralAdapter.updateData(uiState.coralList)

        // Update Total Corals
        val totalCoralsView = view?.findViewById<TextView>(R.id.totalCoralsText) // Use safe call ?.
        totalCoralsView?.text = uiState.totalCorals.toString()                   // Use safe call ?.

        // Update Low Stock
        val lowStockView = view?.findViewById<TextView>(R.id.lowStockText)       // Use safe call ?.
        lowStockView?.text = uiState.lowStockCount.toString()                    // Use safe call ?.

        // Handle loading state
        if (uiState.isLoading) {
            // Show loading indicator if needed
            Log.d("AdminDashboard", "Loading coral data...")
        }

        // Handle error state
        uiState.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        Log.d("AdminDashboard", "UI updated with ${uiState.coralList.size} corals")
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToLogin -> {
                if (isAdded && !isDetached && !isRemoving) {
                    try {
                        findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
                    } catch (e: Exception) {
                        Log.e("AdminDashboard", "Navigation error", e)
                    }
                }
            }
            is NavigationEvent.NavigateToAddCoral -> {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_addCoralFragment)
            }
            is NavigationEvent.NavigateToAdminPlace -> {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_adminPlaceDashboardFragment)
            }
            is NavigationEvent.NavigateToAdminWorker -> {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_adminWorkerDashboardFragment)
            }
            is NavigationEvent.ShowCoralDetail -> {
                Toast.makeText(requireContext(), "Clicked: ${event.coral.tk_name}", Toast.LENGTH_SHORT).show()
                // Navigate to detail if needed
            }
        }
    }

    private fun setInitialStates() {
        listOf(headerLayout, recyclerView, fabAdd, bottomNavigation,
            profileCard, logoutCard, welcomeTextView, collectionTitle).forEach {
            it.alpha = 0f
        }

        headerLayout.translationY = -100f
        recyclerView.translationY = 50f
        bottomNavigation.translationY = 100f
        welcomeTextView.translationX = -50f
        collectionTitle.translationX = -30f

        listOf(fabAdd, profileCard, logoutCard).forEach {
            it.scaleX = 0f
            it.scaleY = 0f
        }
    }

    private suspend fun startAnimations() {
        animateView(headerLayout, translationY = Pair(-100f, 0f), duration = 600)
        delay(200)

        animateCards()
        delay(100)

        animateView(welcomeTextView, translationX = Pair(-50f, 0f), duration = 500)
        delay(150)

        animateView(collectionTitle, translationX = Pair(-30f, 0f), duration = 400)
        delay(200)

        animateView(recyclerView, translationY = Pair(50f, 0f), duration = 500)
        delay(100)

        animateScale(fabAdd, BounceInterpolator(), 600)
        delay(150)

        animateView(bottomNavigation, translationY = Pair(100f, 0f), duration = 500)

        delay(2000)
        if (isAdded) startFloatingAnimation()
    }

    private fun animateView(view: View, translationY: Pair<Float, Float>? = null,
                            translationX: Pair<Float, Float>? = null, duration: Long = 400) {
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val animators = mutableListOf<ObjectAnimator>(fadeIn)

        translationY?.let { (from, to) ->
            animators.add(ObjectAnimator.ofFloat(view, "translationY", from, to))
        }

        translationX?.let { (from, to) ->
            animators.add(ObjectAnimator.ofFloat(view, "translationX", from, to))
        }

        AnimatorSet().apply {
            playTogether(animators as Collection<Animator>)
            this.duration = duration
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun animateCards() {
        val cards = listOf(profileCard to 0L, logoutCard to 100L)
        cards.forEach { (card, delayTime) ->
            animateScale(card, OvershootInterpolator(), 400, delayTime)
        }
    }

    private fun animateScale(view: View, interpolator: Interpolator,
                             duration: Long, startDelay: Long = 0) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn)
            this.duration = duration
            this.startDelay = startDelay
            this.interpolator = interpolator
            start()
        }
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            animateClick(logoutCard) { viewModel.onLogoutClick() }
        }

        fabAdd.setOnClickListener {
            val animators = listOf(
                ObjectAnimator.ofFloat(fabAdd, "scaleX", 1f, 1.2f, 1f),
                ObjectAnimator.ofFloat(fabAdd, "scaleY", 1f, 1.2f, 1f),
                ObjectAnimator.ofFloat(fabAdd, "rotation", 0f, 180f)
            )

            AnimatorSet().apply {
                playTogether(animators)
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            viewModel.onAddCoralClick()
        }

        profileCard.setOnClickListener { animateClick(profileCard) }

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_coral_seed -> {
                    true
                }
                R.id.nav_place -> {
                    animateBottomNavClick(menuItem) {
                        viewModel.onPlaceNavClick()
                    }
                    true
                }
                R.id.nav_worker -> {
                    animateBottomNavClick(menuItem) {
                        viewModel.onWorkerNavClick()
                    }
                    true
                }
                else -> false
            }
        }

        bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_coral_seed -> {
                    recyclerView.smoothScrollToPosition(0)
                }
                R.id.nav_place -> {
                    recyclerView.smoothScrollToPosition(1)
                }
                R.id.nav_worker -> {
                    recyclerView.smoothScrollToPosition(2)
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

    private fun animateClick(view: View, action: (() -> Unit)? = null) {
        val scale = if (view == logoutCard) 0.9f else 1.05f
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scale, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scale, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        action?.let {
            lifecycleScope.launch {
                delay(200)
                it()
            }
        }
    }

    private fun startFloatingAnimation() {
        ObjectAnimator.ofFloat(fabAdd, "translationY", 0f, -10f, 0f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminDashboardFragment()
    }
}