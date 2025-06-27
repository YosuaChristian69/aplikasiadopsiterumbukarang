package id.istts.aplikasiadopsiterumbukarang.presentation.fragments

import android.app.AlertDialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
//import com.google.android.gms.safetynet.SafetyNet
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.register.RegisterViewModel

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    // UI Components
    private lateinit var videoView: VideoView
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var verificationCodeEditText: TextInputEditText
    private lateinit var getCodeButton: MaterialButton
    private lateinit var termsCheckbox: CheckBox
    private lateinit var registerButton: MaterialButton
    private lateinit var loginLink: TextView

    // reCAPTCHA Components
    private lateinit var recaptchaButton: MaterialButton
    private lateinit var recaptchaProgress: ProgressBar
    private lateinit var recaptchaStatus: TextView

    // Timer for countdown
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false

    // reCAPTCHA Site Key - Replace with your actual site key
    private val RECAPTCHA_SITE_KEY = "6LfAxlYrAAAAAKJXzqB-dHwA-2FiJMZwrw9XXqWu"

    companion object {
        private const val TAG = "RegisterFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        initializeViewModel()
        setupVideoBackground(view)
        animateRegisterCard(view)
        setupClickListeners()
        setupTermsAndConditionsLink()
        observeViewModel()
        updateRegisterButtonState() // Initial state check
    }

    private fun initializeViews(view: View) {
        fullNameEditText = view.findViewById(R.id.fullNameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        verificationCodeEditText = view.findViewById(R.id.verificationCodeEditText)
        getCodeButton = view.findViewById(R.id.getCodeButton)
        termsCheckbox = view.findViewById(R.id.termsCheckbox)
        registerButton = view.findViewById(R.id.registerButton)
        loginLink = view.findViewById(R.id.loginLink)
        videoView = view.findViewById(R.id.videoBackground)

        // reCAPTCHA views
        recaptchaButton = view.findViewById(R.id.recaptchaButton)
        recaptchaProgress = view.findViewById(R.id.recaptchaProgress)
        recaptchaStatus = view.findViewById(R.id.recaptchaStatus)
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
    }

    private fun setupVideoBackground(view: View) {
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.coral_background_video
        val uri = Uri.parse(videoPath)

        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            videoView.start()
        }

        videoView.setOnErrorListener { _, _, _ -> true }
    }

    private fun animateRegisterCard(view: View) {
        val registerCard = view.findViewById<CardView>(R.id.registerCard)

        registerCard.alpha = 0f
        registerCard.translationY = 100f

        registerCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupClickListeners() {
        loginLink.setOnClickListener {
            viewModel.navigateToLogin()
        }

        getCodeButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            viewModel.requestVerificationCode(fullName, email, password, confirmPassword)
        }

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val verificationCode = verificationCodeEditText.text.toString().trim()
            val termsAccepted = termsCheckbox.isChecked

            if (viewModel.validateAllInputs(fullName, email, password, confirmPassword, verificationCode, termsAccepted)) {
                viewModel.verifyAndRegister(email, verificationCode)
            }
        }

        // reCAPTCHA button click listener
        recaptchaButton.setOnClickListener {
            startRecaptchaVerification()
        }

        // Add listeners to enable/disable register button based on form completion
        setupFormValidationListeners()
    }

    private fun setupFormValidationListeners() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateRegisterButtonState()
            }
        }

        fullNameEditText.addTextChangedListener(textWatcher)
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
        confirmPasswordEditText.addTextChangedListener(textWatcher)
        verificationCodeEditText.addTextChangedListener(textWatcher)

        termsCheckbox.setOnCheckedChangeListener { _, _ ->
            updateRegisterButtonState()
        }
    }

    private fun updateRegisterButtonState() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val verificationCode = verificationCodeEditText.text.toString().trim()
        val termsAccepted = termsCheckbox.isChecked
        val recaptchaVerified = viewModel.isRecaptchaVerified()

        val isFormValid = fullName.isNotEmpty() &&
                email.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                verificationCode.isNotEmpty() &&
                termsAccepted &&
                recaptchaVerified

        registerButton.isEnabled = isFormValid

        // Update button appearance based on state
        if (isFormValid) {
            registerButton.alpha = 1.0f
        } else {
            registerButton.alpha = 0.6f
        }
    }
    private fun showRecaptchaWebView() {
        val webView = WebView(requireContext())

        // Enable all necessary WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            allowContentAccess = true
            allowFileAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }

        // Add WebView client to handle page loading
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "WebView page finished loading")
            }

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "WebView error: ${error?.description}")
            }
        }

        // Improved HTML content
        val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>reCAPTCHA Verification</title>
            <script src="https://www.google.com/recaptcha/api.js" async defer></script>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 430px;
                    margin: 0;
                    padding: 20px;
                    background-color: #f5f5f5;
                }
                .recaptcha-container {
                    background: white;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    text-align: center;
                }
                .loading {
                    color: #666;
                    font-size: 14px;
                }
            </style>
        </head>
        <body>
            <div class="recaptcha-container">
                <div id="loading" class="loading">Loading reCAPTCHA...</div>
                <div id="recaptcha-element"></div>
            </div>
            
            <script>
                console.log('Script started');
                
                function onRecaptchaReady() {
                    console.log('reCAPTCHA ready');
                    document.getElementById('loading').style.display = 'none';
                    
                    try {
                        grecaptcha.render('recaptcha-element', {
                            'sitekey': '$RECAPTCHA_SITE_KEY',
                            'callback': function(token) {
                                console.log('reCAPTCHA success, token:', token);
                                if (typeof Android !== 'undefined') {
                                    Android.onRecaptchaSuccess(token);
                                } else {
                                    console.error('Android interface not available');
                                }
                            },
                            'expired-callback': function() {
                                console.log('reCAPTCHA expired');
                                if (typeof Android !== 'undefined') {
                                    Android.onRecaptchaExpired();
                                } else {
                                    console.error('Android interface not available');
                                }
                            },
                            'error-callback': function() {
                                console.log('reCAPTCHA error');
                                if (typeof Android !== 'undefined') {
                                    Android.onRecaptchaError();
                                } else {
                                    console.error('Android interface not available');
                                }
                            }
                        });
                    } catch (error) {
                        console.error('Error rendering reCAPTCHA:', error);
                        document.getElementById('loading').innerHTML = 'Error loading reCAPTCHA: ' + error.message;
                    }
                }
                
                // Wait for grecaptcha to be available
                function waitForRecaptcha() {
                    if (typeof grecaptcha !== 'undefined' && grecaptcha.render) {
                        onRecaptchaReady();
                    } else {
                        console.log('Waiting for grecaptcha...');
                        setTimeout(waitForRecaptcha, 100);
                    }
                }
                
                // Start checking when DOM is ready
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', waitForRecaptcha);
                } else {
                    waitForRecaptcha();
                }
            </script>
        </body>
        </html>
    """.trimIndent()

        // Enhanced JavaScript interface
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onRecaptchaSuccess(token: String) {
                Log.d(TAG, "reCAPTCHA success: $token")
                requireActivity().runOnUiThread {
                    viewModel.onRecaptchaSuccess(token)
                    dismissRecaptchaDialog()
                }
            }

            @JavascriptInterface
            fun onRecaptchaExpired() {
                Log.d(TAG, "reCAPTCHA expired")
                requireActivity().runOnUiThread {
                    viewModel.onRecaptchaFailure("reCAPTCHA expired")
                    dismissRecaptchaDialog()
                }
            }

            @JavascriptInterface
            fun onRecaptchaError() {
                Log.d(TAG, "reCAPTCHA error")
                requireActivity().runOnUiThread {
                    viewModel.onRecaptchaFailure("reCAPTCHA error occurred")
                    dismissRecaptchaDialog()
                }
            }
        }, "Android")

        // Create dialog with larger size
        recaptchaDialog = AlertDialog.Builder(requireContext())
            .setTitle("Verify you're not a robot")
            .setView(webView)
            .setNegativeButton("Cancel") { dialog, _ ->
                viewModel.onRecaptchaFailure("Cancelled by user")
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        // Set dialog size
        recaptchaDialog?.setOnShowListener {
            val window = recaptchaDialog?.window
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                (resources.displayMetrics.heightPixels * 0.7).toInt()
            )
        }

        recaptchaDialog?.show()

        // Load HTML content
        webView.loadDataWithBaseURL(
            "https://www.google.com",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun startRecaptchaVerification() {
        showRecaptchaWebView()
    }
    private var recaptchaDialog: AlertDialog? = null

    private fun dismissRecaptchaDialog() {
        recaptchaDialog?.dismiss()
        recaptchaDialog = null
    }
    private fun setupTermsAndConditionsLink() {
        val fullText = getString(R.string.terms_and_conditions_checkbox_text)
        val linkText = getString(R.string.terms_and_conditions_link_text)

        val spannableString = SpannableString(Html.fromHtml(fullText, Html.FROM_HTML_MODE_LEGACY))

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents()
                showTermsAndConditionsDialog()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.ocean_blue)
            }
        }

        val startIndex = spannableString.toString().indexOf(linkText)
        if (startIndex != -1) {
            val endIndex = startIndex + linkText.length
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        termsCheckbox.text = spannableString
        termsCheckbox.movementMethod = LinkMovementMethod.getInstance()
        termsCheckbox.highlightColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
    }

    private fun showTermsAndConditionsDialog() {
        val termsAndConditionsHtml = getString(R.string.terms_and_conditions_content)
        val message = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(termsAndConditionsHtml, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(termsAndConditionsHtml)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_terms_and_conditions))
            .setMessage(message)
            .setPositiveButton(getString(R.string.dialog_action_accept)) { dialog, _ ->
                termsCheckbox.isChecked = true
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_action_close)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.isGetCodeLoading.observe(viewLifecycleOwner) { isLoading ->
            setGetCodeButtonLoading(isLoading)
        }

        viewModel.isRegisterLoading.observe(viewLifecycleOwner) { isLoading ->
            setRegisterButtonLoading(isLoading)
        }

        // Observe reCAPTCHA loading state
        viewModel.isRecaptchaLoading.observe(viewLifecycleOwner) { isLoading ->
            setRecaptchaLoading(isLoading)
        }

        viewModel.verificationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.VerificationState.Idle -> {}
                is RegisterViewModel.VerificationState.Loading -> {
                    clearFieldErrors()
                }
                is RegisterViewModel.VerificationState.Success -> {}
                is RegisterViewModel.VerificationState.Error -> {}
            }
        }

        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegisterState.Idle -> {}
                is RegisterViewModel.RegisterState.Loading -> {
                    clearFieldErrors()
                }
                is RegisterViewModel.RegisterState.Success -> {}
                is RegisterViewModel.RegisterState.Error -> {}
            }
        }

        // Observe reCAPTCHA state
        viewModel.recaptchaState.observe(viewLifecycleOwner) { state ->
            handleRecaptchaState(state)
        }

        viewModel.fieldError.observe(viewLifecycleOwner) { fieldError ->
            showFieldError(fieldError)
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            showSuccessMessage(message)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showErrorMessage(message)
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            handleNavigationEvent(event)
        }

        viewModel.timerEvent.observe(viewLifecycleOwner) { event ->
            handleTimerEvent(event)
        }

        viewModel.fieldsEnabledState.observe(viewLifecycleOwner) { enabled ->
            setFieldsEnabled(enabled)
        }
    }

    private fun handleRecaptchaState(state: RegisterViewModel.RecaptchaState) {
        when (state) {
            is RegisterViewModel.RecaptchaState.Idle -> {
                recaptchaStatus.visibility = View.GONE
                recaptchaButton.text = "Verify I'm not a robot"
                recaptchaButton.isEnabled = true
                updateRegisterButtonState()
            }
            is RegisterViewModel.RecaptchaState.Loading -> {
                recaptchaStatus.visibility = View.GONE
                recaptchaButton.text = "Verifying..."
                recaptchaButton.isEnabled = false
            }
            is RegisterViewModel.RecaptchaState.Success -> {
                recaptchaStatus.visibility = View.VISIBLE
                recaptchaStatus.text = "✓ Verification successful"
                recaptchaStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.sea_green))
                recaptchaButton.text = "✓ Verified"
                recaptchaButton.isEnabled = false
                updateRegisterButtonState()
            }
            is RegisterViewModel.RecaptchaState.Error -> {
                recaptchaStatus.visibility = View.VISIBLE
                recaptchaStatus.text = "✗ ${state.message}"
                recaptchaStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_color))
                recaptchaButton.text = "Retry Verification"
                recaptchaButton.isEnabled = true
                updateRegisterButtonState()
            }
        }
    }

    private fun setRecaptchaLoading(isLoading: Boolean) {
        if (isLoading) {
            recaptchaProgress.visibility = View.VISIBLE
            recaptchaButton.isEnabled = false
        } else {
            recaptchaProgress.visibility = View.GONE
        }
    }

    private fun showFieldError(fieldError: RegisterViewModel.FieldError) {
        when (fieldError.field) {
            RegisterViewModel.FieldType.FULL_NAME -> {
                fullNameEditText.error = fieldError.message
                fullNameEditText.requestFocus()
            }
            RegisterViewModel.FieldType.EMAIL -> {
                emailEditText.error = fieldError.message
                emailEditText.requestFocus()
            }
            RegisterViewModel.FieldType.PASSWORD -> {
                passwordEditText.error = fieldError.message
                passwordEditText.requestFocus()
            }
            RegisterViewModel.FieldType.CONFIRM_PASSWORD -> {
                confirmPasswordEditText.error = fieldError.message
                confirmPasswordEditText.requestFocus()
            }
            RegisterViewModel.FieldType.VERIFICATION_CODE -> {
                verificationCodeEditText.error = fieldError.message
                verificationCodeEditText.requestFocus()
            }
        }
    }

    private fun clearFieldErrors() {
        fullNameEditText.error = null
        emailEditText.error = null
        passwordEditText.error = null
        confirmPasswordEditText.error = null
        verificationCodeEditText.error = null
    }

    private fun handleNavigationEvent(event: RegisterViewModel.NavigationEvent) {
        when (event) {
            is RegisterViewModel.NavigationEvent.NavigateToLogin -> {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            is RegisterViewModel.NavigationEvent.ShowSuccessDialog -> {
                showSuccessDialog()
            }
        }
    }

    private fun handleTimerEvent(event: RegisterViewModel.TimerEvent) {
        when (event) {
            is RegisterViewModel.TimerEvent.Start -> {
                startTimer(event.milliseconds)
            }
            is RegisterViewModel.TimerEvent.Finished -> {
                onTimerFinished()
            }
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        fullNameEditText.isEnabled = enabled
        emailEditText.isEnabled = enabled
        passwordEditText.isEnabled = enabled
        confirmPasswordEditText.isEnabled = enabled
    }

    private fun startTimer(milliseconds: Long) {
        timer?.cancel()

        timer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                isTimerRunning = true
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                getCodeButton.text = String.format("%02d:%02d", minutes, seconds)
                getCodeButton.isEnabled = false
            }

            override fun onFinish() {
                onTimerFinished()
                viewModel.onTimerFinished()
            }
        }.start()
    }

    private fun onTimerFinished() {
        isTimerRunning = false
        getCodeButton.text = "Get Code"
        getCodeButton.isEnabled = true
    }

    private fun setGetCodeButtonLoading(isLoading: Boolean) {
        if (isLoading) {
            getCodeButton.isEnabled = false
            getCodeButton.text = "Sending..."
        } else if (!isTimerRunning) {
            getCodeButton.isEnabled = true
            getCodeButton.text = "Get Code"
        }
    }

    private fun setRegisterButtonLoading(isLoading: Boolean) {
        if (isLoading) {
            registerButton.isEnabled = false
            registerButton.text = "REGISTERING..."
        } else {
            updateRegisterButtonState() // Use the validation logic instead of just enabling
            registerButton.text = "REGISTER"
        }
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Registration Successful")
            .setMessage("Your account has been created successfully. Please login to continue.")
            .setPositiveButton("Login") { _, _ ->
                viewModel.navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::videoView.isInitialized && !videoView.isPlaying) {
            videoView.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::videoView.isInitialized && videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()

    }
}