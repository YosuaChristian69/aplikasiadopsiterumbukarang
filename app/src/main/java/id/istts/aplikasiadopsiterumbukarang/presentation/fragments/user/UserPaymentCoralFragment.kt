package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentUserPaymentCoralBinding
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.TransactionRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserPaymentViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserPaymentViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.service.ApiService
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val FAKE_PAYMENT_FOR_DEV = true // SET TO true TO SKIP MIDTRANS, false FOR REAL TESTING

class UserPaymentCoralFragment : Fragment() {
//    private val apiService = RetrofitClient.instance

    private var _binding: FragmentUserPaymentCoralBinding? = null
    private val binding get() = _binding!!

    // Use Safe Args to get the arguments passed from the previous screen
    private val args: UserPaymentCoralFragmentArgs by navArgs()
    private lateinit var viewModel: UserPaymentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("PAYMENT_LIFECYCLE", "onCreateView called.")
        _binding = FragmentUserPaymentCoralBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We will wrap the entire setup in a try-catch block to find any hidden crash.
        try {
            Log.d("PAYMENT_LIFECYCLE", "onViewCreated: Starting setup...")

            setupViewModel()
            Log.d("PAYMENT_LIFECYCLE", "onViewCreated: setupViewModel() finished.")

            setupClickListeners()
            Log.d("PAYMENT_LIFECYCLE", "onViewCreated: setupClickListeners() finished.")

            observeViewModel()
            Log.d("PAYMENT_LIFECYCLE", "onViewCreated: observeViewModel() finished.")

            // This line is our final goal.
            Log.d("PAYMENT_LIFECYCLE", "onViewCreated: About to call viewModel.loadCoralDetails().")
            viewModel.loadCoralDetails(args.coralId)
            Log.d("PAYMENT_LIFECYCLE", "onViewCreated: viewModel.loadCoralDetails() was successfully called.")

        } catch (e: Exception) {
            // If any part of the setup crashes, this will catch it.
            Log.e("PAYMENT_LIFECYCLE", "A CRASH occurred during fragment setup!", e)
            Toast.makeText(requireContext(), "Critical setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupViewModel() {
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: START")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: Creating ApiService...")
        val apiService = RetrofitClient.instance
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: ApiService created.")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: Creating SessionManager...")
        val sessionManager = SessionManager(requireContext())
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: SessionManager created.")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: Creating CoralRepositoryImpl...")
        val coralRepo = CoralRepositoryImpl()
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: CoralRepositoryImpl created.")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: Creating TransactionRepositoryImpl...")
        val transactionRepo = TransactionRepositoryImpl(apiService, sessionManager)
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: TransactionRepositoryImpl created.")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: Creating ViewModelFactory...")
        val viewModelFactory = UserPaymentViewModelFactory(coralRepo, transactionRepo, sessionManager)
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: ViewModelFactory created.")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: Creating ViewModel instance...")
        viewModel = ViewModelProvider(this, viewModelFactory)[UserPaymentViewModel::class.java]
        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: ViewModel instance created.")

        Log.d("PAYMENT_LIFECYCLE", "setupViewModel: FINISHED")
    }

    private fun setupClickListeners() {
        binding.btnCancelPayment.setOnClickListener {
            findNavController().navigate(R.id.action_global_userDashboardFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPayNow.setOnClickListener {
            // Get the details from the navigation arguments
            val nickname = args.coralNickname.toString()
            val message = args.message.toString()
            val locationId = args.locationId

            // Call the ViewModel to start the payment process
            viewModel.initiatePayment(locationId, nickname, message)
        }
    }

    // In UserPaymentCoralFragment.kt

    private fun observeViewModel() {
        // Observer 1: For the uiState StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Control the visibility of the entire loading layout group
                binding.layoutLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                // Show/hide the main content based on loading state
                binding.mainContentContainer.visibility = if (state.isLoading) View.GONE else View.VISIBLE // Assuming your CardViews are in a group with this ID

                state.coralDetails?.let { coral ->
                    val adminFee = 2500 // You can make this a constant
                    val total = coral.harga_tk + adminFee
                    binding.tvCoralName.text = "${coral.tk_name} Adoption"
                    binding.tvCoralPrice.text = formatToRupiah(coral.harga_tk)
                    binding.tvAdminFee.text = formatToRupiah(adminFee)
                    binding.tvTotalAmount.text = formatToRupiah(total)
                }

                state.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    // You might want a viewModel.clearError() function here
                }
            }
        }

        // Observer 2: For the midtransResponse LiveData
        viewModel.midtransResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (FAKE_PAYMENT_FOR_DEV) {
                    // --- FAKE PATH: For faster frontend development ---
                    Log.d("DEV_MODE", "Faking successful payment...")

                    viewModel.manuallyFulfillOrderForDebug(
                        args.coralId,
                        args.locationId,
                        1 // amount
                    )

                    val action = UserPaymentCoralFragmentDirections
                        .actionUserPaymentCoralFragmentToAdoptionSuccessFragment()
                    findNavController().navigate(action)

                } else {
                    // --- REAL PATH: For actual end-to-end testing ---
                    val snapToken = response.token
//                    MidtransSDK.getInstance().startPaymentUiFlow(requireActivity(), snapToken)
                }
            }
        }
    }

    // Helper function to format numbers into Indonesian Rupiah currency
    private fun formatToRupiah(amount: Int): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.minimumFractionDigits = 0 // Don't show decimal points
        return numberFormat.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}