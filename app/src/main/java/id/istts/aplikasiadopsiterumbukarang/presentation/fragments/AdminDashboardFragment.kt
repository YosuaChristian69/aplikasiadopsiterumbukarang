package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import CoralRepository
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
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class AdminDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var coralRepository: CoralRepository
    private lateinit var coralAdapter: CoralAdapter

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
        sessionManager = SessionManager(requireContext())
        coralRepository = CoralRepositoryImpl()

        lifecycleScope.launch {
            if (validateAccess()) {
                setupViews(view)
                setupRecyclerView()
                loadCoralData()
                startAnimations()
            }
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
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        profileCard = view.findViewById(R.id.profileCard)
        logoutCard = view.findViewById(R.id.logoutCard)
        collectionTitle = view.findViewById(R.id.collectionTitle)
        recyclerView = view.findViewById(R.id.recyclerViewCorals)
        fabAdd = view.findViewById(R.id.fabAdd)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
        headerLayout = view.findViewById(R.id.headerLayout)

        welcomeTextView.text = "Hi, ${sessionManager.fetchUserName() ?: "Admin"}"

        // Set initial animation states
        setInitialStates()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        coralAdapter = CoralAdapter { coral ->
            onCoralItemClick(coral)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = coralAdapter
            setHasFixedSize(true)
        }
    }

    private fun onCoralItemClick(coral: Coral) {
        Toast.makeText(requireContext(), "Clicked: ${coral.tk_name}", Toast.LENGTH_SHORT).show()
        // Example: navigate to detail fragment
        // findNavController().navigate(R.id.action_adminDashboardFragment_to_coralDetailFragment)
    }

    private fun loadCoralData() {
        val token = sessionManager.fetchAuthToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Authentication token not found", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val result = coralRepository.getCoralList(authToken)

                if (result.isSuccess) {
                    val coralList = result.getOrNull() ?: emptyList()
                    coralAdapter.updateData(coralList)

                    val count = coralList.size
                    collectionTitle.text = "MY CORAL'S SEEDS COLLECTION ($count)"

                    Log.d("AdminDashboard", "Successfully loaded ${coralList.size} corals")
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("AdminDashboard", "Failed to load coral data", error)

                    val errorMessage = error?.message ?: "Unknown error occurred"
                    if (errorMessage.contains("Invalid or Expired Token") || errorMessage.contains("401")) {
                        Toast.makeText(requireContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show()
                        navigateToLogin()
                    } else {
                        Toast.makeText(requireContext(), "Failed to load coral data: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboard", "Exception while loading coral data", e)
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
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
            animateClick(logoutCard) { performLogout() }
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
            navigateToAddCoral()
        }

        profileCard.setOnClickListener { animateClick(profileCard) }
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_coral_seed -> {
                    true
                }
                R.id.nav_place -> {
                    animateBottomNavClick(menuItem) {
                        navigateToAdminPlace()
                    }
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
    private fun navigateToAdminPlace(){
        findNavController().navigate(R.id.action_adminDashboardFragment_to_adminPlaceDashboardFragment)
    }
    private fun navigateToAdminWorker(){
        findNavController().navigate(R.id.action_adminDashboardFragment_to_adminWorkerDashboardFragment)
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

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToAddCoral() {
        findNavController().navigate(R.id.action_adminDashboardFragment_to_addCoralFragment)
    }

    private fun performLogout() {
        if (isAdded && !isDetached) {
            sessionManager.clearSession()
            navigateToLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        if (validateAccess()) {
            loadCoralData()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminDashboardFragment()
    }
}