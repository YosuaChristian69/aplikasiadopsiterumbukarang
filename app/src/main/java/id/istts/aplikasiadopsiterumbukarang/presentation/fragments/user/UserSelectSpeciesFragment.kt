package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.UserCoralSelectionAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.UserSelectSpeciesViewModel
//import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserSelectSpeciesViewModel
//import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserSelectSpeciesViewModel
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl // Make sure this is accessible
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserSelectSpeciesFragment : Fragment() {

    private lateinit var viewModel: UserSelectSpeciesViewModel
    private lateinit var coralAdapter: UserCoralSelectionAdapter
    private lateinit var coralRecyclerView: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var btnSelect: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_select_species, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This is a simple way to create a ViewModelFactory.
        // For larger apps, consider a dependency injection library like Hilt.
//        val coralRepository = UserSelectSpeciesViewModel(
//            CoralRepositoryImpl(/* Pass your API service if needed */),
//            SessionManager(requireContext())
//        )
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // We must pass BOTH dependencies here
                return UserSelectSpeciesViewModel(CoralRepositoryImpl(), SessionManager(requireContext())) as T
            }
        }
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserSelectSpeciesViewModel::class.java)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        coralRecyclerView = view.findViewById(R.id.coralRecyclerView)
        btnBack = view.findViewById(R.id.btnBack)
        btnSelect = view.findViewById(R.id.btnSelect)
    }

    private fun setupRecyclerView() {
        // The adapter is initialized here. The click listener calls the ViewModel.
        coralAdapter = UserCoralSelectionAdapter(emptyList()) { coral ->
            viewModel.onCoralSelected(coral)
        }

        coralRecyclerView.apply {
            // Use GridLayoutManager for a 2-column grid
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = coralAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp() // A simpler way to go back
        }

        btnSelect.setOnClickListener {
            val selectedCoral = viewModel.uiState.value.selectedCoral
            if (selectedCoral != null) {
                Log.d("UserSelectSpecies", "Proceeding with ${selectedCoral.tk_name}")

                // TODO: Navigate to the next fragment, passing the coral's ID
                // val action = UserSelectSpeciesFragmentDirections.actionToNextFragment(selectedCoral.id_tk)
                // findNavController().navigate(action)

                Toast.makeText(requireContext(), "${selectedCoral.tk_name} selected!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please select a coral first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // Update the adapter with the list of corals
                coralAdapter.updateData(state.availableCorals)

                // Update the adapter's selection state
                coralAdapter.setSelectedCoral(state.selectedCoral)

                // Handle loading state (e.g., show/hide a ProgressBar)
                // progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                // Show any errors as a Toast
                state.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError() // Clear the error after showing it
                }
            }
        }
    }
}