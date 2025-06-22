package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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


    private fun setupClickListeners() {
        // Use the 'binding' object to access views safely
        binding.btnBack.setOnClickListener {
            // A simple and reliable way to go back
            findNavController().navigateUp()
        }

        binding.btnNext.setOnClickListener {
            val coral = viewModel.uiState.value.coral
            if (coral == null) {
                Toast.makeText(context, "Please wait, coral details are loading.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nickname = binding.etCoralNickname.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()

            // Navigate to the payment screen, passing all necessary data
            val action = UserAddCoralFragmentDirections
                .actionUserAddCoralFragmentToUserPaymentCoralFragment(
                    coralId = coral.id_tk,
                    coralNickname = nickname,
                    message = message
                )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // You can add a ProgressBar and control its visibility here
                // binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                state.coral?.let { coral ->
                    binding.tvSelectedCoralSpecies.text = coral.tk_name
                    binding.tvSelectedCoralLocation.text = coral.tk_name// Assuming your Coral model has this
                }

                state.error?.let { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            }
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