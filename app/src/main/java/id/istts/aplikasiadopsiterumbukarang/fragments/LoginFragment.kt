package id.istts.aplikasiadopsiterumbukarang.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.istts.aplikasiadopsiterumbukarang.R
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.findNavController
import android.widget.TextView

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)
        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}