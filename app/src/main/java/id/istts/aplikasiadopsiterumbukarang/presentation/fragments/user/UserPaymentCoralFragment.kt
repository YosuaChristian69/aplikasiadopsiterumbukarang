package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentUserPaymentCoralBinding
import java.text.NumberFormat
import java.util.Locale


class UserPaymentCoralFragment : Fragment() {

    private var _binding: FragmentUserPaymentCoralBinding? = null
    private val binding get() = _binding!!

    // Use Safe Args to get the arguments passed from the previous screen
    private val args: UserPaymentCoralFragmentArgs by navArgs()

    // You will need a ViewModel for this screen to fetch coral details
    // private lateinit var viewModel: UserPaymentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserPaymentCoralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setupViewModel()
        setupClickListeners()
        observeViewModel()

        // Tell the ViewModel to fetch details for the coralId we received
        // viewModel.loadCoralDetails(args.coralId)
    }

    private fun setupClickListeners() {
        // Handle the cancel button click
        binding.btnCancelPayment.setOnClickListener {
            // This navigates the user back to a main screen, clearing the adoption flow
            // from the back stack. Replace with your actual main screen destination.
            findNavController().navigate(R.id.action_global_userDashboardFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPayNow.setOnClickListener {
            // Handle the payment logic here
        }
    }

    private fun observeViewModel() {
        // Create an observer for your ViewModel's data
        // viewModel.coralDetails.observe(viewLifecycleOwner) { coral ->
        //    if (coral != null) {
        //        val adminFee = 2500
        //        val total = coral.harga_tk + adminFee
        //
        //        binding.tvCoralName.text = "${coral.tk_name} Adoption"
        //        binding.tvCoralPrice.text = formatToRupiah(coral.harga_tk)
        //        binding.tvAdminFee.text = formatToRupiah(adminFee)
        //        binding.tvTotalAmount.text = formatToRupiah(total)
        //    }
        // }
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