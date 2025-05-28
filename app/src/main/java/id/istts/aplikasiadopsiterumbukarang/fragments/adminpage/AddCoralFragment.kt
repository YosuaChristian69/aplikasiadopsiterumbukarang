package id.istts.aplikasiadopsiterumbukarang.fragments.coral

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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AddCoralFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var backButton: ImageButton
    private lateinit var titleText: TextView
    private lateinit var headerLayout: View
    private lateinit var formCard: CardView
    private lateinit var coralNameInput: TextInputLayout
    private lateinit var coralNameEditText: TextInputEditText
    private lateinit var coralTypeInput: TextInputLayout
    private lateinit var coralTypeEditText: TextInputEditText
    private lateinit var coralDescriptionInput: TextInputLayout
    private lateinit var coralDescriptionEditText: TextInputEditText
    private lateinit var coralLocationInput: TextInputLayout
    private lateinit var coralLocationEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_coral, container, false)
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
        backButton = view.findViewById(R.id.backButton)
        titleText = view.findViewById(R.id.titleText)
        headerLayout = view.findViewById(R.id.headerLayout)
        formCard = view.findViewById(R.id.formCard)
        coralNameInput = view.findViewById(R.id.coralNameInput)
        coralNameEditText = view.findViewById(R.id.coralNameEditText)
        coralTypeInput = view.findViewById(R.id.coralTypeInput)
        coralTypeEditText = view.findViewById(R.id.coralTypeEditText)
        coralDescriptionInput = view.findViewById(R.id.coralDescriptionInput)
        coralDescriptionEditText = view.findViewById(R.id.coralDescriptionEditText)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Set initial animation states
        setInitialStates()
        setupClickListeners()
    }

    private fun setInitialStates() {
        listOf(headerLayout, formCard, saveButton, cancelButton).forEach {
            it.alpha = 0f
        }

        headerLayout.translationY = -100f
        formCard.translationY = 50f
        saveButton.translationY = 30f
        cancelButton.translationY = 30f

        formCard.scaleX = 0.8f
        formCard.scaleY = 0.8f
    }

    private suspend fun startAnimations() {
        // Staggered entrance animations
        animateView(headerLayout, translationY = Pair(-100f, 0f), duration = 600)
        delay(200)

        animateCard()
        delay(300)

        animateButtons()
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

    private fun animateCard() {
        val scaleX = ObjectAnimator.ofFloat(formCard, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(formCard, "scaleY", 0.8f, 1f)
        val fadeIn = ObjectAnimator.ofFloat(formCard, "alpha", 0f, 1f)
        val translateY = ObjectAnimator.ofFloat(formCard, "translationY", 50f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn, translateY)
            duration = 600
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun animateButtons() {
        val buttons = listOf(saveButton, cancelButton)
        buttons.forEachIndexed { index, button ->
            val fadeIn = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f)
            val translateY = ObjectAnimator.ofFloat(button, "translationY", 30f, 0f)

            AnimatorSet().apply {
                playTogether(fadeIn, translateY)
                duration = 400
                startDelay = index * 100L
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            animateClick(backButton) { navigateBack() }
        }

        saveButton.setOnClickListener {
            animateClick(saveButton) {
                // TODO: Implement save logic
                navigateBack()
            }
        }

        cancelButton.setOnClickListener {
            animateClick(cancelButton) { navigateBack() }
        }
    }

    private fun animateClick(view: View, action: (() -> Unit)? = null) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)

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

    private fun navigateBack() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_addCoralFragment_to_adminDashboardFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddCoralFragment()
    }
}