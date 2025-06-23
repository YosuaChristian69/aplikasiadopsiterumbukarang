package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentAdoptionSuccessBinding
import id.istts.aplikasiadopsiterumbukarang.databinding.FragmentUserPaymentCoralBinding


class AdoptionSuccessFragment : Fragment() {
    private var _binding: FragmentAdoptionSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("PAYMENT_LIFECYCLE", "onCreateView called.")
        _binding = FragmentAdoptionSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBackToHome.setOnClickListener {
            findNavController().navigate(R.id.action_global_userDashboardFragment)
        }
    }
}