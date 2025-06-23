package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.R
// Import the new Enum class
import id.istts.aplikasiadopsiterumbukarang.presentation.SelectionMode
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.UserCoralSelectionAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.UserSelectSpeciesViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// The class name stays the same, as you requested.
class UserSelectSpeciesFragment : Fragment() {

    private lateinit var viewModel: UserSelectSpeciesViewModel
    private lateinit var coralAdapter: UserCoralSelectionAdapter
    private lateinit var coralRecyclerView: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var btnSelect: Button
    private lateinit var tvTitle: TextView // Added for changing the title

    // Use Safe Args to easily get the navigation argument
    private val args: UserSelectSpeciesFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_select_species, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The ViewModel Factory now needs to pass the selectionMode to the ViewModel
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // Pass the mode from the fragment's arguments into the ViewModel constructor
                return UserSelectSpeciesViewModel(
                    CoralRepositoryImpl(),
                    SessionManager(requireContext()),
                    args.selectionMode // Pass the mode here
                ) as T
            }
        }
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserSelectSpeciesViewModel::class.java)

        initializeViews(view)
        setupRecyclerView()
        setupUIForMode() // New function to set up UI text
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        coralRecyclerView = view.findViewById(R.id.coralRecyclerView)
        btnBack = view.findViewById(R.id.btnBack)
        btnSelect = view.findViewById(R.id.btnSelect)
        tvTitle = view.findViewById(R.id.tvTitle) // Initialize the title TextView
    }

    // This new function changes the UI based on the mode
    private fun setupUIForMode() {
        when (args.selectionMode) {
            SelectionMode.USER_SINGLE_SELECTION -> {
                tvTitle.text = "SELECT CORAL FOR ADOPTION"
                btnSelect.text = "SELECT"
            }
            SelectionMode.ADMIN_MULTI_SELECTION -> {
                tvTitle.text = "SET AVAILABLE CORALS"
                btnSelect.text = "CONFIRM SELECTION"
            }
        }
    }

    private fun setupRecyclerView() {
        // The adapter's click listener now calls the new toggle function
        coralAdapter = UserCoralSelectionAdapter(emptyList()) { coral ->
            viewModel.toggleCoralSelection(coral)
        }

        coralRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = coralAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSelect.setOnClickListener {
            // The logic inside the select button now depends on the mode
            when (args.selectionMode) {
                SelectionMode.USER_SINGLE_SELECTION -> handleUserSelection()
                SelectionMode.ADMIN_MULTI_SELECTION -> handleAdminSelection()
            }
        }
    }

    private fun handleUserSelection() {
        // Get the single selected coral (first from the list)
        val selectedCoral = viewModel.uiState.value.selectedCorals.firstOrNull()
        if (selectedCoral != null) {
            Log.d("SelectSpeciesFragment", "USER: Proceeding with ${selectedCoral.tk_name}")

            val action = UserSelectSpeciesFragmentDirections
                .actionUserSelectSpeciesFragmentToUserAddCoralFragment(selectedCoral.id_tk)
            findNavController().navigate(action)

            Toast.makeText(requireContext(), "${selectedCoral.tk_name} selected!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please select a coral first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleAdminSelection() {
        // Get the whole list of selected corals
        val selectedCorals = viewModel.uiState.value.selectedCorals
        if (selectedCorals.isNotEmpty()) {
            val selectedIds = ArrayList(selectedCorals.map { it.id_tk })
            Log.d("SelectSpeciesFragment", "ADMIN: Returning ${selectedIds.size} selected coral IDs.")

            // Use the Fragment Result API to send the list of IDs back to the previous screen
            setFragmentResult("coralSelectionRequest", bundleOf("selectedCoralIds" to selectedIds))
            findNavController().popBackStack()

            Toast.makeText(requireContext(), "${selectedIds.size} corals selected!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please select at least one coral.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                coralAdapter.updateData(state.availableCorals)

                // The adapter now needs to be updated with a list of selected items
                coralAdapter.updateSelection(state.selectedCorals)

                state.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }
}