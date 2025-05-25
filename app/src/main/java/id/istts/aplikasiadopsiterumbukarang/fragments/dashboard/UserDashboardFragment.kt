package id.istts.aplikasiadopsiterumbukarang.fragments.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import id.istts.aplikasiadopsiterumbukarang.utils.SessionManager
import id.istts.aplikasiadopsiterumbukarang.R

class UserDashboardFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_userDashboardFragment_to_loginFragment)
            return
        }

        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        val userName = sessionManager.fetchUserName() ?: "User"
        welcomeTextView.text = "Welcome, $userName!"

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        // Clear user session
        sessionManager.clearSession()

        Toast.makeText(requireContext(), "Anda berhasil Logout", Toast.LENGTH_SHORT).show()

        findNavController().navigate(R.id.action_userDashboardFragment_to_loginFragment)
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserDashboardFragment()
    }
}