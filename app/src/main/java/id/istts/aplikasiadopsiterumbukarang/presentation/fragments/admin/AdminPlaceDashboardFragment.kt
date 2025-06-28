package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAdminPlaceDashboardBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.RepositoryProvider
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.LokasiAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.LokasiClickListener
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.placeDashboard.AdminPlaceDashboardViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.placeDashboard.AdminPlaceDashboardViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminPlaceDashboardFragment : Fragment() {

    private var _binding: FragmentAdminPlaceDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminPlaceDashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPlaceDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupBinding()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupViewModel() {
        val factory = AdminPlaceDashboardViewModelFactory(
            lokasiRepository = RepositoryProvider.lokasiRepository,
            sessionManager = SessionManager(requireContext())
        )
        viewModel = ViewModelProvider(this, factory)[AdminPlaceDashboardViewModel::class.java]
    }

    private fun setupBinding() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun setupRecyclerView() {
        // ===== PERUBAHAN DIMULAI DI SINI =====
        // Kita sekarang membuat listener menggunakan 'object :' karena ini adalah interface
        val clickListener = object : LokasiClickListener {
            override fun onEditClick(lokasi: Lokasi) {
                // Logika untuk edit tetap sama
                Toast.makeText(context, "Edit ${lokasi.lokasiName}", Toast.LENGTH_SHORT).show()
            }

            override fun onMapsClick(lokasi: Lokasi) {
                // Logika untuk membuka maps tetap sama persis
                val gmmIntentUri = Uri.parse("geo:0,0?q=${lokasi.latitude},${lokasi.longitude}(${lokasi.lokasiName})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(context, "Google Maps tidak terpasang.", Toast.LENGTH_LONG).show()
                }
            }
        }

        val lokasiAdapter = LokasiAdapter(clickListener) // Berikan listener yang baru dibuat

        // Pastikan ID RecyclerView sudah benar sesuai file XML Anda
        binding.recyclerViewPlaces.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lokasiAdapter
        }
        // ===== PERUBAHAN SELESAI DI SINI =====
    }


    private fun setupListeners() {
        // Search
        binding.searchEditText.addTextChangedListener { text ->
            viewModel.onSearchQueryChanged(text.toString())
        }

        // Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_place
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_coral_seed -> {
                    findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_adminDashboardFragment)
                    true
                }
                R.id.nav_worker -> {
                    findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_adminWorkerDashboardFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupObservers() {
        viewModel.navigateToLogin.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_loginFragment)
                viewModel.onNavigationHandled()
            }
        }

        viewModel.navigateToAddPlace.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_adminPlaceDashboardFragment_to_addPlaceFragment4)
                viewModel.onNavigationHandled()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke fragment
        viewModel.loadInitialData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }
}