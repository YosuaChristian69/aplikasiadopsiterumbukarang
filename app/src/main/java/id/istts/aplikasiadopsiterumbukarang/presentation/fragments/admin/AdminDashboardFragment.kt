package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.admin

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.ViewModelFactory.ViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories.RepostioryCorral
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboardRepo.AdminDashboardViewModelFullRepo
import id.istts.aplikasiadopsiterumbukarang.databinding.DialogCoralDetailsBinding
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAdminDashboardBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.CoralClickListener
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard.AdminDashboardViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.dashboard.AdminDashboardViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModel
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editCoral.EditCoralViewModelFactory
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminDashboardViewModelFullRepo
//    private lateinit var viewModel: AdminDashboardViewModel // Atau ViewModelFullRepo Anda

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel = ViewModelProvider(this,
//            AdminDashboardViewModelFactory( // Gunakan Factory yang sesuai
//                CoralRepositoryImpl(), // Ganti dengan instance API service yang benar
//                SessionManager(requireContext())
//            )
//        )[AdminDashboardViewModel::class.java]

        val factory = ViewModelFactory(RepostioryCorral(),CoralRepositoryImpl(), SessionManager(requireContext()), FileUtils(requireContext()),requireContext())
        viewModel = ViewModelProvider(this, factory)[AdminDashboardViewModelFullRepo::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val adapter = CoralAdapter(CoralClickListener(
            itemClickListener = { coral ->
                showCoralDetailsDialog(coral) // Contoh
            },
            editClickListener = { coral ->
                // --- BAGIAN YANG DIUBAH ---
                val bundle = Bundle().apply {
                    // Ganti putInt menjadi putParcelable
                    putParcelable("coral_object", coral)
                }
                findNavController().navigate(R.id.action_adminDashboardFragment_to_editCoralFragment, bundle)
            },
            deleteClickListener = { coral ->
                viewModel.onCoralDeleteClick(coral)
            }
        ))
        binding.recyclerViewCorals.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_coral_seed -> true
                R.id.nav_place -> {
                    viewModel.onPlaceNavClick()
                    true
                }
                R.id.nav_worker -> {
                    viewModel.onWorkerNavClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        // di dalam observeViewModel()
        viewModel.shouldNavigateToLogin.observe(viewLifecycleOwner) { event ->
            // Dapatkan konten HANYA JIKA belum pernah ditangani
            event.getContentIfNotHandled()?.let { navigate ->
                if (navigate) {
                    // Pengecekan currentDestination tetap merupakan pengaman yang baik
                    if (findNavController().currentDestination?.id == R.id.adminDashboardFragment) {
                        findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
                    }
                    // Kita tidak perlu memanggil viewModel.onNavigated() lagi untuk event ini
                }
            }
        }

        viewModel.shouldNavigateToAddCoral.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_addCoralFragment)
                viewModel.onNavigated()
            }
        }

        viewModel.shouldNavigateToPlace.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_adminPlaceDashboardFragment)
                viewModel.onNavigated()
            }
        }

        viewModel.shouldNavigateToWorker.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().navigate(R.id.action_adminDashboardFragment_to_adminWorkerDashboardFragment)
                viewModel.onNavigated()
            }
        }

        viewModel.showMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onMessageShown()
            }
        }

        viewModel.showDeleteDialog.observe(viewLifecycleOwner) { coral ->
            coral?.let {
                showDeleteConfirmationDialog(it)
            }
        }
    }

    private fun showDeleteConfirmationDialog(coral: Coral) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Coral")
            .setMessage("Are you sure you want to delete '${coral.tk_name}'?\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> viewModel.confirmDeleteCoral() }
            .setNegativeButton("Cancel") { _, _ -> viewModel.dismissDeleteDialog() }
            .setCancelable(false)
            .show()
    }

    private fun showCoralDetailsDialog(coral: Coral) {
        val dialog = Dialog(requireContext())

        // Inflate layout dialog menggunakan Data Binding
        val binding = DialogCoralDetailsBinding.inflate(layoutInflater)

        // Set data 'coral' ke variabel di dalam layout dialog
        binding.coral = coral

        // Atur konten dialog
        dialog.setContentView(binding.root)

        // Kustomisasi jendela dialog
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val windowWidth = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(windowWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        // --- Isi data yang tidak bisa langsung di-bind ---
        // Load gambar
        Glide.with(this)
            .load(coral.img_path)
            .placeholder(R.drawable.ic_coral_logo)
            .error(R.drawable.ic_coral_logo)
            .into(binding.ivDialogCoralImage)

        // Atur harga dan stok dengan format
        binding.tvDialogCoralPrice.text = "Rp ${String.format("%,d", coral.harga_tk).replace(',', '.')}"
        binding.tvDialogCoralStock.text = "${coral.stok_tersedia} units"
        binding.tvDialogDescription.text = coral.description.ifEmpty { "No description available" }

        // Logika untuk status stok (warna dan teks)
        val stockStatusText: String
        val stockStatusColor: Int
        val checkmarkText: String

        when {
            coral.stok_tersedia == 0 -> {
                stockStatusText = "Out of Stock"
                stockStatusColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                checkmarkText = "❌"
            }
            coral.stok_tersedia < 10 -> {
                stockStatusText = "Low Stock"
                stockStatusColor = ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light)
                checkmarkText = "⚠️"
            }
            else -> {
                stockStatusText = "In Stock"
                stockStatusColor = ContextCompat.getColor(requireContext(), R.color.coral_accent) // ganti dengan warna sukses Anda
                checkmarkText = "✅"
            }
        }

        binding.tvDialogStockStatus.text = stockStatusText
        binding.llStockStatusContainer.setBackgroundColor(stockStatusColor)
        binding.tvchecklist.text = checkmarkText

        // Atur listener untuk tombol close
        binding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        // Tampilkan dialog
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }
}