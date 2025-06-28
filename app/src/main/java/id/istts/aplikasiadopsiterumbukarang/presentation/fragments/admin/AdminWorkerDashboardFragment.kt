package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.animation.ObjectAnimator
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAdminWorkerDashboardBinding // <-- IMPORT KELAS BINDING
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.WorkerAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.workerdashboard.AdminWorkerDashboardViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.workerdashboard.AdminWorkerDashboardViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminWorkerDashboardFragment : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AdminWorkerDashboardViewModel
    private lateinit var workerAdapter: WorkerAdapter

    // Objek Binding untuk mengakses View secara aman dan efisien
    private var _binding: FragmentAdminWorkerDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Meng-inflate layout menggunakan DataBinding
        _binding = FragmentAdminWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Inisialisasi ViewModel menggunakan Factory
        val apiService = RetrofitClient.instance
        val viewModelFactory = AdminWorkerDashboardViewModelFactory(apiService)
        viewModel = ViewModelProvider(this, viewModelFactory)[AdminWorkerDashboardViewModel::class.java]

        // Menghubungkan ViewModel dan LifecycleOwner ke layout
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Validasi akses sebelum melanjutkan
        if (validateAccess()) {
            setupUserGreeting()
            setupRecyclerView()
            setupBottomNavigation()
            observeViewModel()
            loadWorkers()
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

    private fun setupUserGreeting() {
        val userName = sessionManager.fetchUserName() ?: "Admin"
        // Mengatur teks sapaan. Ini juga bisa dipindahkan ke ViewModel jika diinginkan
        binding.tvUserGreeting.text = "Hi, $userName"
    }

    private fun setupRecyclerView() {
        workerAdapter = WorkerAdapter { worker ->
            navigateToEditWorker(worker)
        }
        binding.rvWorkers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workerAdapter
        }
    }

    private fun observeViewModel() {
        // Mengamati perubahan daftar worker untuk diperbarui ke adapter
        viewModel.workers.observe(viewLifecycleOwner, Observer { workers ->
            workers?.let {
                workerAdapter.updateWorkers(it)
            }
        })

        // Mengamati perubahan pada search query dari EditText (via Two-way DataBinding)
        viewModel.searchQuery.observe(viewLifecycleOwner, Observer { query ->
            // Debounce untuk mengurangi frekuensi pencarian saat pengguna mengetik
            lifecycleScope.launch {
                delay(300) // Tunggu 300ms
                if (query == viewModel.searchQuery.value) {
                    viewModel.searchWorkers(query ?: "")
                }
            }
        })

        // Mengamati event untuk navigasi ke halaman tambah worker
        viewModel.navigateToAddWorker.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate == true) {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_addWorkerFragment)
                viewModel.doneNavigatingToAddWorker() // Reset event agar tidak ter-trigger lagi
            }
        })

        // Mengamati event untuk melakukan logout
        viewModel.navigateToLogout.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate == true) {
                performLogout()
                viewModel.doneNavigatingToLogout() // Reset event
            }
        })

        // Mengamati error
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                Log.e(TAG, "Error loading workers: $error")
                if (error.contains("authentication") || error.contains("token")) {
                    navigateToLogin()
                } else {
                    Snackbar.make(binding.root, "Error: $error", Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun loadWorkers() {
        viewModel.loadWorkers(sessionManager)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.let { bottomNav ->
            bottomNav.selectedItemId = R.id.nav_worker

            bottomNav.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_coral_seed -> {
                        animateBottomNavClick(menuItem) { navigateToAdminDashboard() }
                        true
                    }
                    R.id.nav_place -> {
                        animateBottomNavClick(menuItem) { navigateToAdminPlace() }
                        true
                    }
                    R.id.nav_worker -> true // Sudah di halaman ini
                    else -> false
                }
            }

            bottomNav.setOnItemReselectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_worker -> {
                        binding.rvWorkers.smoothScrollToPosition(0)
                        binding.searchEditText.setText("") // Hapus pencarian
                    }
                    // Navigasi ulang jika item lain dipilih kembali
                    R.id.nav_coral_seed -> navigateToAdminDashboard()
                    R.id.nav_place -> navigateToAdminPlace()
                }
            }
        }
    }

    private fun animateBottomNavClick(menuItem: MenuItem, action: () -> Unit) {
        // Fungsi animasi tetap sama
        val viewToAnimate = binding.bottomNavigationView.findViewById<View>(menuItem.itemId)
        val scaleAnimation = ObjectAnimator.ofFloat(viewToAnimate, "scaleY", 1f, 0.95f, 1f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
        }
        scaleAnimation.start()
        lifecycleScope.launch {
            delay(100)
            action()
        }
    }

    private fun performLogout() {
        if (isAdded && !isDetached) {
            sessionManager.clearSession()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_loginFragment)
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to Login failed", e)
            }
        }
    }

    private fun navigateToEditWorker(worker: Worker) {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                val bundle = Bundle().apply {
                    putString("workerId", worker.id_user)
                    putString("workerName", worker.full_name)
                    putString("workerEmail", worker.email)
                    putString("workerBalance", worker.balance)
                    putString("workerStatus", worker.user_status)
                }
                Snackbar.make(binding.root, "Opening edit for ${worker.full_name}", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(
                    R.id.action_adminWorkerDashboardFragment_to_editUserFragment,
                    bundle
                )
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to Edit Worker failed", e)
                Snackbar.make(binding.root, "Error opening edit screen", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToAdminDashboard() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_adminDashboardFragment)
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to Admin Dashboard failed", e)
            }
        }
    }

    private fun navigateToAdminPlace() {
        if (isAdded && !isDetached && !isRemoving) {
            try {
                findNavController().navigate(R.id.action_adminWorkerDashboardFragment_to_adminPlaceDashboardFragment)
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to Admin Place failed", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (validateAccess()) {
            binding.bottomNavigationView.selectedItemId = R.id.nav_worker
            loadWorkers()
            setupUserGreeting()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Penting untuk membersihkan referensi binding untuk mencegah memory leak
        binding.rvWorkers.adapter = null
        _binding = null
    }
}