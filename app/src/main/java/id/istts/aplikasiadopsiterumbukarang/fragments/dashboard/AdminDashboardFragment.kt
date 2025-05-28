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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        lifecycleScope.launch {
            if (validateAccess()) {
                setupViews(view)
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
        // Staggered entrance animations
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

        // Start floating animation
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
            playTogether(animators as Collection<android.animation.Animator>)
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

    private fun animateScale(view: View, interpolator: android.view.animation.Interpolator,
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
            // FAB specific animation with rotation
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
            navigtateToAddCoral()
        }

        profileCard.setOnClickListener { animateClick(profileCard) }
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
    private fun navigtateToAddCoral(){
        findNavController().navigate(R.id.action_adminDashboardFragment_to_addCoralFragment2)
    }

    private fun performLogout() {
        if (isAdded && !isDetached) {
            sessionManager.clearSession()
            navigateToLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        validateAccess()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminDashboardFragment()
    }
}