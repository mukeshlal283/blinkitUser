package com.example.userblinkit.auth

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.userblinkit.R
import com.example.userblinkit.Utils
import com.example.userblinkit.databinding.FragmentSigninBinding

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSigninBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        getStatusBarColor()
        getUserNumber()
        onContinueButtonClick()

        return binding.root
    }

    private fun onContinueButtonClick() {

        binding.etbtnContinue.setOnClickListener {
            val number = binding.etUserNumber.text.toString()

            if (number.isEmpty() || number.length != 10) {
                Utils.showToast(requireContext(), "Please enter a valid phone number")
            }

            else {
                val bundle = Bundle()
                bundle.putString("number", number)
                findNavController().navigate(R.id.action_signinFragment_to_OTPFragment, bundle)
            }

        }

    }

    private fun getUserNumber() {

        binding.etUserNumber.addTextChangedListener ( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val len = number?.length

                if(len == 10) {
                    binding.etbtnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.etbtnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greyish_blue))
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        } )
    }

    private fun getStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}