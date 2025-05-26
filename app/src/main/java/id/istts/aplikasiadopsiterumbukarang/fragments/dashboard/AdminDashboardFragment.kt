package id.istts.aplikasiadopsiterumbukarang.fragments.dashboard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
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
import id.istts.aplikasiadopsiterumbukarang.fragments.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.fragments.models.CoralItem

class AdminDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: ImageButton
    private lateinit var profileCard: CardView
    private lateinit var logoutCard: CardView
    private lateinit var collectionTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var headerLayout: View

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
            startEntranceAnimations()
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
        // Initialize all views
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        profileCard = view.findViewById(R.id.profileCard)
        logoutCard = view.findViewById(R.id.logoutCard)
        collectionTitle = view.findViewById(R.id.collectionTitle)
        recyclerView = view.findViewById(R.id.recyclerViewCorals)
        fabAdd = view.findViewById(R.id.fabAdd)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
        headerLayout = view.findViewById(R.id.headerLayout)

        val userName = sessionManager.fetchUserName() ?: "Admin"
        welcomeTextView.text = "Hi, $userName"

        // Set initial states for animations
        setInitialAnimationStates()

        // Setup click listeners with animations
        setupClickListeners()
    }

    private fun setInitialAnimationStates() {
        // Hide elements initially for entrance animations
        headerLayout.alpha = 0f
        headerLayout.translationY = -100f

        recyclerView.alpha = 0f
        recyclerView.translationY = 50f

        fabAdd.alpha = 0f
        fabAdd.scaleX = 0f
        fabAdd.scaleY = 0f

        bottomNavigation.alpha = 0f
        bottomNavigation.translationY = 100f

        profileCard.scaleX = 0f
        profileCard.scaleY = 0f

        logoutCard.scaleX = 0f
        logoutCard.scaleY = 0f

        welcomeTextView.alpha = 0f
        welcomeTextView.translationX = -50f

        collectionTitle.alpha = 0f
        collectionTitle.translationX = -30f
    }

    private suspend fun startEntranceAnimations() {
        // Animate header slide down
        animateHeaderEntrance()

        delay(200)

        // Animate profile and logout cards
        animateCardsEntrance()

        delay(100)

        // Animate welcome text
        animateWelcomeText()

        delay(150)

        // Animate collection title
        animateCollectionTitle()

        delay(200)

        // Animate RecyclerView fade in
        animateRecyclerView()

        delay(100)

        // Animate FAB bounce in
        animateFAB()

        delay(150)

        // Animate bottom navigation slide up
        animateBottomNavigation()
    }

    private fun animateHeaderEntrance() {
        val fadeIn = ObjectAnimator.ofFloat(headerLayout, "alpha", 0f, 1f)
        val slideDown = ObjectAnimator.ofFloat(headerLayout, "translationY", -100f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideDown)
        animatorSet.duration = 600
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateCardsEntrance() {
        // Profile card animation
        val profileScaleX = ObjectAnimator.ofFloat(profileCard, "scaleX", 0f, 1f)
        val profileScaleY = ObjectAnimator.ofFloat(profileCard, "scaleY", 0f, 1f)

        val profileAnimator = AnimatorSet()
        profileAnimator.playTogether(profileScaleX, profileScaleY)
        profileAnimator.duration = 400
        profileAnimator.interpolator = OvershootInterpolator()
        profileAnimator.start()

        // Logout card animation with slight delay
        val logoutScaleX = ObjectAnimator.ofFloat(logoutCard, "scaleX", 0f, 1f)
        val logoutScaleY = ObjectAnimator.ofFloat(logoutCard, "scaleY", 0f, 1f)

        val logoutAnimator = AnimatorSet()
        logoutAnimator.playTogether(logoutScaleX, logoutScaleY)
        logoutAnimator.duration = 400
        logoutAnimator.startDelay = 100
        logoutAnimator.interpolator = OvershootInterpolator()
        logoutAnimator.start()
    }

    private fun animateWelcomeText() {
        val fadeIn = ObjectAnimator.ofFloat(welcomeTextView, "alpha", 0f, 1f)
        val slideRight = ObjectAnimator.ofFloat(welcomeTextView, "translationX", -50f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideRight)
        animatorSet.duration = 500
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateCollectionTitle() {
        val fadeIn = ObjectAnimator.ofFloat(collectionTitle, "alpha", 0f, 1f)
        val slideRight = ObjectAnimator.ofFloat(collectionTitle, "translationX", -30f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideRight)
        animatorSet.duration = 400
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateRecyclerView() {
        val fadeIn = ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(recyclerView, "translationY", 50f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideUp)
        animatorSet.duration = 500
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateFAB() {
        val fadeIn = ObjectAnimator.ofFloat(fabAdd, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(fabAdd, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(fabAdd, "scaleY", 0f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleX, scaleY)
        animatorSet.duration = 600
        animatorSet.interpolator = BounceInterpolator()
        animatorSet.start()
    }

    private fun animateBottomNavigation() {
        val fadeIn = ObjectAnimator.ofFloat(bottomNavigation, "alpha", 0f, 1f)
        val slideUp = ObjectAnimator.ofFloat(bottomNavigation, "translationY", 100f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideUp)
        animatorSet.duration = 500
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun setupClickListeners() {
        // Logout button with animation
        logoutButton.setOnClickListener {
            animateLogoutClick()
        }

        // FAB with pulse animation
        fabAdd.setOnClickListener {
            animateFABClick()
            // Add your FAB click logic here
        }

        // Profile card click animation
        profileCard.setOnClickListener {
            animateCardClick(profileCard)
        }
    }

    private fun animateLogoutClick() {
        // Scale animation for logout button
        val scaleDown = ObjectAnimator.ofFloat(logoutCard, "scaleX", 1f, 0.9f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(logoutCard, "scaleY", 1f, 0.9f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleDown, scaleDownY)
        animatorSet.duration = 200
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()

        // Perform logout after animation
        lifecycleScope.launch {
            delay(200)
            performLogout()
        }
    }

    private fun animateFABClick() {
        // Pulse animation for FAB
        val scaleX = ObjectAnimator.ofFloat(fabAdd, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(fabAdd, "scaleY", 1f, 1.2f, 1f)
        val rotation = ObjectAnimator.ofFloat(fabAdd, "rotation", 0f, 180f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, rotation)
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateCardClick(card: CardView) {
        // Gentle bounce animation for card clicks
        val scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.05f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 200
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun animateExitTransition() {
        // Animate elements out before navigation
        val headerFade = ObjectAnimator.ofFloat(headerLayout, "alpha", 1f, 0f)
        val headerSlide = ObjectAnimator.ofFloat(headerLayout, "translationY", 0f, -50f)

        val recyclerFade = ObjectAnimator.ofFloat(recyclerView, "alpha", 1f, 0f)

        val fabScale = ObjectAnimator.ofFloat(fabAdd, "scaleX", 1f, 0f)
        val fabScaleY = ObjectAnimator.ofFloat(fabAdd, "scaleY", 1f, 0f)

        val bottomFade = ObjectAnimator.ofFloat(bottomNavigation, "alpha", 1f, 0f)
        val bottomSlide = ObjectAnimator.ofFloat(bottomNavigation, "translationY", 0f, 50f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            headerFade, headerSlide, recyclerFade,
            fabScale, fabScaleY, bottomFade, bottomSlide
        )
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun navigateToLogin() {
        // Pastikan fragment masih aktif dan navigation controller tersedia
        if (isAdded && !isDetached && !isRemoving) {
            try {
                animateExitTransition()
                lifecycleScope.launch {
                    delay(300) // Wait for exit animation
                    if (isAdded && !isDetached && !isRemoving) {
                        findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
                    }
                }
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

        navigateToLogin()
    }

    override fun onResume() {
        super.onResume()
        if (!validateWorkerAccess()) {
            return
        }
    }

    // Add floating animation for FAB
    private fun startFABFloatingAnimation() {
        val floatingAnimation = ObjectAnimator.ofFloat(fabAdd, "translationY", 0f, -10f, 0f)
        floatingAnimation.duration = 2000
        floatingAnimation.repeatCount = ValueAnimator.INFINITE
        floatingAnimation.interpolator = AccelerateDecelerateInterpolator()
        floatingAnimation.start()
    }

    override fun onStart() {
        super.onStart()
        // Start continuous floating animation for FAB after initial entrance
        lifecycleScope.launch {
            delay(2000) // Wait for entrance animations to complete
            if (isAdded && !isDetached) {
                startFABFloatingAnimation()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminDashboardFragment()
    }
}