import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// Ganti com.example.app dengan package name aplikasi Anda
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentWorkerDashboardBinding

class WorkerDashboardFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardBinding? = null
    // Properti ini hanya valid antara onCreateView dan onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MotionLayout akan memulai animasi secara otomatis karena
        // app:autoTransition="animateToEnd" di file scene.
        // Anda tidak perlu menambahkan kode Kotlin khusus untuk memulai animasi utama.

        // Setup listener atau logic lain di sini jika diperlukan
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mission -> {
                    // Handle klik "My Mission"
                    true
                }
                R.id.navigation_profile -> {
                    // Handle klik "My Profile"
                    true
                }
                else -> false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}