package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentUserAddCoralBinding // <- 1. Import the generated View Binding class
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.UserAddCoralViewModel
//import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserAddCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserAddCoralViewModelFactory // <- 2. Import the Factory
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.launch

class UserAddCoralFragment : Fragment() {

    // 3. Setup View Binding
    private var _binding: FragmentUserAddCoralBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserAddCoralViewModel

    // 4. Declare ViewModel and navArgs as class properties
    private val args: UserAddCoralFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use View Binding to inflate the layout
        _binding = FragmentUserAddCoralBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This is the main setup flow
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        setupLocationResultListener()
        viewModel.loadCoralDetails(args.coralId)
    }

    private fun setupViewModel() {
        // 1. You create instances of the dependencies your ViewModel needs.
        val coralRepository = CoralRepositoryImpl(/* ... */)
        val sessionManager = SessionManager(requireContext())

        // 2. You pass those dependencies into your custom factory.
        val viewModelFactory = UserAddCoralViewModelFactory(coralRepository, sessionManager)

        // 3. You give that factory to the ViewModelProvider.
        // The ViewModelProvider then uses your factory to correctly build the ViewModel.
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserAddCoralViewModel::class.java)
    }
    private fun setupLocationResultListener() {
        setFragmentResultListener("locationSelectionRequest") { _, bundle ->
            val locationId = bundle.getInt("selectedLocationId")
            val locationName = bundle.getString("selectedLocationName")

            if (locationName != null) {
                // Update the ViewModel with the selected location
                viewModel.onLocationSelected(locationId, locationName)
            }
        }
    }

    private fun setupClickListeners() {
        // Use the 'binding' object to access views safely
        // In setupClickListeners()
        binding.btnBack.setOnClickListener {
            // This simply takes the user to the previous screen on the back stack.
            findNavController().popBackStack()
        }
// In setupClickListeners()
        binding.btnNext.setOnClickListener {
            Log.d("NEXT_BUTTON_DEBUG", "Next button was clicked.")

            val coral = viewModel.uiState.value.coral
            val locationId = viewModel.selectedLocationId.value

            if (coral != null && locationId != null) {
                // This is the success path
                Log.d("NEXT_BUTTON_DEBUG", "SUCCESS: Both coral and location are available. Navigating...")

                val nickname = binding.etCoralNickname.text.toString().trim()
                val message = binding.etMessage.text.toString().trim()

                val action = UserAddCoralFragmentDirections
                    .actionUserAddCoralFragmentToUserPaymentCoralFragment(
                        coralId = coral.id_tk,
                        locationId = locationId,
                        coralNickname = nickname,
                        message = message
                    )
                findNavController().navigate(action)
            } else {
                // This is the failure path
                Log.e("NEXT_BUTTON_DEBUG", "FAILURE: Cannot navigate.")
                Log.e("NEXT_BUTTON_DEBUG", "Is coral null? -> ${coral == null}")
                Log.e("NEXT_BUTTON_DEBUG", "Is locationId null? -> ${locationId == null}")
                Toast.makeText(requireContext(), "Please select a location first.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.etSelectedLocation.setOnClickListener {
            val coral = viewModel.uiState.value.coral
            if (coral != null) {
                // Navigate to the location selection screen, passing the current coral's ID
                val action = UserAddCoralFragmentDirections
                    .actionUserAddCoralFragmentToLocationSelectionFragment(coral.id_tk)
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "Waiting for coral details...", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // In your UserAddCoralFragment class
    private fun observeViewModel() {
        // OBSERVER 1: For the StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                state.coral?.let { coral ->
                    binding.tvSelectedCoralSpecies.text = coral.tk_name
                }

                state.error?.let { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        // OBSERVER 2: For the selectedLocationName LiveData
        viewModel.selectedLocationName.observe(viewLifecycleOwner) { locationName ->
            binding.etSelectedLocation.setText(locationName)
        }

        // OBSERVER 3: For the button's enabled state LiveData
        viewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnNext.isEnabled = isEnabled
        }
    }

    // This is important to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // The companion object should create an instance of THIS fragment
    companion object {
        @JvmStatic
        fun newInstance() = UserAddCoralFragment()
    }
}

// It's good practice to put the factory in its own file,
// but for simplicity, you can also have it here or inside the fragment.
class UserAddCoralViewModelFactory(
    private val coralRepository: CoralRepositoryImpl,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserAddCoralViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserAddCoralViewModel(coralRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}